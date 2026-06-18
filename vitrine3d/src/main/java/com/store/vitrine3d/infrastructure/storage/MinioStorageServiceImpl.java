package com.store.vitrine3d.infrastructure.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class MinioStorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageServiceImpl.class);

    private final MinioProperties props;
    private MinioClient client;

    public MinioStorageServiceImpl(MinioProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() throws Exception {
        client = MinioClient.builder()
                .endpoint(props.getEndpoint())
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();

        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(props.getBucketName()).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(props.getBucketName()).build());
            log.info("MinIO bucket '{}' created", props.getBucketName());
        }

        // Ensure the bucket allows anonymous read so image URLs are publicly accessible
        String publicReadPolicy = """
                {"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"AWS":["*"]},"Action":["s3:GetObject"],"Resource":["arn:aws:s3:::%s/*"]}]}
                """.formatted(props.getBucketName()).strip();

        client.setBucketPolicy(SetBucketPolicyArgs.builder()
                .bucket(props.getBucketName())
                .config(publicReadPolicy)
                .build());

        log.info("MinIO bucket '{}' public-read policy applied. Public URL base: {}/{}",
                props.getBucketName(), props.getPublicEndpoint(), props.getBucketName());
    }

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            String objectName = UUID.randomUUID() + "_" + file.getOriginalFilename();

            client.putObject(PutObjectArgs.builder()
                    .bucket(props.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            String url = props.getPublicEndpoint() + "/" + props.getBucketName() + "/" + objectName;
            log.debug("File uploaded: {}", url);
            return url;

        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("Failed to upload file to MinIO", e);
            throw new RuntimeException("Failed to upload file to storage", e);
        }
    }
}
