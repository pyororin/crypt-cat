package pyororin.cryptcat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CryptCatApplication.HelloworldController.class)
class CryptCatApplicationTests {

    @Autowired
    MockMvc mockMvc;

    @Test
    void test_response() throws Exception {
        String response = this.mockMvc
                .perform(get("/"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(response,"Hello World!");
    }
}