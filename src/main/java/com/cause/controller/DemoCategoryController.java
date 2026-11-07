package com.cause.controller;

import com.cause.domain.DemoCategory;
import com.cause.service.CategoryDraft;
import com.cause.service.DemoCategoryService;
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
@RequestMapping("/api/categories")
public class DemoCategoryController {

    private final DemoCategoryService demoCategoryService;

    public DemoCategoryController(DemoCategoryService demoCategoryService) {
        this.demoCategoryService = demoCategoryService;
    }

    @GetMapping
    public List<DemoCategory> list() {
        return demoCategoryService.list();
    }

    @GetMapping("/{id}")
    public DemoCategory get(@PathVariable Long id) {
        return demoCategoryService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemoCategory create(@RequestBody CategoryDraft draft) {
        return demoCategoryService.create(draft);
    }

    @PutMapping("/{id}")
    public DemoCategory update(@PathVariable Long id, @RequestBody CategoryDraft draft) {
        return demoCategoryService.update(id, draft);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        demoCategoryService.delete(id);
    }
}
