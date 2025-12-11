package xyz.stanleyw.secureshare.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import xyz.stanleyw.secureshare.entity.StoredFile;
import xyz.stanleyw.secureshare.model.ExpirationDetails;
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

    @GetMapping("/{fileId}")
    public ResponseEntity<?> getStoredFileMetadata(@PathVariable String fileId) {
        StoredFile storedFile = storageService.getMetadata(fileId);

        return ResponseEntity.ok(storedFile);
    }

    @PutMapping("/{fileId}")
    public ResponseEntity<?> updateExpirationDetails(@PathVariable String fileId,
                                                     @RequestBody ExpirationDetails expirationDetails) {
        StoredFile storedFile = storageService.updateExpiration(fileId, expirationDetails);
        return ResponseEntity.ok(storedFile);
    }
}
