package com.cause.controller;

import com.cause.domain.DemoItem;
import com.cause.service.DemoItemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/demo-items")
public class DemoItemController {

    private final DemoItemService demoItemService;

    public DemoItemController(DemoItemService demoItemService) {
        this.demoItemService = demoItemService;
    }

    @GetMapping
    public List<DemoItem> list() {
        return demoItemService.list();
    }
}
