package com.cause.dao;

import com.cause.domain.DemoCategory;
import org.springframework.data.repository.ListCrudRepository;

public interface DemoCategoryRepository extends ListCrudRepository<DemoCategory, Long> {
}
