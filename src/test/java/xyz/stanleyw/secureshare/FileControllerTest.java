package xyz.stanleyw.secureshare;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import xyz.stanleyw.secureshare.controller.FileController;
import xyz.stanleyw.secureshare.service.StorageService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
public class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StorageService storageService;

    @Test
    void uploadFile_returns200() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "test.txt",
                        MediaType.TEXT_PLAIN_VALUE, "sample".getBytes());

        mockMvc.perform(multipart("/upload")
                .file(file))
                .andExpect(status().isOk());
    }
}
