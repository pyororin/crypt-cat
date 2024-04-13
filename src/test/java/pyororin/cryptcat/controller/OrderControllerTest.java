package pyororin.cryptcat.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pyororin.cryptcat.controller.filter.RequestIntervalFilter;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.IPCheckService;
import pyororin.cryptcat.service.TradeService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext context;

    @MockBean
    TradeService skipTradeService;

    @Mock
    IPCheckService ipCheckService;

    @Test
    void filter() throws Exception {
        var filterMok = MockMvcBuilders.webAppContextSetup(this.context)
                .addFilter(new RequestIntervalFilter(ipCheckService), "/order/*").build();
        when(ipCheckService.isNotAllowedIPAddress(any())).thenReturn(true);
        var response = filterMok.perform(
                        post("/order/strategy/{id}", Pair.LSK_JPY.getValue())
                                .header("x-forwarded-for", "127.0.0.2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {"reason": "test-reason", "group": "test-group1", "range": 1, "order_type": "sell", "ratio": 2.0}
                                        """)
                )
                .andExpect(status().is4xxClientError()).andReturn().getResponse().getContentAsString();
        assertEquals("", response);
    }

    @Test
    void strategySell() throws Exception {
        String response = this.mockMvc.perform(
                        post("/order/strategy/{id}", Pair.BTC_JPY.getValue())
                                .header("x-forwarded-for", "127.0.0.1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {"reason": "test-reason", "group": "test-group1", "range": 1, "order_type": "sell", "ratio": 2.0}
                                        """)
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals("OK", response);
    }

    @Test
    void strategyBuy() throws Exception {
        String response = this.mockMvc.perform(
                        post("/order/strategy/{id}", Pair.BTC_JPY.getValue())
                                .header("x-forwarded-for", "127.0.0.1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {"reason": "test-reason", "group": "test-group2", "order_type": "buy"}
                                        """)
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals("OK", response);
    }

    @Test
    void strategyBuySplit() throws Exception {
        String response = this.mockMvc.perform(
                        post("/order/strategy/{id}/split", Pair.BTC_JPY.getValue())
                                .header("x-forwarded-for", "127.0.0.1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {"reason": "test-reason", "group": "test-group2", "order_type": "buy", "ratio": 2}
                                        """)
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals("OK", response);
    }

    @Test
    void strategyOther() throws Exception {
        String response = this.mockMvc.perform(
                        post("/order/strategy/{id}", Pair.BTC_JPY.getValue())
                                .header("x-forwarded-for", "127.0.0.1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {"reason": "test-reason", "group": "test-group3", "range": 1, "order-type": "other", "ratio": 2.0}
                                        """)
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(response, "OK");
    }
}