package xyz.stanleyw.secureshare.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import xyz.stanleyw.secureshare.entity.StoredFile;

import java.nio.file.Path;

public interface StorageService {

    void init();

    void store(MultipartFile file);

    Path load(String id);

    Resource loadAsResource(String id);

    StoredFile getMetadata(String id);

    void delete(String id);
}
