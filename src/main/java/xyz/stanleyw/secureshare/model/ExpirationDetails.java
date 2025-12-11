package xyz.stanleyw.secureshare.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpirationDetails {
    @NotNull
    private int maxDownloads;

    @NotNull
    private long expiresInSeconds;
}
