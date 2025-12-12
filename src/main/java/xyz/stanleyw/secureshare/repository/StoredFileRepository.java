package xyz.stanleyw.secureshare.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import xyz.stanleyw.secureshare.entity.StoredFile;

import java.time.Instant;
import java.util.List;

public interface StoredFileRepository extends JpaRepository<StoredFile, String> {

    @Modifying
    @Transactional
    @Query(
            value = """
                DELETE FROM stored_files
                WHERE expires_at < :now
                RETURNING id
                """,
            nativeQuery = true
    )
    List<String> deleteExpiredReturningIds(@Param("now") Instant now);
}
