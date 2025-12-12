package xyz.stanleyw.secureshare.exception;

public class StoredFileNotFoundException extends StorageException {
    public StoredFileNotFoundException(String message) {
        super(message);
    }

    public StoredFileNotFoundException(String message, Throwable cause) {
        super(message,  cause);
    }
}
