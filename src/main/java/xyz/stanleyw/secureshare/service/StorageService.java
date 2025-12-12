package xyz.stanleyw.secureshare.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import xyz.stanleyw.secureshare.entity.StoredFile;
import xyz.stanleyw.secureshare.model.ExpirationDetails;

public interface StorageService {

    void init();

    void store(MultipartFile file);

    Resource loadAsResource(String id);

    StoredFile getMetadata(String id);

    StoredFile updateExpiration(String id, ExpirationDetails expirationDetails);

    void delete(String id);
}
