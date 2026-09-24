package liuyuyang.net.core.config;

import liuyuyang.net.core.execption.CustomException;
import liuyuyang.net.core.storage.StorageProvider;
import liuyuyang.net.vo.file.FileDirCreateVO;
import liuyuyang.net.vo.file.FileDirDeleteVO;
import liuyuyang.net.vo.file.FileDirRenameVO;
import liuyuyang.net.vo.file.FileInfoVO;
import liuyuyang.net.vo.file.FileListItemVO;
import liuyuyang.net.vo.file.FileTreeFileVO;
import liuyuyang.net.vo.file.FileTreeNodeVO;
import liuyuyang.net.vo.file.FileTreeVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** 本地文件存储。所有 path 都是上传目录内的相对路径，禁止越界访问。 */
@Service
public class LocalStorageConfig implements StorageProvider {
    private static final String PLACEHOLDER = ".keep";
    private final Path root;
    private final String publicUrl;

    public LocalStorageConfig(@Value("${file.dir:./upload/}") String dir,
                              @Value("${file.public-url:}") String publicUrl) {
        this.root = Paths.get(dir).toAbsolutePath().normalize();
        this.publicUrl = publicUrl == null ? "" : publicUrl.replaceAll("/+$", "");
    }

    @Override public String name() { return "local"; }

    @Override public String upload(String dir, MultipartFile file) throws IOException {
        Path folder = resolveDir(dir);
        Files.createDirectories(folder);
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = name.lastIndexOf('.') >= 0 ? name.substring(name.lastIndexOf('.')) : "";
        Path target = folder.resolve(UUID.randomUUID().toString().replace("-", "") + ext);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return publicUrl(relative(target));
    }

    @Override public void delete(String path) throws IOException { Files.deleteIfExists(resolve(path)); }

    @Override public FileInfoVO info(String path) throws IOException {
        Path file = resolve(path);
        if (!Files.isRegularFile(file)) throw new CustomException("文件不存在");
        FileInfoVO out = new FileInfoVO();
        out.setName(file.getFileName().toString()); out.setPath(relative(file)); out.setSize(Files.size(file));
        out.setMimeType(Files.probeContentType(file)); out.setPutTime(Files.getLastModifiedTime(file).toMillis());
        out.setUrl(publicUrl(relative(file))); return out;
    }

    @Override public List<FileListItemVO> list(String dir) throws IOException {
        Path folder = resolveDir(dir);
        if (!Files.exists(folder)) return new ArrayList<>();
        try (Stream<Path> paths = Files.list(folder)) {
            return paths.filter(Files::isRegularFile).filter(p -> !PLACEHOLDER.equals(p.getFileName().toString()))
                    .map(this::toListItem).sorted(Comparator.comparing(FileListItemVO::getDate).reversed()).collect(Collectors.toList());
        }
    }

    @Override public FileTreeVO tree() throws IOException {
        Files.createDirectories(root);
        FileTreeVO out = new FileTreeVO(); out.setBasePath(publicUrl("")); out.setResult(nodes(root));
        try (Stream<Path> paths = Files.walk(root)) { out.setTotal((int) paths.filter(Files::isRegularFile).filter(p -> !PLACEHOLDER.equals(p.getFileName().toString())).count()); }
        return out;
    }

    @Override public FileDirCreateVO createDir(String dir) throws IOException {
        Path folder = resolveDir(dir); Files.createDirectories(folder);
        Path marker = folder.resolve(PLACEHOLDER); if (!Files.exists(marker)) Files.createFile(marker);
        FileDirCreateVO out = new FileDirCreateVO(); out.setDir(relativeDir(folder)); out.setPlaceholder(relative(marker)); out.setNode(node(folder)); return out;
    }

    @Override public FileDirRenameVO renameDir(String from, String to) throws IOException {
        Path source = resolveDir(from), target = resolveDir(to);
        if (!Files.exists(source)) throw new CustomException("目录不存在"); if (source.equals(target)) throw new CustomException("新旧目录不能相同");
        Files.createDirectories(target.getParent()); Files.move(source, target);
        FileDirRenameVO out = new FileDirRenameVO(); out.setFromDir(relativeDir(source)); out.setToDir(relativeDir(target)); out.setMoved(0); return out;
    }

