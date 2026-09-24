package liuyuyang.net.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import liuyuyang.net.dto.file.FileBatchDeleteFormDTO;
import liuyuyang.net.dto.file.FileDirCreateFormDTO;
import liuyuyang.net.dto.file.FileDirDeleteFormDTO;
import liuyuyang.net.dto.file.FileDirRenameFormDTO;
import liuyuyang.net.dto.file.FileFilterDTO;
import liuyuyang.net.vo.file.FileDirCreateVO;
import liuyuyang.net.vo.file.FileDirDeleteVO;
import liuyuyang.net.vo.file.FileDirRenameVO;
import liuyuyang.net.vo.file.FileInfoVO;
import liuyuyang.net.vo.file.FileListItemVO;
import liuyuyang.net.vo.file.FileTreeVO;
import liuyuyang.net.vo.file.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    FileUploadVO addFileData(String dir, MultipartFile[] files) throws IOException;

    void delFileData(String filePath);

    void batchDelFileData(FileBatchDeleteFormDTO dto);

    FileInfoVO getFileData(String filePath);

    Page<FileListItemVO> getFileList(FileFilterDTO fileFilterDTO);

    FileTreeVO getFileTreeData();

    void testStorageConnection();

    FileDirCreateVO addFileDirData(FileDirCreateFormDTO dto) throws IOException;

    FileDirRenameVO renameFileDirData(FileDirRenameFormDTO dto);

    FileDirDeleteVO delFileDirData(FileDirDeleteFormDTO dto);
}
