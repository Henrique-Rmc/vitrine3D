package com.store.vitrine3d.infrastructure.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

/**
 * O SDK do MinIO instancia as classes {@code *Args} via reflexão (construtor sem argumentos)
 * dentro de {@code BaseArgs.Builder.newInstance()}. Sem esse hint, o binário GraalVM falha em
 * runtime com "class X must have no argument constructor" ao chamar qualquer {@code .build()}.
 */
class MinioArgsRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection()
                .registerType(BucketExistsArgs.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(MakeBucketArgs.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(SetBucketPolicyArgs.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS)
                .registerType(PutObjectArgs.class, MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
    }
}
