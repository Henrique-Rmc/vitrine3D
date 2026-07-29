package com.store.vitrine3d.infrastructure.storage;

import com.store.vitrine3d.rest.exception.InvalidImageFormatException;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.RemoveObjectArgs;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.UUID;

@Service
@ImportRuntimeHints(MinioArgsRuntimeHints.class)
public class MinioStorageServiceImpl implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioStorageServiceImpl.class);

    private static final long MAX_FILE_SIZE = 2L * 1024 * 1024; // 2 MB (second line of defense)
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/webp", "image/jpeg", "image/png"
    );

    private static final byte[] MAGIC_JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] MAGIC_PNG  = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] MAGIC_RIFF = {0x52, 0x49, 0x46, 0x46}; // WebP: RIFF....WEBP

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
                .region(props.getRegion())
                .build();

        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(props.getBucketName()).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(props.getBucketName()).build());
            log.info("MinIO bucket '{}' created", props.getBucketName());
        }

        // Provedores S3-compatible como o Cloudflare R2 não implementam PutBucketPolicy —
        // nesses casos o acesso público precisa ser configurado no próprio provedor
        // (ex.: R2.dev / custom domain no dashboard da Cloudflare). Não travamos o boot por isso.
        String publicReadPolicy = """
                {"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"AWS":["*"]},"Action":["s3:GetObject"],"Resource":["arn:aws:s3:::%s/*"]}]}
                """.formatted(props.getBucketName()).strip();
        try {
            client.setBucketPolicy(SetBucketPolicyArgs.builder()
                    .bucket(props.getBucketName())
                    .config(publicReadPolicy)
                    .build());
            log.info("MinIO bucket '{}' public-read policy applied.", props.getBucketName());
        } catch (MinioException e) {
            log.warn("Provedor não suporta PutBucketPolicy — configure acesso público ao bucket '{}' "
                    + "manualmente no painel do provedor. Causa: {}", props.getBucketName(), e.getMessage());
        }

        log.info("Public URL base: {}", props.getPublicEndpoint());
    }

    @Override
    public String uploadFile(MultipartFile file) {
        validateImageSecurity(file);

        try {
            String objectName = UUID.randomUUID() + "_" + sanitizeFilename(file.getOriginalFilename());

            client.putObject(PutObjectArgs.builder()
                    .bucket(props.getBucketName())
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(resolvedMimeType(file))
                    .build());

            // A URL pública NÃO inclui o nome do bucket no path — cada provedor tem sua
            // convenção (MinIO/S3: inclua o bucket no valor de MINIO_PUBLIC_ENDPOINT;
            // R2.dev / custom domain: já mapeiam direto pra raiz do bucket).
            String url = props.getPublicEndpoint() + "/" + objectName;
            log.debug("File uploaded: {}", url);
            return url;

        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("Failed to upload file to MinIO", e);
            throw new RuntimeException("Falha ao enviar o arquivo para o storage", e);
        }
    }

    @Override
    public void deleteFile(String url) {
        if (url == null || url.isBlank()) return;
        String objectName = url.substring(url.lastIndexOf('/') + 1);
        try {
            client.removeObject(RemoveObjectArgs.builder()
                    .bucket(props.getBucketName())
                    .object(objectName)
                    .build());
            log.debug("File deleted from MinIO: {}", objectName);
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.warn("Failed to delete file '{}' from MinIO: {}", objectName, e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    public void validateImageSecurity(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageFormatException("Image file must not be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidImageFormatException(
                    "Image size exceeds the maximum allowed limit of 2MB");
        }

        String declaredType = file.getContentType();
        if (declaredType == null || !ALLOWED_MIME_TYPES.contains(declaredType.toLowerCase())) {
            throw new InvalidImageFormatException(
                    "Invalid image format. Allowed types: image/webp, image/jpeg, image/png");
        }

        // Read first 12 bytes to verify actual magic bytes regardless of declared Content-Type
        try {
            byte[] header = new byte[12];
            int read;
            try (InputStream is = file.getInputStream()) {
                read = is.read(header, 0, header.length);
            }
            if (read < 4 || !isValidImageHeader(header)) {
                throw new InvalidImageFormatException(
                        "File content does not match a valid image signature");
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read the uploaded file", e);
        }
    }

    private boolean isValidImageHeader(byte[] h) {
        if (startsWith(h, MAGIC_JPEG)) return true;
        if (startsWith(h, MAGIC_PNG))  return true;
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
