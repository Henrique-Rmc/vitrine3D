package com.store.vitrine3d.infrastructure.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class MinioStorageServiceImpl implements StorageService {

    private final MinioProperties props;
    private MinioClient client;

    public MinioStorageServiceImpl(MinioProperties props) {
        this.props = props;
    }

    @PostConstruct
    void init() throws Exception {
        client = MinioClient.builder()
                .endpoint(props.getEndpoint())
                .credentials(props.getAccessKey(), props.getSecretKey())
                .build();

        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(props.getBucketName()).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(props.getBucketName()).build());
        }
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

            return props.getEndpoint() + "/" + props.getBucketName() + "/" + objectName;

        } catch (Exception e) {
            throw new RuntimeException("Falha ao enviar arquivo para o MinIO", e);
        }
    }
}
