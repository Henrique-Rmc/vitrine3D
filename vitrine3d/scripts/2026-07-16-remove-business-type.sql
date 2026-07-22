-- BusinessType deixa de ter qualquer efeito no schema de atributos — vira so um rotulo que
-- a loja escolhe no cadastro (tracking/analytics). Quem monta o schema de atributos de cada
-- loja agora e o ProductType (criado pela propria loja). Esse script remove o mecanismo de
-- atributo global compartilhado entre lojas (e tudo que dependia dele: isolamento de valor
-- por loja, esconder-sem-apagar) e torna todo atributo obrigatoriamente dono de uma loja.
--
-- Necessario porque producao roda com spring.jpa.hibernate.ddl-auto=validate (nunca cria
-- schema sozinho) — ver application-prod.properties. A entidade nova nem tem mais o campo
-- businessType, entao rodar isso DEPOIS do deploy deixaria o boot com o schema desalinhado.
--
-- DESTRUTIVO: apaga todo AttributeDefinition global (store_id IS NULL) e suas opcoes. Loja
-- que hoje depende de um atributo global perde esse campo do conjunto efetivo dela ate
-- recriar como atributo proprio. Os valores ja gravados em Product.attributes (JSONB) pras
-- chaves apagadas NAO sao tocados — ficam orfaos, mesmo nivel de tolerancia que ja existe
-- hoje pra atributo escondido/deletado.
--
-- QUANDO RODAR: antes do deploy do codigo novo.

BEGIN;

-- 1. store_attribute_options tem FK NOT NULL pra attribute_definitions SEM ON DELETE
--    CASCADE, e essas linhas existem justamente pros atributos globais que estamos
--    prestes a apagar — precisa derrubar essa tabela ANTES de apagar as linhas globais,
--    senao a DELETE do passo 4 quebra por violacao de FK.
DROP TABLE IF EXISTS store_attribute_options;

-- 2. Mecanismo de esconder-sem-apagar atributo global tambem deixa de fazer sentido —
--    nao existe mais nada compartilhado com outra loja pra esconder.
DROP TABLE IF EXISTS store_hidden_attributes;

-- 3. Remove as opcoes dos atributos globais antes de remover os proprios atributos.
DELETE FROM attribute_definition_options
 WHERE attribute_definition_id IN (SELECT id FROM attribute_definitions WHERE store_id IS NULL);

-- 4. Remove os atributos globais em si — dai em diante todo AttributeDefinition e de uma loja.
DELETE FROM attribute_definitions WHERE store_id IS NULL;

-- 5. Derruba a constraint unica antiga de 4 colunas (idempotente via information_schema,
--    nunca por nome hardcoded — Hibernate nomeia diferente de um script manual).
DO $$
DECLARE
    old_constraint record;
BEGIN
    FOR old_constraint IN
        SELECT tc.constraint_name FROM information_schema.table_constraints tc
        WHERE tc.table_name = 'attribute_definitions' AND tc.constraint_type = 'UNIQUE'
          AND tc.constraint_name IN (
              SELECT constraint_name FROM information_schema.key_column_usage
              WHERE table_name = 'attribute_definitions' AND column_name = 'store_id')
          AND tc.constraint_name IN (
              SELECT constraint_name FROM information_schema.key_column_usage
              WHERE table_name = 'attribute_definitions' AND column_name = 'attribute_key')
          AND tc.constraint_name IN (
              SELECT constraint_name FROM information_schema.key_column_usage
              WHERE table_name = 'attribute_definitions' AND column_name = 'business_type_id')
    LOOP
        EXECUTE format('ALTER TABLE attribute_definitions DROP CONSTRAINT %I', old_constraint.constraint_name);
    END LOOP;
END $$;

-- 6. Derruba a FK e a coluna business_type_id.
DO $$
DECLARE
    fk record;
BEGIN
    FOR fk IN
        SELECT tc.constraint_name FROM information_schema.table_constraints tc
        JOIN information_schema.key_column_usage kcu
          ON tc.constraint_name = kcu.constraint_name AND tc.table_schema = kcu.table_schema
        WHERE tc.table_name = 'attribute_definitions' AND tc.constraint_type = 'FOREIGN KEY'
          AND kcu.column_name = 'business_type_id'
    LOOP
        EXECUTE format('ALTER TABLE attribute_definitions DROP CONSTRAINT %I', fk.constraint_name);
    END LOOP;
END $$;

ALTER TABLE attribute_definitions DROP COLUMN IF EXISTS business_type_id;

-- 7. store_id vira obrigatorio (seguro agora — linhas com store_id nulo ja foram removidas
--    no passo 4).
ALTER TABLE attribute_definitions ALTER COLUMN store_id SET NOT NULL;

