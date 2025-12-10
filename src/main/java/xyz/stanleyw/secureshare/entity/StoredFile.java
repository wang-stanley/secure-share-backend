package xyz.stanleyw.secureshare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stored_files")
public class StoredFile {

    @Id
    private String id;

    @Column(nullable = false)
    private String storagePath;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private int downloadsRemaining;
}
