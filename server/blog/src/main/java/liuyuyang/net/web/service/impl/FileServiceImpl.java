package liuyuyang.net.web.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import liuyuyang.net.core.storage.StorageProvider;
import liuyuyang.net.core.storage.StorageProviderSelector;
import liuyuyang.net.core.execption.CustomException;
import liuyuyang.net.core.utils.CommonUtils;
import liuyuyang.net.dto.PageDTO;
import liuyuyang.net.dto.file.FileBatchDeleteFormDTO;
import liuyuyang.net.dto.file.FileDirCreateFormDTO;
import liuyuyang.net.dto.file.FileDirDeleteFormDTO;
import liuyuyang.net.dto.file.FileDirRenameFormDTO;
import liuyuyang.net.dto.file.FileFilterDTO;
import liuyuyang.net.enums.file.FileImageExtensionEnum;
import liuyuyang.net.vo.file.FileDirCreateVO;
import liuyuyang.net.vo.file.FileDirDeleteVO;
import liuyuyang.net.vo.file.FileDirRenameVO;
import liuyuyang.net.vo.file.FileInfoVO;
import liuyuyang.net.vo.file.FileListItemVO;
import liuyuyang.net.vo.file.FileTreeVO;
import liuyuyang.net.vo.file.FileUploadVO;
import liuyuyang.net.web.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class FileServiceImpl implements FileService {

    @Resource
    private StorageProviderSelector storageProviderSelector;

    @Resource
    private CommonUtils commonUtils;

    private StorageProvider storage() {
        return storageProviderSelector.current();
    }

    @Override
    public FileUploadVO addFileData(String dir, MultipartFile[] files) throws IOException {
        if (dir == null || dir.trim().isEmpty()) {
            throw new CustomException("请指定一个目录");
        }

        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            validateImageFile(file);
            urls.add(storage().upload(dir, file));
        }

        FileUploadVO vo = new FileUploadVO();
        vo.setUrls(urls);
        return vo;
    }

    /**
     * 与控制器原逻辑一致：扩展名、MIME、解码校验。
     */
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new CustomException("文件不能为空");
        }

        Set<String> allowedExt = FileImageExtensionEnum.allowedExtensions();
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        }

        if (!allowedExt.contains(ext)) {
            throw new CustomException("仅支持上传图片类型文件（jpg、jpeg、png、webp）");
        }

        Set<String> allowedContentTypes = FileImageExtensionEnum.allowedMimeTypes();
        String contentType = file.getContentType();
        if (contentType == null || !allowedContentTypes.contains(contentType.toLowerCase())) {
            throw new CustomException("文件类型不合法，仅支持上传图片类型文件");
        }

        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image == null) {
            throw new CustomException("文件内容不是有效的图片");
        }
    }

    @Override
    public void delFileData(String filePath) {
        try { storage().delete(filePath); } catch (IOException e) { throw new CustomException("删除文件失败"); }
    }

    @Override
    public void batchDelFileData(FileBatchDeleteFormDTO dto) {
        List<String> pathList = dto.getPaths();
        if (pathList == null || pathList.isEmpty()) {
            return;
        }
        for (String url : pathList) {
            try { storage().delete(url); } catch (IOException e) { throw new CustomException("删除文件失败"); }
        }
    }

    @Override
    public FileInfoVO getFileData(String filePath) {
        try { return storage().info(filePath); } catch (IOException e) { throw new CustomException("读取文件信息失败"); }
    }

    @Override
    public Page<FileListItemVO> getFileList(FileFilterDTO fileFilterDTO) {
        if (fileFilterDTO.getDir() == null || fileFilterDTO.getDir().trim().isEmpty()) {
            throw new CustomException("请指定一个目录");
        }

        List<FileListItemVO> all;
        try { all = storage().list(fileFilterDTO.getDir()); }
        catch (IOException e) { throw new CustomException("读取文件列表失败"); }

        if (fileFilterDTO.getPageNum() == null || fileFilterDTO.getPageSize() == null) {
            Page<FileListItemVO> result = new Page<>(1, all.size());
            result.setRecords(new ArrayList<>(all));
            result.setTotal(all.size());
            return result;
        }

        PageDTO pageDTO = new PageDTO();
        pageDTO.setPageNum(Math.max(1, fileFilterDTO.getPageNum()));
        pageDTO.setPageSize(Math.max(1, fileFilterDTO.getPageSize()));
        return commonUtils.getPageData(pageDTO, all);
    }

    @Override
    public FileTreeVO getFileTreeData() {
        try { return storage().tree(); } catch (IOException e) { throw new CustomException("读取目录树失败"); }
    }

    @Override
    public void testStorageConnection() {
        try {
            storage().verify();
        } catch (IOException e) {
            throw new CustomException("存储连接测试失败：" + e.getMessage());
        }
    }

    @Override
    public FileDirCreateVO addFileDirData(FileDirCreateFormDTO dto) throws IOException {
        String dir = dto.getDir();
        if (dir == null || dir.trim().isEmpty()) {
            throw new CustomException("请指定一个目录");
        }
        return storage().createDir(dir);
    }

    @Override
    public FileDirRenameVO renameFileDirData(FileDirRenameFormDTO dto) {
        String fromDir = dto.getFromDir();
        String toDir = dto.getToDir();
        if (fromDir == null || fromDir.trim().isEmpty() || toDir == null || toDir.trim().isEmpty()) {
            throw new CustomException("请指定原目录和新目录");
        }
        try { return storage().renameDir(fromDir, toDir); }
        catch (IOException e) { throw new CustomException("重命名目录失败"); }
    }

    @Override
    public FileDirDeleteVO delFileDirData(FileDirDeleteFormDTO dto) {
        String dir = dto.getDir();
        if (dir == null || dir.trim().isEmpty()) {
            throw new CustomException("请指定一个目录");
        }
        try { return storage().deleteDir(dir); }
        catch (IOException e) { throw new CustomException("删除目录失败"); }
    }

}
