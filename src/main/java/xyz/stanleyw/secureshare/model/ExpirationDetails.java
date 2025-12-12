package xyz.stanleyw.secureshare.model;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExpirationDetails {
    @NotNull
    private int maxDownloads;

    @NotNull
    private long expiresInSeconds;
}
