package xyz.stanleyw.secureshare.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import xyz.stanleyw.secureshare.service.StorageService;

import java.util.Map;
import java.util.UUID;

@RestController
public class FileController {

    private final StorageService storageService;

    public FileController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        storageService.store(file);

        return ResponseEntity.ok(Map.of("id", UUID.randomUUID()));
    }
}
