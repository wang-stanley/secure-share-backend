package xyz.stanleyw.secureshare.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import xyz.stanleyw.secureshare.entity.StoredFile;

public interface StoredFileRepository extends JpaRepository<StoredFile, String> {
}
