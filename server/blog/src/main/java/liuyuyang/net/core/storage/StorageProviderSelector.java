package liuyuyang.net.core.storage;

import liuyuyang.net.core.execption.CustomException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** 根据运行时模式选择唯一的文件存储实现。 */
@Service
public class StorageProviderSelector {
    private final Map<String, StorageProvider> providers;
    private final String providerName;

    public StorageProviderSelector(List<StorageProvider> providers,
                                   S3StorageProperties s3Properties,
                                   @Value("${storage.provider:local}") String providerName) {
        this.providerName = normalizeProviderName(providerName);
        this.providers = providerMap(providers);
        if (!this.providers.containsKey(this.providerName)) {
            throw new CustomException("存储模式无效: " + providerName + "，仅支持 local 或 s3");
        }
        if ("s3".equals(this.providerName)) {
            s3Properties.validate();
        }
    }

    public StorageProvider current() {
        return providers.get(providerName);
    }

    private static String normalizeProviderName(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static Map<String, StorageProvider> providerMap(List<StorageProvider> providers) {
        Map<String, StorageProvider> result = new HashMap<>();
        for (StorageProvider provider : providers) {
            String name = normalizeProviderName(provider.name());
            if (!"local".equals(name) && !"s3".equals(name)) {
                continue;
            }
            if (result.put(name, provider) != null) {
                throw new CustomException("存储模式重复注册: " + name);
            }
        }
        return result;
    }
}
