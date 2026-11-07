package com.cause.dao;

import com.cause.domain.DemoAudit;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;

public interface DemoAuditRepository extends ListCrudRepository<DemoAudit, Long> {

    @Modifying
    @Query("DELETE FROM demo_audit WHERE biz_type = :bizType AND biz_id = :bizId")
    void deleteByBiz(@Param("bizType") String bizType, @Param("bizId") Long bizId);
}
