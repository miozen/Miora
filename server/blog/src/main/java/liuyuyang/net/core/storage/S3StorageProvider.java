package liuyuyang.net.core.storage;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import liuyuyang.net.core.execption.CustomException;
import liuyuyang.net.vo.file.FileDirCreateVO;
import liuyuyang.net.vo.file.FileDirDeleteVO;
import liuyuyang.net.vo.file.FileDirRenameVO;
import liuyuyang.net.vo.file.FileInfoVO;
import liuyuyang.net.vo.file.FileListItemVO;
import liuyuyang.net.vo.file.FileTreeFileVO;
import liuyuyang.net.vo.file.FileTreeNodeVO;
import liuyuyang.net.vo.file.FileTreeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** S3 兼容对象存储。业务层仅使用不含 bucket/prefix 的相对 key。 */
@Service
@Slf4j
public class S3StorageProvider implements StorageProvider {
    private static final String PLACEHOLDER = ".keep";

    private final S3StorageProperties properties;
    private volatile AmazonS3 client;

    @Autowired
    public S3StorageProvider(S3StorageProperties properties) {
        this.properties = properties;
    }

    S3StorageProvider(S3StorageProperties properties, AmazonS3 client) {
        this.properties = properties;
        this.client = client;
    }

    @Override
    public String name() {
        return "s3";
    }

    @Override
    public String upload(String dir, MultipartFile file) throws IOException {
        String key = join(directory(dir), uniqueFileName(file.getOriginalFilename()));
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        if (file.getContentType() != null) {
            metadata.setContentType(file.getContentType());
        }
        try (InputStream input = file.getInputStream()) {
            s3().putObject(new PutObjectRequest(properties.bucket(), storageKey(key), input, metadata));
        } catch (RuntimeException e) {
            throw storageError("上传文件失败", e);
        }
        return publicUrl(key);
    }

    @Override
    public void delete(String path) throws IOException {
        try {
            s3().deleteObject(properties.bucket(), storageKey(fileKey(path)));
        } catch (RuntimeException e) {
            throw storageError("删除文件失败", e);
        }
    }

    @Override
    public FileInfoVO info(String path) throws IOException {
        String key = fileKey(path);
        try {
            ObjectMetadata metadata = s3().getObjectMetadata(
                    new GetObjectMetadataRequest(properties.bucket(), storageKey(key)));
            FileInfoVO result = new FileInfoVO();
            result.setName(fileName(key));
            result.setPath(key);
            result.setSize(metadata.getContentLength());
            result.setHash(metadata.getETag());
            result.setMimeType(metadata.getContentType());
            result.setPutTime(time(metadata.getLastModified()));
            result.setUrl(publicUrl(key));
            return result;
        } catch (RuntimeException e) {
            throw storageError("读取文件信息失败", e);
        }
    }

    @Override
    public List<FileListItemVO> list(String dir) throws IOException {
        String logicalDir = directory(dir);
        List<FileListItemVO> result = new ArrayList<>();
        try {
            String token = null;
            do {
                ListObjectsV2Result page = s3().listObjectsV2(new ListObjectsV2Request()
                        .withBucketName(properties.bucket())
                        .withPrefix(storagePrefix(logicalDir))
                        .withDelimiter("/")
                        .withContinuationToken(token));
                for (S3ObjectSummary object : page.getObjectSummaries()) {
                    String key = logicalKey(object.getKey());
                    if (!isPlaceholder(key) && isDirectChild(logicalDir, key)) {
                        result.add(listItem(object, key));
                    }
                }
                token = page.getNextContinuationToken();
            } while (token != null);
        } catch (RuntimeException e) {
            throw storageError("读取文件列表失败", e);
        }
        result.sort(Comparator.comparing(FileListItemVO::getDate).reversed());
        return result;
    }

    @Override
    public FileTreeVO tree() throws IOException {
        List<S3ObjectSummary> objects = allObjects("");
        List<FileTreeNodeVO> roots = new ArrayList<>();
        Map<String, FileTreeNodeVO> directories = new HashMap<>();
        for (S3ObjectSummary object : objects) {
            String key = logicalKey(object.getKey());
            if (key.isEmpty()) {
                continue;
            }
            addTreeObject(roots, directories, object, key);
        }
        sortTree(roots);

        FileTreeVO result = new FileTreeVO();
        result.setBasePath(publicUrl(""));
        result.setTotal(objects.size());
        result.setResult(roots);
        return result;
    }

