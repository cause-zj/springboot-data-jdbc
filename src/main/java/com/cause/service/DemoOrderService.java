package com.cause.service;

import com.cause.dao.DemoAuditRepository;
import com.cause.dao.DemoOrderRepository;
import com.cause.domain.DemoAudit;
import com.cause.domain.DemoOrder;
import com.cause.domain.DemoOrderLine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DemoOrderService {

    static final String BIZ_TYPE = "ORDER";

    private final DemoOrderRepository demoOrderRepository;
    private final DemoAuditRepository demoAuditRepository;

    public DemoOrderService(DemoOrderRepository demoOrderRepository, DemoAuditRepository demoAuditRepository) {
        this.demoOrderRepository = demoOrderRepository;
        this.demoAuditRepository = demoAuditRepository;
    }

    @Transactional(readOnly = true)
    public List<DemoOrder> list() {
        return demoOrderRepository.findAll().stream()
                .sorted(Comparator.comparing(DemoOrder::getId))
                .toList();
    }

    @Transactional(readOnly = true)
    public DemoOrder get(Long id) {
        return demoOrderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("订单不存在: " + id));
    }

    /**
     * 同一个事务里写入订单、明细和审计。
     * {@code forceRollback=true} 时在写入之后抛出运行时异常，三张表一起回滚。
     */
    @Transactional(rollbackFor = Exception.class)
    public DemoOrder place(OrderDraft draft) {
        if (draft == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (draft.lines() == null || draft.lines().isEmpty()) {
            throw new IllegalArgumentException("订单至少一条明细");
        }
        DemoOrder order = new DemoOrder();
        order.setTitle(Texts.required(draft.title(), "标题", 100));
        order.setCreateTime(Timestamps.now());
        List<DemoOrderLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        for (OrderDraft.Line line : draft.lines()) {
            DemoOrderLine entity = toLine(line);
            lines.add(entity);
            total = total.add(entity.getPrice().multiply(BigDecimal.valueOf(entity.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP));
        }
        order.setLines(lines);
        order.setTotalAmount(total);
        DemoOrder saved = demoOrderRepository.save(order);

        DemoAudit audit = new DemoAudit();
        audit.setBizType(BIZ_TYPE);
        audit.setBizId(saved.getId());
        audit.setMessage(saved.getTitle());
        audit.setCreateTime(saved.getCreateTime());
        demoAuditRepository.save(audit);

        if (draft.rollbackRequested()) {
            throw new RollbackDemoException("演示回滚：订单、明细和审计记录已撤销");
        }
        return saved;
    }

    @Transactional(rollbackFor = Exception.class)
    public DemoOrder updateTitle(Long id, OrderTitleDraft draft) {
        if (draft == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        DemoOrder order = get(id);
        order.setTitle(Texts.required(draft.title(), "标题", 100));
        return demoOrderRepository.save(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (!demoOrderRepository.existsById(id)) {
            throw new NotFoundException("订单不存在: " + id);
        }
        demoAuditRepository.deleteByBiz(BIZ_TYPE, id);
        demoOrderRepository.deleteById(id);
    }

    private static DemoOrderLine toLine(OrderDraft.Line line) {
        if (line == null) {
            throw new IllegalArgumentException("明细不能为空");
        }
        if (line.quantity() == null || line.quantity() <= 0) {
            throw new IllegalArgumentException("数量必须大于 0");
        }
        if (line.price() == null) {
            throw new IllegalArgumentException("单价不能为空");
        }
        if (line.price().signum() < 0) {
            throw new IllegalArgumentException("单价不能为负");
        }
        DemoOrderLine entity = new DemoOrderLine();
        entity.setItemName(Texts.required(line.itemName(), "明细名称", 100));
        entity.setQuantity(line.quantity());
        entity.setPrice(line.price().setScale(2, RoundingMode.HALF_UP));
        return entity;
    }
}
