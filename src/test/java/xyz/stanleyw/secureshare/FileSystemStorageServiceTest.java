package xyz.stanleyw.secureshare;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import xyz.stanleyw.secureshare.exception.StorageException;
import xyz.stanleyw.secureshare.properties.StorageProperties;
import xyz.stanleyw.secureshare.service.FileSystemStorageService;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FileSystemStorageServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private MultipartFile mockMultipartFile;

    private FileSystemStorageService storageService;

    @BeforeEach
    void setUp() {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setLocation(tempDir.toString());
        storageService = new FileSystemStorageService(storageProperties);
    }

    @Test
    void constructor_whenLocationIsEmpty_shouldThrowStorageException() {
        StorageProperties emptyLocProps = new StorageProperties();
        emptyLocProps.setLocation("");

        assertThrows(StorageException.class,
                () -> new FileSystemStorageService(emptyLocProps));
    }

    @Test
    void constructor_whenLocationIsWhitespace_shouldThrowStorageException() {
        StorageProperties whitespaceLocProps = new StorageProperties();
        whitespaceLocProps.setLocation("    ");

        assertThrows(StorageException.class,
                () -> new FileSystemStorageService(whitespaceLocProps));
    }

    @Test
    void constructor_whenLocationIsValid_shouldUseThatLocation() {
        assertEquals(tempDir, storageService.getRootLocation());
    }

    @Test
    void init_whenRootDirectoryNonexistent_shouldCreateThatLocation() {
        Path nonExistentPath = tempDir.resolve("new-directory");
        assertFalse(Files.exists(nonExistentPath));

        StorageProperties props = new StorageProperties();
        props.setLocation(nonExistentPath.toString());
        storageService = new FileSystemStorageService(props);

        storageService.init();

        assertTrue(Files.exists(nonExistentPath));
        assertTrue(Files.isDirectory(nonExistentPath));
    }

    @Test
    void init_whenRootDirectoryExists_shouldSucceed() {
        storageService.init();

        assertTrue(Files.exists(tempDir));
        assertTrue(Files.isDirectory(tempDir));
    }

    @Test
    void init_whenDirectoryCannotBeCreated_shouldThrowStorageException() {
        assertTrue(tempDir.toFile().setReadOnly());
        Path invalidRootLocation = tempDir.resolve("subdir");

        StorageProperties props = new StorageProperties();
        props.setLocation(invalidRootLocation.toString());
        storageService = new FileSystemStorageService(props);

        assertThrows(StorageException.class,
                () -> storageService.init());
    }
}
