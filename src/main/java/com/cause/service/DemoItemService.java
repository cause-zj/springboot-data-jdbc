package com.cause.service;

import com.cause.dao.DemoCategoryRepository;
import com.cause.dao.DemoItemRepository;
import com.cause.domain.DemoItem;
import com.cause.domain.DemoItemDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class DemoItemService {

    private static final Set<String> SORT_FIELDS = Set.of(
            "id", "name", "amount", "remark", "categoryId", "createTime", "updateTime");

    private final DemoItemRepository demoItemRepository;
    private final DemoCategoryRepository demoCategoryRepository;

    public DemoItemService(DemoItemRepository demoItemRepository, DemoCategoryRepository demoCategoryRepository) {
        this.demoItemRepository = demoItemRepository;
        this.demoCategoryRepository = demoCategoryRepository;
    }

    @Transactional(readOnly = true)
    public List<DemoItem> list() {
        return demoItemRepository.findAll().stream()
                .sorted(Comparator.comparing(DemoItem::getId))
                .toList();
    }

    @Transactional(readOnly = true)
    public DemoItem get(Long id) {
        return demoItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("条目不存在: " + id));
    }

    @Transactional(readOnly = true)
    public Page<DemoItem> page(String name, Pageable pageable) {
        Pageable sorted = sanitize(pageable);
        if (name == null || name.isBlank()) {
            return demoItemRepository.findAll(sorted);
        }
        return demoItemRepository.findByNameContaining(name.trim(), sorted);
    }

    @Transactional(readOnly = true)
    public List<DemoItem> listByCategory(Long categoryId) {
        if (!demoCategoryRepository.existsById(categoryId)) {
            throw new NotFoundException("分类不存在: " + categoryId);
        }
        return demoItemRepository.findByCategoryId(categoryId).stream()
                .sorted(Comparator.comparing(DemoItem::getId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DemoItemDetail> listWithCategory() {
        return demoItemRepository.findAllWithCategory();
    }

    @Transactional(rollbackFor = Exception.class)
    public DemoItem create(DemoItemDraft draft) {
        DemoItem saved = demoItemRepository.save(newItem(draft));
        return reload(saved.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public List<DemoItem> createBatch(List<DemoItemDraft> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            throw new IllegalArgumentException("批量新增至少需要一条");
        }
        if (drafts.size() > 100) {
            throw new IllegalArgumentException("单次批量最多 100 条");
        }
        List<DemoItem> items = new ArrayList<>();
        for (DemoItemDraft draft : drafts) {
            items.add(newItem(draft));
        }
        return demoItemRepository.saveAll(items).stream()
                .map(item -> reload(item.getId()))
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public DemoItem update(Long id, DemoItemDraft draft) {
        DemoItem item = get(id);
        apply(item, draft);
        item.setUpdateTime(Timestamps.now());
        demoItemRepository.save(item);
        return reload(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (!demoItemRepository.existsById(id)) {
            throw new NotFoundException("条目不存在: " + id);
        }
        demoItemRepository.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteBatch(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("批量删除至少需要一个 id");
        }
        List<Long> distinct = ids.stream().filter(Objects::nonNull).distinct().toList();
        if (distinct.isEmpty()) {
            throw new IllegalArgumentException("批量删除至少需要一个 id");
        }
        if (distinct.size() > 1000) {
            throw new IllegalArgumentException("单次批量删除最多 1000 个 id");
        }
        return demoItemRepository.deleteAllByIds(distinct);
    }

    private Pageable sanitize(Pageable pageable) {
        Sort sort = pageable.getSort().isUnsorted() ? Sort.by(Sort.Order.asc("id")) : pageable.getSort();
        for (Sort.Order order : sort) {
            if (!SORT_FIELDS.contains(order.getProperty())) {
                throw new IllegalArgumentException("排序字段不存在");
            }
        }
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
    }

    private DemoItem reload(Long id) {
        return demoItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("条目不存在: " + id));
    }

    private DemoItem newItem(DemoItemDraft draft) {
        DemoItem item = new DemoItem();
        apply(item, draft);
        item.setCreateTime(Timestamps.now());
        return item;
    }

    private void apply(DemoItem item, DemoItemDraft draft) {
        if (draft == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        Long categoryId = draft.categoryId();
        if (categoryId != null && !demoCategoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("分类不存在: " + categoryId);
        }
        item.setName(Texts.required(draft.name(), "名称", 100));
        item.setCategoryId(categoryId);
        item.setAmount(Texts.money(draft.amount()));
        item.setRemark(Texts.optional(draft.remark(), "备注", 200));
    }
}
