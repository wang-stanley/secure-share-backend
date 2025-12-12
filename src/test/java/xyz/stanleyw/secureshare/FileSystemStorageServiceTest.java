package xyz.stanleyw.secureshare;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import xyz.stanleyw.secureshare.entity.StoredFile;
import xyz.stanleyw.secureshare.exception.StorageException;
import xyz.stanleyw.secureshare.config.StorageProperties;
import xyz.stanleyw.secureshare.exception.StoredFileNotFoundException;
import xyz.stanleyw.secureshare.model.ExpirationDetails;
import xyz.stanleyw.secureshare.repository.StoredFileRepository;
import xyz.stanleyw.secureshare.service.FileSystemStorageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

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
        MultipartFile multipartFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "hello world".getBytes()
        );

        storageService.store(multipartFile);

        Path expectedPath = tempDir.resolve("test.txt").toAbsolutePath();

        assertTrue(Files.exists(expectedPath));
        assertEquals("hello world", Files.readString(expectedPath));

        ArgumentCaptor<StoredFile> captor = ArgumentCaptor.forClass(StoredFile.class);
        verify(storedFileRepository).save(captor.capture());

        StoredFile saved = captor.getValue();

        assertEquals(expectedPath.toString(), saved.getStoragePath());
        assertEquals(multipartFile.getSize(), saved.getSizeBytes());
    }

    @Test
    void getMetadata_whenFileIdIsInvalid_shouldThrowStoredFileNotFoundException() {
        String id = "id";
        when(storedFileRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(StoredFileNotFoundException.class,
                () -> storageService.getMetadata(id));

        verify(storedFileRepository).findById(id);
    }

    @Test
    void getMetadata_whenFileIsValid_shouldReturnStoredFile() {
        String id = "id";
        StoredFile storedFile = new StoredFile();
        storedFile.setId(id);

        when(storedFileRepository.findById(id)).thenReturn(Optional.of(storedFile));

        StoredFile result = storageService.getMetadata(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(storedFileRepository).findById(id);
    }

    @Test
    void updateExpiration_whenFileIsInvalid_shouldThrowStoredFileNotFoundException() {
        String id = "id";
        ExpirationDetails expirationDetails = new ExpirationDetails();

        when(storedFileRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(StoredFileNotFoundException.class,
                () -> storageService.updateExpiration(id, expirationDetails));

        verify(storedFileRepository).findById(id);
    }

    @Test
    void updateExpiration_whenFileIsValid_shouldUpdateAndReturnStoredFile() {
        String id = "id";
        ExpirationDetails expirationDetails = new ExpirationDetails(67, 241200);

        Instant createdAt = Instant.parse("2025-12-11T00:00:00Z");

        StoredFile storedFile = new StoredFile();
        storedFile.setId(id);
        storedFile.setMaxDownloads(100);
        storedFile.setDownloadsRemaining(99);
        storedFile.setCreatedAt(createdAt);

        when(storedFileRepository.findById(id)).thenReturn(Optional.of(storedFile));

        when(storedFileRepository.save(any(StoredFile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Instant expectedExpiresAt = storedFile.getCreatedAt()
                .plus(expirationDetails.getExpiresInSeconds(), ChronoUnit.SECONDS);

        int downloadsUsed = storedFile.getMaxDownloads() - storedFile.getDownloadsRemaining();
        int expectedDownloadsRemaining = expirationDetails.getMaxDownloads() - downloadsUsed;

        StoredFile result = storageService.updateExpiration(id, expirationDetails);

        assertEquals(expectedExpiresAt, result.getExpiresAt());
        assertEquals(expirationDetails.getMaxDownloads(), result.getMaxDownloads());
        assertEquals(expectedDownloadsRemaining, result.getDownloadsRemaining());

        verify(storedFileRepository).findById(id);
        verify(storedFileRepository).save(storedFile);
    }
}
