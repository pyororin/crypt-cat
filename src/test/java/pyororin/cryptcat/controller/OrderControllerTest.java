package pyororin.cryptcat.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.TradeService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockBean
    TradeService skipTradeService;

    @Test
    void buy() throws Exception {
        String response = this.mockMvc.perform(
                        post("/order/buy/{id}", Pair.BTC_JPY.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {"reason": "test-reason", "group": "test-group", "range": 3}
                                        """)
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(response, "OK");
    }

    @Test
    void shortInterval() throws Exception {
        this.mockMvc.perform(
                post("/order/buy/{id}", Pair.LSK_JPY.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {"reason": "test-reason", "group": "test-group", "range": 3}
                                """)
        );
        String response = this.mockMvc.perform(
                        post("/order/buy/{id}", Pair.LSK_JPY.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {"reason": "test-reason", "group": "test-group", "range": 1}
                                        """)
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(response, "Request interval too short. Please try again later.");
    }

    @Test
    void sell() throws Exception {
        String response = this.mockMvc.perform(
                        post("/order/sell/{id}", Pair.BTC_JPY.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {"reason": "test-reason", "group": "test-group", "range": 1}
                                        """)
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(response, "OK");
    }

    @Test
    void strategySell() throws Exception {
        String response = this.mockMvc.perform(
                        post("/order/strategy/{id}", Pair.BTC_JPY.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {"reason": "test-reason", "group": "test-group1", "range": 1, "order_type": "sell"}
                                        """)
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(response, "OK");
    }

    @Test
    void strategyBuy() throws Exception {
        String response = this.mockMvc.perform(
                        post("/order/strategy/{id}", Pair.BTC_JPY.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {"reason": "test-reason", "group": "test-group2", "range": 1, "order_type": "buy"}
                                        """)
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(response, "OK");
    }

    @Test
    void strategyOther() throws Exception {
        String response = this.mockMvc.perform(
                        post("/order/strategy/{id}", Pair.BTC_JPY.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {"reason": "test-reason", "group": "test-group3", "range": 1, "order-type": "other"}
                                        """)
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(response, "OK");
    }
}