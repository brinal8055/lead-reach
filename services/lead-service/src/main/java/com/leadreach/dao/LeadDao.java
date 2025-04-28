package com.leadreach.dao;

import com.leadreach.domain.LeadEntity;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface LeadDao {

    @SqlQuery("SELECT id, name, email, status FROM `lead`")
    @RegisterBeanMapper(LeadEntity.class)
    List<LeadEntity> findAll();

    @SqlQuery("SELECT id, name, email, status FROM `lead` WHERE id = :id")
    @RegisterBeanMapper(LeadEntity.class)
    Optional<LeadEntity> findById(@Bind("id") String id);

    @SqlQuery("SELECT id, name, email, status FROM `lead` WHERE email = :email")
    @RegisterBeanMapper(LeadEntity.class)
    Optional<LeadEntity> findByEmail(@Bind("email") String email);

    @SqlUpdate("INSERT INTO `lead` (id, name, email, status) VALUES (:l.id, :l.name, :l.email, :l.status)")
    void insert(@BindBean("l") LeadEntity lead);

    @SqlUpdate("UPDATE `lead` SET name = :l.name, email = :l.email, status = :l.status WHERE id = :l.id")
    int update(@BindBean("l") LeadEntity lead);

    @SqlUpdate("UPDATE `lead` SET status = :status WHERE id = :id")
    int updateStatus(@Bind("id") String id, @Bind("status") String status);

    @SqlUpdate("DELETE FROM `lead` WHERE id = :id")
    int deleteById(@Bind("id") String id);

    @SqlQuery("SELECT COUNT(*) FROM `lead`")
    long count();
}
