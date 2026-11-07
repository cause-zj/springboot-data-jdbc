package com.cause.service;

import com.cause.dao.DemoItemRepository;
import com.cause.domain.DemoItem;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DemoItemService {

    private final DemoItemRepository demoItemRepository;

    public DemoItemService(DemoItemRepository demoItemRepository) {
        this.demoItemRepository = demoItemRepository;
    }

    public List<DemoItem> list() {
        return demoItemRepository.findAll();
    }
}
