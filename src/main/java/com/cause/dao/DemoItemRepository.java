package com.cause.dao;

import com.cause.domain.DemoItem;
import org.springframework.data.repository.ListCrudRepository;

public interface DemoItemRepository extends ListCrudRepository<DemoItem, Long> {
}
