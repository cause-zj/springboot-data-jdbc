package com.cause.dao;

import com.cause.domain.DemoItem;
import com.cause.domain.DemoItemDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DemoItemRepository extends ListCrudRepository<DemoItem, Long>, PagingAndSortingRepository<DemoItem, Long> {

    Page<DemoItem> findByNameContaining(String name, Pageable pageable);

    List<DemoItem> findByCategoryId(Long categoryId);

    @Query(value = """
            SELECT i.id AS id,
                   i.name AS name,
                   i.category_id AS category_id,
                   c.name AS category_name,
                   i.amount AS amount,
                   i.remark AS remark,
                   i.create_time AS create_time,
                   i.update_time AS update_time
              FROM demo_item i
              LEFT JOIN demo_category c ON c.id = i.category_id
             ORDER BY i.id
            """, rowMapperClass = DemoItemDetailRowMapper.class)
    List<DemoItemDetail> findAllWithCategory();

    @Modifying
    @Query("DELETE FROM demo_item WHERE id IN (:ids)")
    int deleteAllByIds(@Param("ids") Collection<Long> ids);
}
