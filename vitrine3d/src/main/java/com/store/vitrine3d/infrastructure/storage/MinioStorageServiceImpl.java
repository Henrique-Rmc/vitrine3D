package com.store.vitrine3d.infrastructure.storage;

import io.minio.*;
import io.minio.errors.MinioException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.UUID;

@Service
public class MinioStorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageServiceImpl.class);

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );

    // Magic bytes de cada formato permitido
    private static final byte[] MAGIC_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] MAGIC_PNG  = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] MAGIC_GIF  = {0x47, 0x49, 0x46, 0x38};
    private static final byte[] MAGIC_RIFF = {0x52, 0x49, 0x46, 0x46}; // WebP começa com RIFF

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
        validateImageFile(file);

        try {
            String objectName = UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());

            client.putObject(PutObjectArgs.builder()
                    .bucket(props.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(resolvedMimeType(file))
                    .build());

            String url = props.getPublicEndpoint() + "/" + props.getBucketName() + "/" + objectName;
            log.debug("File uploaded: {}", url);
            return url;

        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("Failed to upload file to MinIO", e);
            throw new RuntimeException("Falha ao enviar o arquivo para o storage", e);
        }
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo não pode estar vazio");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "O arquivo excede o tamanho máximo permitido de 10MB");
        }

        String declaredType = file.getContentType();
        if (declaredType == null || !ALLOWED_MIME_TYPES.contains(declaredType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Tipo de arquivo não permitido. Formatos aceitos: JPEG, PNG, WebP, GIF");
        }

        // Lê apenas os primeiros 12 bytes para verificar os magic bytes reais
        // independente do Content-Type declarado pelo cliente
        try {
            byte[] header = new byte[12];
            int read;
            try (InputStream is = file.getInputStream()) {
                read = is.read(header, 0, header.length);
            }
            if (read < 4 || !isValidImageHeader(header)) {
                throw new IllegalArgumentException(
                        "O conteúdo do arquivo não corresponde a uma imagem válida");
            }
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível ler o arquivo enviado", e);
        }
    }

    private boolean isValidImageHeader(byte[] h) {
        if (startsWith(h, MAGIC_JPEG)) return true;
        if (startsWith(h, MAGIC_PNG))  return true;
        if (startsWith(h, MAGIC_GIF))  return true;
        // WebP: bytes 0-3 = RIFF, bytes 8-11 = WEBP
        if (h.length >= 12 && startsWith(h, MAGIC_RIFF)) {
            return h[8] == 0x57 && h[9] == 0x45 && h[10] == 0x42 && h[11] == 0x50;
        }
        return false;
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String resolvedMimeType(MultipartFile file) {
        // Usa o tipo declarado (já validado acima) para o header do objeto no MinIO
        String ct = file.getContentType();
        return ct != null ? ct : "application/octet-stream";
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null) return "upload";
        // Remove path traversal characters — mantém apenas nome do arquivo
        return originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
