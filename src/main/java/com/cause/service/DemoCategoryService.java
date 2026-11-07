package com.cause.service;

import com.cause.dao.DemoCategoryRepository;
import com.cause.domain.DemoCategory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class DemoCategoryService {

    private final DemoCategoryRepository demoCategoryRepository;

    public DemoCategoryService(DemoCategoryRepository demoCategoryRepository) {
        this.demoCategoryRepository = demoCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<DemoCategory> list() {
        return demoCategoryRepository.findAll().stream()
                .sorted(Comparator.comparing(DemoCategory::getId))
                .toList();
    }

    @Transactional(readOnly = true)
    public DemoCategory get(Long id) {
        return demoCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("分类不存在: " + id));
    }

    @Transactional(rollbackFor = Exception.class)
    public DemoCategory create(CategoryDraft draft) {
        DemoCategory category = new DemoCategory();
        category.setName(nameOf(draft));
        category.setCreateTime(Timestamps.now());
        return demoCategoryRepository.save(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public DemoCategory update(Long id, CategoryDraft draft) {
        DemoCategory category = get(id);
        category.setName(nameOf(draft));
        return demoCategoryRepository.save(category);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (!demoCategoryRepository.existsById(id)) {
            throw new NotFoundException("分类不存在: " + id);
        }
        demoCategoryRepository.deleteById(id);
    }

    private static String nameOf(CategoryDraft draft) {
        if (draft == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        return Texts.required(draft.name(), "分类名称", 100);
    }
}