    @Override
    public FileDirCreateVO createDir(String dir) throws IOException {
        String logicalDir = directory(dir);
        if (logicalDir.isEmpty()) {
            throw new CustomException("目录不能为空");
        }
        String placeholder = logicalDir + PLACEHOLDER;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        try {
            s3().putObject(new PutObjectRequest(properties.bucket(), storageKey(placeholder),
                    new java.io.ByteArrayInputStream(new byte[0]), metadata));
        } catch (RuntimeException e) {
            throw storageError("创建目录失败", e);
        }
        FileDirCreateVO result = new FileDirCreateVO();
        result.setDir(logicalDir);
        result.setPlaceholder(placeholder);
        result.setNode(directoryNode(logicalDir));
        return result;
    }

    @Override
    public FileDirRenameVO renameDir(String fromDir, String toDir) throws IOException {
        String from = directory(fromDir);
        String to = directory(toDir);
        if (from.isEmpty() || to.isEmpty()) {
            throw new CustomException("目录不能为空");
        }
        if (from.equals(to) || to.startsWith(from)) {
            throw new CustomException("新目录不能相同或位于原目录内");
        }
        List<S3ObjectSummary> objects = allObjects(from);
        if (objects.isEmpty()) {
            throw new CustomException("目录不存在");
        }
        try {
            for (S3ObjectSummary object : objects) {
                String oldKey = object.getKey();
                String suffix = oldKey.substring(storagePrefix(from).length());
                s3().copyObject(new CopyObjectRequest(properties.bucket(), oldKey,
                        properties.bucket(), storageKey(to + suffix)));
            }
            for (S3ObjectSummary object : objects) {
                s3().deleteObject(properties.bucket(), object.getKey());
            }
        } catch (RuntimeException e) {
            throw storageError("重命名目录失败", e);
        }
        FileDirRenameVO result = new FileDirRenameVO();
        result.setFromDir(from);
        result.setToDir(to);
        result.setMoved(objects.size());
        return result;
    }

    @Override
    public FileDirDeleteVO deleteDir(String dir) throws IOException {
        String logicalDir = directory(dir);
        if (logicalDir.isEmpty()) {
            throw new CustomException("目录不能为空");
        }
        List<S3ObjectSummary> objects = allObjects(logicalDir);
        if (objects.isEmpty()) {
            throw new CustomException("目录不存在");
        }
        for (S3ObjectSummary object : objects) {
            String key = logicalKey(object.getKey());
            if (!isPlaceholder(key) && !key.endsWith("/")) {
                throw new CustomException("目录内存在文件，请先删除文件后再删除目录");
            }
        }
        try {
            for (int index = 0; index < objects.size(); index += 1000) {
                int end = Math.min(index + 1000, objects.size());
                List<DeleteObjectsRequest.KeyVersion> keys = new ArrayList<>();
                for (S3ObjectSummary object : objects.subList(index, end)) {
                    keys.add(new DeleteObjectsRequest.KeyVersion(object.getKey()));
                }
                s3().deleteObjects(new DeleteObjectsRequest(properties.bucket()).withKeys(keys));
            }
        } catch (RuntimeException e) {
            throw storageError("删除目录失败", e);
        }
        FileDirDeleteVO result = new FileDirDeleteVO();
        result.setDir(logicalDir);
        result.setDeleted(objects.size());
        return result;
    }

