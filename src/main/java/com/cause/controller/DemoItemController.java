package com.cause.controller;

import com.cause.domain.DemoItem;
import com.cause.domain.DemoItemDetail;
import com.cause.service.BatchDeleteResult;
import com.cause.service.DemoItemDraft;
import com.cause.service.DemoItemService;
import com.cause.service.IdBatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @GetMapping("/page")
    public Page<DemoItem> page(@RequestParam(required = false) String name, Pageable pageable) {
        return demoItemService.page(name, pageable);
    }

    @GetMapping("/with-category")
    public List<DemoItemDetail> withCategory() {
        return demoItemService.listWithCategory();
    }

    @GetMapping("/by-category/{categoryId}")
    public List<DemoItem> byCategory(@PathVariable Long categoryId) {
        return demoItemService.listByCategory(categoryId);
    }

    @GetMapping("/{id}")
    public DemoItem get(@PathVariable Long id) {
        return demoItemService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DemoItem create(@RequestBody DemoItemDraft draft) {
        return demoItemService.create(draft);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.CREATED)
    public List<DemoItem> createBatch(@RequestBody List<DemoItemDraft> drafts) {
        return demoItemService.createBatch(drafts);
    }

    @PutMapping("/{id}")
    public DemoItem update(@PathVariable Long id, @RequestBody DemoItemDraft draft) {
        return demoItemService.update(id, draft);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        demoItemService.delete(id);
    }

    @DeleteMapping("/batch")
    public BatchDeleteResult deleteBatch(@RequestBody IdBatch batch) {
        return new BatchDeleteResult(demoItemService.deleteBatch(batch == null ? null : batch.ids()));
    }
}
