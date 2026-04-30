package com.andrew.smartielts.common.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {

    private String endpoint;
    private String region;
    private String accessKeyId;
    private String accessKeySecret;
    private Map<String, BucketConfig> buckets = new LinkedHashMap<>();

    public BucketConfig requireBucket(String bucketKey) {
        if (bucketKey == null || bucketKey.isBlank()) {
            throw new RuntimeException("OSS bucket key is required");
        }
        BucketConfig bucketConfig = buckets.get(bucketKey);
        if (bucketConfig == null) {
            throw new RuntimeException("OSS bucket config not found: " + bucketKey);
        }
        if (bucketConfig.getBucketName() == null || bucketConfig.getBucketName().isBlank()) {
            throw new RuntimeException("OSS bucket-name is missing for: " + bucketKey);
        }
        if (bucketConfig.getDomain() == null || bucketConfig.getDomain().isBlank()) {
            throw new RuntimeException("OSS domain is missing for: " + bucketKey);
        }
        return bucketConfig;
    }

    @Data
    public static class BucketConfig {
        private String bucketName;
        private String domain;

        public String normalizedDomain() {
            if (domain == null) {
                return null;
            }
            String value = domain.trim();
            while (value.endsWith("/")) {
                value = value.substring(0, value.length() - 1);
            }
            return value;
        }
    }
}