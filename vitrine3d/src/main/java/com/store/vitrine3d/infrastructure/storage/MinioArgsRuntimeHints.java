package com.store.vitrine3d.infrastructure.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.messages.ErrorResponse;
import io.minio.messages.LocationConstraint;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import java.util.List;

/**
 * O SDK do MinIO instancia as classes {@code *Args} via reflexão (construtor sem argumentos)
 * dentro de {@code BaseArgs.Builder.newInstance()}. Sem esse hint, o binário GraalVM falha em
 * runtime com "class X must have no argument constructor" ao chamar qualquer {@code .build()}.
 *
 * As classes em {@code io.minio.messages} são desserializadas via SimpleXML (reflexão sobre
 * construtor + campos) sempre que o servidor retorna XML — tanto respostas normais
 * (ex.: LocationConstraint, ao checar a região do bucket) quanto respostas de erro
 * (ErrorResponse, sempre que a chamada falha — ex.: PutBucketPolicy não suportado no R2).
 *
 * O SimpleXML cria TextLabel/ElementLabel por reflexão ao ler essas classes; são
 * package-private, por isso registradas por nome.
 */
class MinioArgsRuntimeHints implements RuntimeHintsRegistrar {

    private static final TypeReference CONTACT = TypeReference.of("org.simpleframework.xml.core.Contact");
    private static final TypeReference FORMAT = TypeReference.of("org.simpleframework.xml.stream.Format");

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection()
                .registerType(BucketExistsArgs.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(MakeBucketArgs.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(SetBucketPolicyArgs.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(PutObjectArgs.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(RemoveObjectArgs.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(LocationConstraint.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                        MemberCategory.INTROSPECT_DECLARED_METHODS,
                        MemberCategory.DECLARED_FIELDS)
                .registerType(ErrorResponse.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS)
                .registerType(TypeReference.of("org.simpleframework.xml.core.TextLabel"),
                        b -> b.withConstructor(
                                List.of(CONTACT, TypeReference.of("org.simpleframework.xml.Text"), FORMAT),
                                ExecutableMode.INVOKE))
                .registerType(TypeReference.of("org.simpleframework.xml.core.ElementLabel"),
                        b -> b.withConstructor(
                                List.of(CONTACT, TypeReference.of("org.simpleframework.xml.Element"), FORMAT),
                                ExecutableMode.INVOKE));

        hints.resources()
                .registerPattern("com/store/vitrine3d/infrastructure/storage/MinioProperties.class")
                .registerPattern("com/store/vitrine3d/infrastructure/storage/MinioStorageServiceImpl.class");
    }
}
