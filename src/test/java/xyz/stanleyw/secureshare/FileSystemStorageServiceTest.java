package xyz.stanleyw.secureshare;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import xyz.stanleyw.secureshare.exception.StorageException;
import xyz.stanleyw.secureshare.config.StorageProperties;
import xyz.stanleyw.secureshare.repository.StoredFileRepository;
import xyz.stanleyw.secureshare.service.FileSystemStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileSystemStorageServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    StoredFileRepository storedFileRepository;

    private FileSystemStorageService storageService;

    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemStorageServiceTest.class);

    @BeforeEach
    void setUp() {
        StorageProperties storageProperties = new StorageProperties();
        storageProperties.setLocation(tempDir.toString());
        storageService = new FileSystemStorageService(storageProperties, storedFileRepository);
    }

    @Test
    void constructor_whenLocationIsEmpty_shouldThrowStorageException() {
        StorageProperties emptyLocProps = new StorageProperties();
        emptyLocProps.setLocation("");

        StorageException ex = assertThrows(StorageException.class,
                () -> new FileSystemStorageService(emptyLocProps, storedFileRepository));

        LOGGER.info(ex.getMessage());
    }

    @Test
    void constructor_whenLocationIsWhitespace_shouldThrowStorageException() {
        StorageProperties whitespaceLocProps = new StorageProperties();
        whitespaceLocProps.setLocation("    ");

        StorageException ex = assertThrows(StorageException.class,
                () -> new FileSystemStorageService(whitespaceLocProps, storedFileRepository));

        LOGGER.info(ex.getMessage());
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
        storageService = new FileSystemStorageService(props, storedFileRepository);

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
        storageService = new FileSystemStorageService(props, storedFileRepository);

        StorageException ex = assertThrows(StorageException.class,
                () -> storageService.init());

        LOGGER.info(ex.getMessage());
    }

    @Test
    void store_whenFileIsEmpty_shouldThrowStorageException() {
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(mockMultipartFile.isEmpty()).thenReturn(true);

        StorageException ex = assertThrows(StorageException.class,
                () -> storageService.store(mockMultipartFile));

        LOGGER.info(ex.getMessage());
    }

    @Test
    void store_whenFilenameIsNull_shouldThrowStorageException() {
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getOriginalFilename()).thenReturn(null);

        StorageException ex = assertThrows(StorageException.class,
                () -> storageService.store(mockMultipartFile));

        LOGGER.info(ex.getMessage());
    }

    @Test
    void store_whenFilenameIsMalicious_shouldThrowStorageException() {
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(mockMultipartFile.getOriginalFilename()).thenReturn("../testMaliciousDir");

        StorageException ex = assertThrows(StorageException.class,
                () -> storageService.store(mockMultipartFile));

        LOGGER.info(ex.getMessage());
    }

    @Test
    void store_whenFileTransferToFails_shouldThrowIOException() throws IOException {
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getOriginalFilename()).thenReturn("test.txt");

        doThrow(new IOException("Mocked IOException TransferTo Failure"))
                .when(mockMultipartFile).transferTo(any(Path.class));

        StorageException ex = assertThrows(StorageException.class,
                () -> storageService.store(mockMultipartFile));

        LOGGER.info(ex.getMessage());
    }

    @Test
    void store_whenFileIsValid_shouldStoreSuccessfully() throws IOException {
        MultipartFile mockMultipartFile = mock(MultipartFile.class);
        when(mockMultipartFile.isEmpty()).thenReturn(false);
        when(mockMultipartFile.getOriginalFilename()).thenReturn("test.txt");

        doNothing().when(mockMultipartFile).transferTo(any(Path.class));

        assertDoesNotThrow(() -> storageService.store(mockMultipartFile));

        Path expectedPath = storageService.getRootLocation().resolve("test.txt").normalize().toAbsolutePath();

        verify(mockMultipartFile).transferTo(expectedPath);
    }
}