    @Override public FileDirDeleteVO deleteDir(String dir) throws IOException {
        Path folder = resolveDir(dir);
        if (!Files.exists(folder)) throw new CustomException("目录不存在");
        try (Stream<Path> paths = Files.walk(folder)) { if (paths.anyMatch(p -> Files.isRegularFile(p) && !PLACEHOLDER.equals(p.getFileName().toString()))) throw new CustomException("目录内存在文件，请先删除文件后再删除目录"); }
        try (Stream<Path> paths = Files.walk(folder)) { paths.sorted(Comparator.reverseOrder()).forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException e) { throw new CustomException("删除目录失败"); } }); }
        FileDirDeleteVO out = new FileDirDeleteVO(); out.setDir(relativeDir(folder)); out.setDeleted(1); return out;
    }

    private List<FileTreeNodeVO> nodes(Path folder) throws IOException { try (Stream<Path> paths = Files.list(folder)) { return paths.filter(Files::isDirectory).map(p -> { try { return node(p); } catch (IOException e) { throw new CustomException("读取目录失败"); } }).sorted(Comparator.comparing(FileTreeNodeVO::getName)).collect(Collectors.toList()); } }
    private FileTreeNodeVO node(Path folder) throws IOException { FileTreeNodeVO n = new FileTreeNodeVO(); n.setType("dir"); n.setName(folder.getFileName().toString()); n.setPath(relativeDir(folder)); n.setChildren(nodes(folder)); n.setFiles(list(relativeDir(folder)).stream().map(this::toTreeFile).collect(Collectors.toList())); n.setFileCount(n.getFiles().size() + n.getChildren().stream().mapToInt(FileTreeNodeVO::getFileCount).sum()); n.setTotalSize(n.getFiles().stream().mapToLong(FileTreeFileVO::getSize).sum() + n.getChildren().stream().mapToLong(FileTreeNodeVO::getTotalSize).sum()); return n; }
    private FileListItemVO toListItem(Path p) { try { FileListItemVO f = new FileListItemVO(); String path = relative(p), name = p.getFileName().toString(); int dot = name.lastIndexOf('.'); f.setPath(path); f.setName(name); f.setDir(relativeDir(p.getParent())); f.setSize(Files.size(p)); f.setDate(Files.getLastModifiedTime(p).toMillis()); f.setType(dot < 0 ? "" : name.substring(dot + 1).toLowerCase()); f.setBasePath(publicUrl("")); f.setUrl(publicUrl(path)); return f; } catch (IOException e) { throw new CustomException("读取文件失败"); } }
    private FileTreeFileVO toTreeFile(FileListItemVO f) { FileTreeFileVO out = new FileTreeFileVO(); out.setType("file"); out.setPath(f.getPath()); out.setName(f.getName()); out.setDir(f.getDir()); out.setSize(f.getSize()); out.setDate(f.getDate()); out.setExt(f.getType()); out.setBasePath(f.getBasePath()); out.setUrl(f.getUrl()); return out; }
    private Path resolveDir(String dir) { return resolve(dir == null ? "" : dir); }
    private Path resolve(String value) { Path path = root.resolve(key(value)).normalize(); if (!path.startsWith(root)) throw new CustomException("非法文件路径"); return path; }
    private String relative(Path path) { return root.relativize(path).toString().replace('\\', '/'); }
    private String relativeDir(Path path) { String value = relative(path); return value.isEmpty() ? "" : value + "/"; }
    private String key(String value) {
        String path = value == null ? "" : value.trim();
        if (path.startsWith("http://") || path.startsWith("https://")) {
            try { path = new URI(path).getPath(); } catch (URISyntaxException e) { throw new CustomException("文件 URL 格式不正确"); }
        }
        path = path.replace('\\', '/').replaceFirst("^/+", "");
        String prefix = "static/upload/";
        if (path.startsWith(prefix)) path = path.substring(prefix.length());
        if (path.contains("..")) throw new CustomException("非法文件路径");
        return path;
    }
    private String publicUrl(String path) { return (publicUrl.isEmpty() ? "" : publicUrl) + "/static/upload/" + path; }
    @Override public void verify() throws IOException { Files.createDirectories(root); if (!Files.isWritable(root)) throw new IOException("本地上传目录不可写：" + root); }
}
