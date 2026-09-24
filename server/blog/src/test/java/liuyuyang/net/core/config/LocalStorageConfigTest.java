package liuyuyang.net.core.config;

import liuyuyang.net.core.execption.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void managesFilesAndDirectoriesUsingRelativeKeys() throws Exception {
        LocalStorageConfig storage = new LocalStorageConfig(tempDir.toString(), "https://api.example.com");
        storage.createDir("article");

        String url = storage.upload("article", new MockMultipartFile("file", "cover.png", "image/png", new byte[]{1, 2, 3}));
        String key = url.substring(url.indexOf("/static/upload/") + "/static/upload/".length());

        assertTrue(url.startsWith("https://api.example.com/static/upload/article/"));
        assertEquals(1, storage.list("article").size());
        assertEquals(key, storage.info(url).getPath());
        assertEquals(1, storage.tree().getTotal());

        storage.delete(url);
        assertEquals(0, storage.list("article").size());
        storage.deleteDir("article");
    }

    @Test
    void rejectsPathTraversal() {
        LocalStorageConfig storage = new LocalStorageConfig(tempDir.toString(), "");
        assertThrows(CustomException.class, () -> storage.createDir("../outside"));
        assertThrows(CustomException.class, () -> storage.info("/static/upload/../secret.png"));
    }

    @Test
    void refusesToDeleteDirectoryContainingFiles() throws Exception {
        LocalStorageConfig storage = new LocalStorageConfig(tempDir.toString(), "");
        storage.createDir("article");
        storage.upload("article", new MockMultipartFile("file", "cover.png", "image/png", new byte[]{1}));

        assertThrows(CustomException.class, () -> storage.deleteDir("article"));
        assertTrue(Files.exists(tempDir.resolve("article")));
    }
}
