package xyz.stanleyw.secureshare;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import xyz.stanleyw.secureshare.controller.FileController;
import xyz.stanleyw.secureshare.entity.StoredFile;
import xyz.stanleyw.secureshare.exception.StorageException;
import xyz.stanleyw.secureshare.exception.StoredFileNotFoundException;
import xyz.stanleyw.secureshare.model.ExpirationDetails;
import xyz.stanleyw.secureshare.service.StorageService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@ActiveProfiles("test")
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StorageService storageService;

    @Test
    void uploadFile_whenFileIsValid_shouldReturn200() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "test.txt",
                        MediaType.TEXT_PLAIN_VALUE, "sample".getBytes());

        mockMvc.perform(multipart("/upload")
                .file(file))
                .andExpect(status().isOk());
    }

    @Test
    void uploadFile_whenServiceError_shouldReturn500() throws Exception {
        doThrow(new StorageException("Exception!"))
                .when(storageService)
                .store(any(MockMultipartFile.class));

        MockMultipartFile file =
                new MockMultipartFile("file", "test.txt",
                        MediaType.TEXT_PLAIN_VALUE, "sample".getBytes());

        mockMvc.perform(multipart("/upload")
                .file(file))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getStoredFileMetadata_whenValidId_shouldReturnStoredFile() throws Exception {
        String id = "id";
        StoredFile storedFile = new StoredFile();
        storedFile.setId(id);

        when(storageService.getMetadata(id)).thenReturn(storedFile);

        mockMvc.perform(get("/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id"));
    }

    @Test
    void getStoredFileMetadata_whenInvalidId_shouldReturn404() throws Exception {
        String id = "id";

        doThrow(new StoredFileNotFoundException("Exception!"))
                .when(storageService)
                .getMetadata(id);

        mockMvc.perform(get("/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateExpirationDetails_whenValidId_shouldReturnStoredFile() throws Exception {
        String id = "id";
        ExpirationDetails expirationDetails = new ExpirationDetails();

        StoredFile storedFile = new StoredFile();
        storedFile.setId(id);

        when(storageService.updateExpiration(eq(id), any(ExpirationDetails.class))).thenReturn(storedFile);

        mockMvc.perform(
                put("/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expirationDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("id"));

    }

    @Test
    void updateExpirationDetails_whenInvalidId_shouldReturn404() throws Exception {
        String id = "id";
        ExpirationDetails expirationDetails = new ExpirationDetails();

        doThrow(new StoredFileNotFoundException("Exception!"))
                .when(storageService)
                .updateExpiration(eq(id), any(ExpirationDetails.class));

        mockMvc.perform(
                put("/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expirationDetails)))
                .andExpect(status().isNotFound());
    }
}
