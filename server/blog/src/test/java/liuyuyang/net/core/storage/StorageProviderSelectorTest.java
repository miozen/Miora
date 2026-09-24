package liuyuyang.net.core.storage;

import liuyuyang.net.core.execption.CustomException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageProviderSelectorTest {
    @Test
    void defaultsToLocalWithoutS3Configuration() {
        StorageProvider local = provider("local");
        StorageProviderSelector selector = new StorageProviderSelector(Arrays.asList(local, provider("s3")),
                s3Properties("", "", "", "", ""), "local");

        assertEquals(local, selector.current());
    }

    @Test
    void selectsS3WhenAllRequiredConfigurationIsPresent() {
        StorageProvider s3 = provider("s3");
        StorageProviderSelector selector = new StorageProviderSelector(Arrays.asList(provider("local"), s3),
                s3Properties("https://s3.example.com", "ap-guangzhou", "images", "key", "secret"), " S3 ");

        assertEquals(s3, selector.current());
    }

    @Test
    void rejectsUnknownProviderAndMissingS3Fields() {
        assertThrows(CustomException.class, () -> new StorageProviderSelector(
                Arrays.asList(provider("local"), provider("s3")),
                s3Properties("", "", "", "", ""), "unsupported"));
        CustomException error = assertThrows(CustomException.class, () -> new StorageProviderSelector(
                Arrays.asList(provider("local"), provider("s3")),
                s3Properties("", "ap-guangzhou", "images", "key", "secret"), "s3"));

        assertEquals("S3 配置缺少字段: STORAGE_S3_ENDPOINT", error.getMessage());
    }

    private static StorageProvider provider(String name) {
        StorageProvider provider = mock(StorageProvider.class);
        when(provider.name()).thenReturn(name);
        return provider;
    }

    private static S3StorageProperties s3Properties(String endpoint, String region, String bucket,
                                                     String accessKey, String secretKey) {
        return new S3StorageProperties(endpoint, region, bucket, "", accessKey, secretKey, "", false);
    }
}
