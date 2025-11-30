package xyz.stanleyw.secureshare.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.stanleyw.secureshare.exception.StorageException;
import xyz.stanleyw.secureshare.properties.StorageProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileSystemStorageService implements StorageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemStorageService.class);
    private final Path rootLocation;

    public Path getRootLocation() {
        return this.rootLocation;
    }

    @Autowired
    public FileSystemStorageService(StorageProperties storageProperties) {
        if (storageProperties.getLocation().trim().isEmpty()) {
            throw new StorageException("File upload location can not be empty.");
        }

        this.rootLocation = Paths.get(storageProperties.getLocation());
        LOGGER.info("Root Location: {}", rootLocation);
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