    @Override
    public void verify() {
        properties.validate();
        String testKey = ".thrivex-health/" + UUID.randomUUID().toString().replace("-", "");
        String objectKey = storageKey(testKey);
        boolean created = false;
        try {
            if (!s3().doesBucketExistV2(properties.bucket())) {
                throw new CustomException("S3 bucket 不存在或无访问权限");
            }
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(0);
            s3().putObject(new PutObjectRequest(properties.bucket(), objectKey,
                    new java.io.ByteArrayInputStream(new byte[0]), metadata));
            created = true;
            s3().getObjectMetadata(new GetObjectMetadataRequest(properties.bucket(), objectKey));
            ListObjectsV2Result listing = s3().listObjectsV2(new ListObjectsV2Request()
                    .withBucketName(properties.bucket()).withPrefix(objectKey));
            boolean found = false;
            for (S3ObjectSummary object : listing.getObjectSummaries()) {
                if (objectKey.equals(object.getKey())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new CustomException("S3 bucket 缺少对象列举权限");
            }
        } catch (CustomException e) {
            throw e;
        } catch (AmazonServiceException e) {
            log.error("S3 存储连接测试失败", e);
            throw new CustomException(serviceErrorMessage(e));
        } catch (SdkClientException e) {
            log.error("S3 存储连接测试失败", e);
            throw new CustomException("S3 endpoint 无法连接，请检查地址、网络和 region 配置");
        } catch (RuntimeException e) {
            log.error("S3 存储连接测试失败", e);
            throw new CustomException("S3 存储连接测试失败，请检查服务配置");
        } finally {
            if (created) {
                try {
                    s3().deleteObject(properties.bucket(), objectKey);
                } catch (RuntimeException e) {
                    log.error("S3 存储连接测试对象清理失败", e);
                    throw new CustomException("S3 bucket 缺少删除权限，请手动清理测试对象");
                }
            }
        }
    }

    private static String serviceErrorMessage(AmazonServiceException error) {
        int status = error.getStatusCode();
        if (status == 401 || status == 403) {
            return "S3 凭据无效或缺少 bucket 访问权限";
        }
        if (status == 404) {
            return "S3 bucket 不存在或 endpoint 配置错误";
        }
        if (status == 400) {
            return "S3 endpoint 或 region 配置错误";
        }
        return "S3 服务请求失败，请检查 endpoint、凭据和 bucket 权限";
    }

    private AmazonS3 s3() {
        AmazonS3 current = client;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (client == null) {
                properties.validate();
                client = AmazonS3ClientBuilder.standard()
                        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                                properties.endpoint(), properties.region()))
                        .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                                properties.accessKey(), properties.secretKey())))
                        .withPathStyleAccessEnabled(properties.pathStyle())
                        .build();
            }
            return client;
        }
    }

    private List<S3ObjectSummary> allObjects(String dir) throws IOException {
        List<S3ObjectSummary> objects = new ArrayList<>();
        try {
            String token = null;
            do {
                ListObjectsV2Result page = s3().listObjectsV2(new ListObjectsV2Request()
                        .withBucketName(properties.bucket())
                        .withPrefix(storagePrefix(dir))
                        .withContinuationToken(token));
                objects.addAll(page.getObjectSummaries());
                token = page.getNextContinuationToken();
            } while (token != null);
            return objects;
        } catch (RuntimeException e) {
            throw storageError("读取目录失败", e);
        }
    }

    private void addTreeObject(List<FileTreeNodeVO> roots, Map<String, FileTreeNodeVO> directories,
                               S3ObjectSummary object, String key) {
        String value = key.endsWith("/") ? key.substring(0, key.length() - 1) : key;
        String[] segments = value.split("/");
        if (segments.length == 0 || segments[0].isEmpty()) {
            return;
        }
        int directoryEnd = key.endsWith("/") ? segments.length : segments.length - 1;
        FileTreeNodeVO current = null;
        StringBuilder path = new StringBuilder();
        for (int index = 0; index < directoryEnd; index++) {
            path.append(segments[index]).append('/');
            current = directory(roots, directories, current, segments[index], path.toString());
        }
        if (isPlaceholder(key) || key.endsWith("/") || current == null) {
            return;
        }
        FileTreeFileVO file = treeFile(object, key);
        current.getFiles().add(file);
        for (int index = 0; index < directoryEnd; index++) {
            String ancestor = joinSegments(segments, index + 1) + "/";
            FileTreeNodeVO node = directories.get(ancestor);
            node.setFileCount(node.getFileCount() + 1);
            node.setTotalSize(node.getTotalSize() + object.getSize());
        }
    }

    private FileTreeNodeVO directory(List<FileTreeNodeVO> roots, Map<String, FileTreeNodeVO> directories,
                                     FileTreeNodeVO parent, String name, String path) {
        FileTreeNodeVO node = directories.get(path);
        if (node == null) {
            node = new FileTreeNodeVO();
            node.setType("dir");
            node.setName(name);
            node.setPath(path);
            node.setChildren(new ArrayList<>());
            node.setFiles(new ArrayList<>());
            node.setFileCount(0);
            node.setTotalSize(0L);
            directories.put(path, node);
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return node;
    }

    private FileListItemVO listItem(S3ObjectSummary object, String key) {
        FileListItemVO result = new FileListItemVO();
        result.setBasePath(publicUrl(""));
        result.setDir(parent(key));
        result.setPath(key);
        result.setName(fileName(key));
        result.setSize(object.getSize());
        result.setType(extension(fileName(key)));
        result.setDate(time(object.getLastModified()));
        result.setUrl(publicUrl(key));
        return result;
    }

    private FileTreeFileVO treeFile(S3ObjectSummary object, String key) {
        FileTreeFileVO result = new FileTreeFileVO();
        result.setType("file");
        result.setPath(key);
        result.setBasePath(publicUrl(""));
        result.setSize(object.getSize());
        result.setName(fileName(key));
        result.setDir(parent(key));
        result.setExt(extension(fileName(key)));
        result.setDate(time(object.getLastModified()));
        result.setUrl(publicUrl(key));
        return result;
    }

    private FileTreeNodeVO directoryNode(String dir) {
        String value = dir.substring(0, dir.length() - 1);
        FileTreeNodeVO node = new FileTreeNodeVO();
        node.setType("dir");
        node.setName(fileName(value));
        node.setPath(dir);
        node.setChildren(new ArrayList<>());
        node.setFiles(new ArrayList<>());
        node.setFileCount(0);
        node.setTotalSize(0L);
        return node;
    }

    private void sortTree(List<FileTreeNodeVO> nodes) {
        for (FileTreeNodeVO node : nodes) {
            node.getChildren().sort(Comparator.comparing(FileTreeNodeVO::getName));
            node.getFiles().sort(Comparator.comparing(FileTreeFileVO::getDate).reversed());
            sortTree(node.getChildren());
        }
    }

    private String directory(String value) {
        String key = normalize(value, true);
        return key.isEmpty() ? "" : key + "/";
    }

    private String fileKey(String value) {
        String key = normalize(value, false);
        if (key.isEmpty() || key.endsWith("/")) {
            throw new CustomException("文件路径不能为空");
        }
        return key;
    }

    private String normalize(String value, boolean allowEmpty) {
        String path = value == null ? "" : value.trim().replace('\\', '/');
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("/")) {
            throw new CustomException("文件路径必须是相对 key");
        }
        String[] segments = path.split("/");
        List<String> clean = new ArrayList<>();
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }
            if (".".equals(segment) || "..".equals(segment)) {
                throw new CustomException("非法文件路径");
            }
            clean.add(segment);
        }
        String key = String.join("/", clean);
        if (!allowEmpty && key.isEmpty()) {
            throw new CustomException("文件路径不能为空");
        }
        return key;
    }

    private String storageKey(String logicalKey) {
        return properties.prefix().isEmpty() ? logicalKey : properties.prefix() + "/" + logicalKey;
    }

    private String storagePrefix(String logicalDir) {
        String prefix = properties.prefix();
        if (!prefix.isEmpty()) {
            prefix += "/";
        }
        return prefix + logicalDir;
    }

    private String logicalKey(String storageKey) {
        String prefix = properties.prefix();
        if (prefix.isEmpty()) {
            return storageKey;
        }
        String prefixWithSlash = prefix + "/";
        return storageKey.startsWith(prefixWithSlash) ? storageKey.substring(prefixWithSlash.length()) : "";
    }

    private String publicUrl(String logicalKey) {
        String objectKey = storageKey(logicalKey);
        String base = properties.publicUrl().isEmpty()
                ? properties.endpoint().replaceAll("/+$", "") + "/" + properties.bucket()
                : properties.publicUrl();
        return objectKey.isEmpty() ? base + "/" : base + "/" + objectKey;
    }

    private static String uniqueFileName(String originalFilename) {
        String name = originalFilename == null ? "" : originalFilename;
        int index = name.lastIndexOf('.');
        String extension = index >= 0 ? name.substring(index) : "";
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private static String join(String left, String right) {
        return left.isEmpty() ? right : left + right;
    }

    private static boolean isDirectChild(String dir, String key) {
        String remainder = dir.isEmpty() ? key : key.substring(dir.length());
        return !remainder.isEmpty() && !remainder.contains("/");
    }

    private static boolean isPlaceholder(String key) {
        return key.endsWith("/" + PLACEHOLDER) || PLACEHOLDER.equals(key);
    }

    private static String fileName(String key) {
        int index = key.lastIndexOf('/');
        return index < 0 ? key : key.substring(index + 1);
    }

    private static String parent(String key) {
        int index = key.lastIndexOf('/');
        return index < 0 ? "" : key.substring(0, index + 1);
    }

    private static String extension(String name) {
        int index = name.lastIndexOf('.');
        return index < 0 || index == name.length() - 1 ? "" : name.substring(index + 1).toLowerCase();
    }

    private static Long time(Date date) {
        return date == null ? null : date.getTime();
    }

    private static String joinSegments(String[] segments, int count) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < count; index++) {
            if (index > 0) {
                result.append('/');
            }
            result.append(segments[index]);
        }
        return result.toString();
    }

    private static IOException storageError(String message, RuntimeException cause) {
        return new IOException(message, cause);
    }
}
