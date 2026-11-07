package com.cause.controller;

import com.cause.domain.DemoItem;
import com.cause.service.DemoItemService;
import com.cause.service.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {HealthController.class, DemoItemController.class})
class ApiEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DemoItemService demoItemService;

    @Test
    void pingReturnsOk() throws Exception {
        mockMvc.perform(get("/api/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void healthReturnsUp() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").value("springboot-data-jdbc"));
    }

    @Test
    void demoItemsCanBeEmpty() throws Exception {
        when(demoItemService.list()).thenReturn(List.of());

        mockMvc.perform(get("/api/demo-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void demoItemsReturnsRows() throws Exception {
        DemoItem item = new DemoItem();
        item.setId(1L);
        item.setName("示例条目");
        item.setCreateTime(LocalDateTime.of(2026, 11, 7, 8, 0));
        when(demoItemService.list()).thenReturn(List.of(item));

        mockMvc.perform(get("/api/demo-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("示例条目"))
                .andExpect(jsonPath("$[0].createTime").value("2026-11-07T08:00:00"));
    }

    @Test
    void createRejectsInvalidItem() throws Exception {
        when(demoItemService.create(any())).thenThrow(new IllegalArgumentException("金额不能为空"));

        mockMvc.perform(post("/api/demo-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("bad_request"))
                .andExpect(jsonPath("$.message").value("金额不能为空"));
    }

    @Test
    void missingItemReturns404() throws Exception {
        when(demoItemService.get(9L)).thenThrow(new NotFoundException("条目不存在: 9"));

        mockMvc.perform(get("/api/demo-items/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }
}
