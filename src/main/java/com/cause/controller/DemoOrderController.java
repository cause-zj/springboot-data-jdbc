package com.cause.controller;

import com.cause.domain.DemoOrder;
import com.cause.service.DemoOrderService;
import com.cause.service.OrderDraft;
import com.cause.service.OrderTitleDraft;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class DemoOrderController {

    private final DemoOrderService demoOrderService;

    public DemoOrderController(DemoOrderService demoOrderService) {
        this.demoOrderService = demoOrderService;
    }

    @GetMapping
    public List<DemoOrder> list() {
        return demoOrderService.list();
    }

    @GetMapping("/{id}")
    public DemoOrder get(@PathVariable Long id) {
        return demoOrderService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemoOrder place(@RequestBody OrderDraft draft) {
        return demoOrderService.place(draft);
    }

    @PutMapping("/{id}")
    public DemoOrder updateTitle(@PathVariable Long id, @RequestBody OrderTitleDraft draft) {
        return demoOrderService.updateTitle(id, draft);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        demoOrderService.delete(id);
    }
}
