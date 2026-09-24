package liuyuyang.net.core.storage;

import liuyuyang.net.core.execption.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** 仅由服务端环境变量注入的 S3 配置，避免凭据进入数据库与浏览器。 */
@Component
public class S3StorageProperties {
    private final String endpoint;
    private final String region;
    private final String bucket;
    private final String prefix;
    private final String accessKey;
    private final String secretKey;
    private final String publicUrl;
    private final boolean pathStyle;

    public S3StorageProperties(
            @Value("${storage.s3.endpoint:}") String endpoint,
            @Value("${storage.s3.region:}") String region,
            @Value("${storage.s3.bucket:}") String bucket,
            @Value("${storage.s3.prefix:}") String prefix,
            @Value("${storage.s3.access-key:}") String accessKey,
            @Value("${storage.s3.secret-key:}") String secretKey,
            @Value("${storage.s3.public-url:}") String publicUrl,
            @Value("${storage.s3.path-style:false}") boolean pathStyle) {
        this.endpoint = trim(endpoint); this.region = trim(region); this.bucket = trim(bucket);
        this.prefix = trim(prefix).replaceAll("^/+|/+$", ""); this.accessKey = trim(accessKey);
        this.secretKey = trim(secretKey); this.publicUrl = trim(publicUrl).replaceAll("/+$", ""); this.pathStyle = pathStyle;
    }

    public void validate() { required(endpoint, "STORAGE_S3_ENDPOINT"); required(region, "STORAGE_S3_REGION"); required(bucket, "STORAGE_S3_BUCKET"); required(accessKey, "STORAGE_S3_ACCESS_KEY"); required(secretKey, "STORAGE_S3_SECRET_KEY"); }
    public String endpoint() { return endpoint; } public String region() { return region; } public String bucket() { return bucket; }
    public String prefix() { return prefix; } public String accessKey() { return accessKey; } public String secretKey() { return secretKey; }
    public String publicUrl() { return publicUrl; } public boolean pathStyle() { return pathStyle; }
    private static String trim(String value) { return value == null ? "" : value.trim(); }
    private static void required(String value, String name) { if (value.isEmpty()) throw new CustomException("S3 配置缺少字段: " + name); }
}
