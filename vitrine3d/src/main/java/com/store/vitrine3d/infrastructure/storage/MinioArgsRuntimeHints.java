package com.store.vitrine3d.infrastructure.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.messages.ErrorResponse;
import io.minio.messages.LocationConstraint;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * O SDK do MinIO instancia as classes {@code *Args} via reflexão (construtor sem argumentos)
 * dentro de {@code BaseArgs.Builder.newInstance()}. Sem esse hint, o binário GraalVM falha em
 * runtime com "class X must have no argument constructor" ao chamar qualquer {@code .build()}.
 *
 * As classes em {@code io.minio.messages} são desserializadas via SimpleXML (reflexão sobre
 * construtor + campos) sempre que o servidor retorna XML — tanto respostas normais
 * (ex.: LocationConstraint, ao checar a região do bucket) quanto respostas de erro
 * (ErrorResponse, sempre que a chamada falha — ex.: PutBucketPolicy não suportado no R2).
 */
class MinioArgsRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection()
                .registerType(BucketExistsArgs.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(MakeBucketArgs.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(SetBucketPolicyArgs.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(PutObjectArgs.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(LocationConstraint.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS)
                .registerType(ErrorResponse.class,
                        MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.DECLARED_FIELDS);
    }
}
