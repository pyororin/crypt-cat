package pyororin.cryptcat.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pyororin.cryptcat.batch.TradeBatchServiceImpl;
import pyororin.cryptcat.controller.filter.RequestIntervalFilter;
import pyororin.cryptcat.repository.model.Pair;
import pyororin.cryptcat.service.IPCheckService;
import pyororin.cryptcat.service.TradeService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext context;

    @MockBean(name = "tradeBtcFixServiceImpl")
    TradeService tradeBtcFixServiceImpl;

    @MockBean(name = "tradeJpyFixServiceImpl")
    TradeService tradeJpyFixServiceImpl;

    @MockBean(name = "tradeJpyFixServiceV2Impl")
    TradeService tradeJpyFixServiceV2Impl;

    @MockBean(name = "tradeJpyFixServiceV3Impl")
    TradeService tradeJpyFixServiceV3Impl;

    @MockBean(name = "tradeJpyFixServiceV4Impl")
    TradeService tradeJpyFixServiceV4Impl;

    @MockBean(name = "tradeJpyFixServiceV5Impl")
    TradeService tradeJpyFixServiceV5Impl;

    @MockBean(name = "tradeJpyFixBuyServiceV6Impl")
    TradeService tradeJpyFixBuyServiceV6Impl;

    @MockBean(name = "tradeJpyFixSellServiceV6Impl")
    TradeService tradeJpyFixSellServiceV6Impl;

    @MockBean(name = "tradeAllInBuyServiceV2Impl")
    TradeService tradeAllInBuyServiceV2Impl;

    @MockBean(name = "tradeAllInSellServiceV2Impl")
    TradeService tradeAllInSellServiceV2Impl;

    @MockBean(name = "tradeBatchServiceImpl")
    TradeBatchServiceImpl tradeBatchServiceImpl;

    @Mock
    IPCheckService ipCheckService;

    @Test
    void filterOK() throws Exception {
        var filterMok = MockMvcBuilders.webAppContextSetup(this.context)
                .addFilter(new RequestIntervalFilter(ipCheckService), "/order/*").build();
        when(ipCheckService.isNotAllowedIPAddress(any())).thenReturn(false);
        var response = filterMok.perform(
                        post("/order/btcfix/{id}", Pair.LSK_JPY.getValue())
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
    void filterNG() throws Exception {
        var filterMok = MockMvcBuilders.webAppContextSetup(this.context)
                .addFilter(new RequestIntervalFilter(ipCheckService), "/order/*").build();
        when(ipCheckService.isNotAllowedIPAddress(any())).thenReturn(true);
        var response = filterMok.perform(
                        post("/order/btcfix/{id}", Pair.LSK_JPY.getValue())
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
    void btcfixSell() throws Exception {
        String response = this.mockMvc.perform(
                        post("/order/btcfix/{id}", Pair.BTC_JPY.getValue())
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
    @Disabled
    void btcfixBuy() throws Exception {
        String response = this.mockMvc.perform(
                        post("/order/btcfix/{id}", Pair.BTC_JPY.getValue())
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
    void jpyfixBuy() throws Exception {
        String response = this.mockMvc.perform(
                        post("/order/jpyfix/{id}", Pair.BTC_JPY.getValue())
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
    void jpyfixBuyV4() throws Exception {
        String response = this.mockMvc.perform(
                        post("/v4/order/jpyfix/{id}", Pair.BTC_JPY.getValue())
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
    void jpyfixSell() throws Exception {
        String response = this.mockMvc.perform(
                        post("/order/jpyfix/{id}", Pair.BTC_JPY.getValue())
                                .header("x-forwarded-for", "127.0.0.1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {"reason": "test-reason", "group": "test-group2", "order_type": "sell", "ratio": 2}
                                        """)
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals("OK", response);
    }

    @Test
    void ng() throws Exception {
        doThrow(RuntimeException.class).when(tradeJpyFixServiceImpl).order(any(), any());
        String response = this.mockMvc.perform(
                        post("/order/jpyfix/{id}", Pair.BTC_JPY.getValue())
                                .header("x-forwarded-for", "127.0.0.1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                            {"reason": "test-reason", "group": "test-group2", "order_type": "sell", "ratio": 2}
                                        """)
                )
                .andExpect(status().is5xxServerError()).andReturn().getResponse().getContentAsString();
        assertEquals("", response);
    }

    @Test
    void clearTransaction() throws Exception {
        String response = this.mockMvc.perform(
                        delete("/transaction/{minutes}", 360)
                                .header("x-forwarded-for", "127.0.0.1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals("OK", response);
    }
}