package com.cause.dao;

import com.cause.domain.DemoOrder;
import org.springframework.data.repository.ListCrudRepository;

public interface DemoOrderRepository extends ListCrudRepository<DemoOrder, Long> {
}
