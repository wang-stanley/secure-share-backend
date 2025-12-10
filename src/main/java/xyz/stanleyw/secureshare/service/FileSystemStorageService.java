package xyz.stanleyw.secureshare.service;

import com.soundicly.jnanoidenhanced.jnanoid.NanoIdUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.stanleyw.secureshare.entity.StoredFile;
import xyz.stanleyw.secureshare.exception.StorageException;
import xyz.stanleyw.secureshare.config.StorageProperties;
import xyz.stanleyw.secureshare.repository.StoredFileRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Getter
@Service
public class FileSystemStorageService implements StorageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemStorageService.class);
    private final Path rootLocation;

    private final StoredFileRepository storedFileRepository;

    private static final String id_alphabet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    @Autowired
    public FileSystemStorageService(StorageProperties storageProperties, StoredFileRepository storedFileRepository) {
        if (storageProperties.getLocation().trim().isEmpty()) {
            throw new StorageException("File upload location can not be empty.");
        }

        this.rootLocation = Paths.get(storageProperties.getLocation());
        LOGGER.info("Root Location: {}", rootLocation);
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

            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new StorageException("Cannot store file outside current directory");
            }

            file.transferTo(destinationFile);

            Instant oneDayFromNow = Instant.now().plus(1, ChronoUnit.DAYS);

            StoredFile storedFile = new StoredFile(
                    NanoIdUtils.randomNanoId(id_alphabet, 7),
                    destinationFile.toString(),
                    file.getSize(),
                    oneDayFromNow,
                    Instant.now(),
                    100
            );

            storedFileRepository.save(storedFile);
        } catch (IOException e) {
            LOGGER.error("Failed to store file! Error:{}", e.getMessage());
            throw new StorageException("Failed to store file!", e);
        }

        LOGGER.info("RECEIVED FILE: [{}]", file.getOriginalFilename());
    }

    @Override
    public Path load(String filename) {
        return null;
    }

    @Override
    public Resource loadAsResource(String filename) {
        return null;
    }
}
