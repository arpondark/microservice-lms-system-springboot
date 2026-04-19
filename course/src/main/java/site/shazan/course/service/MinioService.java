package site.shazan.course.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
public class MinioService {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.access-key}")
    private String username;

    @Value("${minio.secret-key}")
    private String password;

    @Value("${minio.public-url}")
    private String publicUrl;

    private MinioClient client;

    @jakarta.annotation.PostConstruct
    public void init() {
        this.client = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(username, password)
                .build();
    }

    public String upload(MultipartFile file, String bucket) {
        try {
            String objectName = Objects.requireNonNull(file.getOriginalFilename(), "File name is required");

            client.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1L)
                            .contentType(file.getContentType())
                            .build()
            );

            return buildPublicUrl(bucket, objectName);

        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    private String buildPublicUrl(String bucket, String objectName) {
        String baseUrl = publicUrl;
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/" + bucket + "/" + objectName;
    }
}