-- 8. Cria a nova constraint unica de 3 colunas.
DO $$
DECLARE
    already_exists boolean;
BEGIN
    SELECT EXISTS (
        SELECT tc.constraint_name FROM information_schema.table_constraints tc
        WHERE tc.table_name = 'attribute_definitions' AND tc.constraint_type = 'UNIQUE'
          AND tc.constraint_name IN (
              SELECT constraint_name FROM information_schema.key_column_usage
              WHERE table_name = 'attribute_definitions' AND column_name = 'store_id')
          AND tc.constraint_name IN (
              SELECT constraint_name FROM information_schema.key_column_usage
              WHERE table_name = 'attribute_definitions' AND column_name = 'product_type_id')
          AND tc.constraint_name IN (
              SELECT constraint_name FROM information_schema.key_column_usage
              WHERE table_name = 'attribute_definitions' AND column_name = 'attribute_key')
    ) INTO already_exists;

    IF NOT already_exists THEN
        ALTER TABLE attribute_definitions
            ADD CONSTRAINT uk_attribute_definitions_store_ptype_key
            UNIQUE (store_id, product_type_id, attribute_key);
    END IF;
END $$;

-- 9. business_types (tabela) e stores.business_type_id (coluna) continuam existindo — so
--    perdem o vinculo com atributos. Mas a lista de seed ficou mais generica (menos
--    verticais, nomes/slugs diferentes pra conceitos que se fundiram — ex.: "roupas" virou
--    "moda-vestuario", "alimentos"+"bebidas" viraram um so "alimentos-bebidas"). Sem esse
--    passo, o DataInitializer novo criaria linhas duplicadas ao lado das antigas (o guard
--    dele so olha por slug exato). Reconcilia idempotente, funciona rodando antes OU depois
--    do primeiro boot do codigo novo (que ja pode ter criado a linha nova):
DO $$
DECLARE
    mapping record;
    old_id bigint;
    new_id bigint;
BEGIN
    FOR mapping IN
        SELECT * FROM (VALUES
            ('arte', 'arte-artesanato', 'Arte e Artesanato'),
            ('roupas', 'moda-vestuario', 'Moda e Vestuário'),
            ('medicos', 'consultorios-clinicas', 'Consultórios e Clínicas'),
            ('moveis-decoracao', 'moveis', 'Móveis'),
            ('alimentos', 'alimentos-bebidas', 'Alimentos e Bebidas'),
            ('bebidas', 'alimentos-bebidas', 'Alimentos e Bebidas'),
            ('cosmeticos-perfumaria', 'beleza-estetica', 'Beleza e Estética')
        ) AS t(old_slug, new_slug, new_name)
    LOOP
        SELECT id INTO old_id FROM business_types WHERE slug = mapping.old_slug;
        IF old_id IS NULL THEN CONTINUE; END IF; -- ja foi tratado nesta mesma rodada, ou nunca existiu

        SELECT id INTO new_id FROM business_types WHERE slug = mapping.new_slug;

        IF new_id IS NULL THEN
            -- Alvo novo ainda nao existe: reaproveita a linha antiga (preserva o id, e
            -- portanto qualquer stores.business_type_id que ja aponte pra ela).
            UPDATE business_types SET name = mapping.new_name, slug = mapping.new_slug WHERE id = old_id;
        ELSIF NOT EXISTS (SELECT 1 FROM stores WHERE business_type_id = old_id) THEN
            -- Alvo novo ja existe e nenhuma loja usa a linha antiga: so remove a antiga.
            DELETE FROM business_types WHERE id = old_id;
        ELSE
            -- Alvo novo ja existe E alguma loja ainda aponta pra antiga: reaponta a loja
            -- pro id novo antes de remover a antiga (evita loja orfa e duplicata).
            UPDATE stores SET business_type_id = new_id WHERE business_type_id = old_id;
            DELETE FROM business_types WHERE id = old_id;
        END IF;
    END LOOP;
END $$;

-- Verificacao antes de COMMIT
SELECT count(*) AS atributos_globais_restantes FROM attribute_definitions WHERE store_id IS NULL;
SELECT table_name FROM information_schema.tables WHERE table_name = 'store_attribute_options';
SELECT table_name FROM information_schema.tables WHERE table_name = 'store_hidden_attributes';
SELECT column_name FROM information_schema.columns
 WHERE table_name = 'attribute_definitions' AND column_name = 'business_type_id';

-- As duas primeiras devem vir 0/vazio, as duas ultimas devem vir vazias. Se bater, rode:
COMMIT;
-- Caso contrario, rode ROLLBACK; e investigue antes de tentar de novo.
