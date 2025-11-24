package xyz.stanleyw.secureshare.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StorageService {
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    public void storeFile(MultipartFile file) {
        LOGGER.info("RECEIVED FILE: [{}]", file.getOriginalFilename());
    }
}
