package liuyuyang.net.core.storage;

import liuyuyang.net.vo.file.FileDirCreateVO;
import liuyuyang.net.vo.file.FileDirDeleteVO;
import liuyuyang.net.vo.file.FileDirRenameVO;
import liuyuyang.net.vo.file.FileInfoVO;
import liuyuyang.net.vo.file.FileListItemVO;
import liuyuyang.net.vo.file.FileTreeVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/** 不依赖具体云厂商的文件存储契约。 */
public interface StorageProvider {
    String name();
    String upload(String dir, MultipartFile file) throws IOException;
    void delete(String path) throws IOException;
    FileInfoVO info(String path) throws IOException;
    List<FileListItemVO> list(String dir) throws IOException;
    FileTreeVO tree() throws IOException;
    FileDirCreateVO createDir(String dir) throws IOException;
    FileDirRenameVO renameDir(String fromDir, String toDir) throws IOException;
    FileDirDeleteVO deleteDir(String dir) throws IOException;
    void verify() throws IOException;
}
