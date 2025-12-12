package xyz.stanleyw.secureshare.service;

import com.soundicly.jnanoidenhanced.jnanoid.NanoIdUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.stanleyw.secureshare.config.StorageProperties;
import xyz.stanleyw.secureshare.entity.StoredFile;
import xyz.stanleyw.secureshare.exception.StorageException;
import xyz.stanleyw.secureshare.exception.StoredFileNotFoundException;
import xyz.stanleyw.secureshare.model.ExpirationDetails;
import xyz.stanleyw.secureshare.repository.StoredFileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Getter
@Service
@Slf4j
public class FileSystemStorageService implements StorageService {
    private final Path rootLocation;

    private final StoredFileRepository storedFileRepository;

    // Custom alphabet for generating Nano IDs for uploaded files
    private static final String id_alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    @Autowired
    public FileSystemStorageService(StorageProperties storageProperties, StoredFileRepository storedFileRepository) {
        if (storageProperties.getLocation().trim().isEmpty()) {
            throw new StorageException("File upload location can not be empty.");
        }

        this.rootLocation = Paths.get(storageProperties.getLocation());
        log.info("Root Location: {}", rootLocation);
        this.storedFileRepository = storedFileRepository;
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage!", e);
        }
    }

    @Override
    public void store(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }

            if (file.getOriginalFilename() == null) {
                throw new StorageException("Failed to store file with null file name");
            }

            Path destinationFile = this.rootLocation
                    .resolve(Paths.get(file.getOriginalFilename()))
                    .normalize()
                    .toAbsolutePath();

            // Safety check to make sure that there's no malicious file path
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new StorageException("Cannot store file outside current directory");
            }

            file.transferTo(destinationFile);

            // Create default expiration instant 1 day from now
            Instant oneDayFromNow = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
            Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

            StoredFile storedFile = new StoredFile(
                    NanoIdUtils.randomNanoId(id_alphabet, 7),
                    destinationFile.toString(),
                    file.getSize(),
                    oneDayFromNow,
                    now,
                    100,
                    100
            );

            storedFileRepository.save(storedFile);
        } catch (IOException e) {
            log.error("Failed to store file! Error:{}", e.getMessage());
            throw new StorageException("Failed to store file!", e);
        }

        log.info("RECEIVED FILE: [{}]", file.getOriginalFilename());
    }

    @Override
    public Resource loadAsResource(String filename) {
        return null;
    }

    @Override
    public StoredFile getMetadata(String id) {
        log.info("Fetching metadata for file [{}]", id);
        StoredFile metadata = storedFileRepository.findById(id).orElse(null);

        if (metadata == null) {
            log.error("Metadata lookup failed for file [{}]", id);
            throw new StoredFileNotFoundException("Failed to get metadata for file: " + id);
        }

        return metadata;
    }

    @Override
    public StoredFile updateExpiration(String id, ExpirationDetails expirationDetails) {
        log.info("Updating expiration metadata for file [{}]", id);
        StoredFile storedFile = storedFileRepository.findById(id).orElse(null);

        if (storedFile == null) {
            log.error("Could not find file [{}]", id);
            throw new StoredFileNotFoundException("Failed to fetch file: " + id);
        }

        // Requested expiration updates
        long timeDetail = expirationDetails.getExpiresInSeconds();
        int downloadDetail = expirationDetails.getMaxDownloads();

        // New expiresAt = createdAt + timeDetail (in seconds)
        Instant updatedExpiresAtTime = storedFile.getCreatedAt().plus(timeDetail, ChronoUnit.SECONDS);

        // Remove however many downloads have already been used from the updated max downloads value
        int usedDownloads = storedFile.getMaxDownloads() - storedFile.getDownloadsRemaining();
        int updatedDownloadsRemaining = downloadDetail - usedDownloads;

        storedFile.setMaxDownloads(downloadDetail);
        storedFile.setDownloadsRemaining(updatedDownloadsRemaining);
        storedFile.setExpiresAt(updatedExpiresAtTime);

        return storedFileRepository.save(storedFile);
    }

    @Override
    public void delete(String id) {

    }
}
