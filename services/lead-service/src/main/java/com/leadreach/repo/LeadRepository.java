package com.leadreach.repo;

import com.leadreach.dao.LeadDao;
import com.leadreach.domain.LeadEntity;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.json.JsonPlugin;

import java.util.List;
import java.util.Optional;

@Slf4j
@Singleton
public class LeadRepository {

    @Inject
    private Jdbi jdbi;

    @PostConstruct
    public void installJsonPlugin() {
        jdbi.installPlugin(new JsonPlugin());
    }

    public List<LeadEntity> findAll() {
        return jdbi.withExtension(LeadDao.class, LeadDao::findAll);
    }

    public Optional<LeadEntity> findById(String id) {
        return jdbi.withExtension(LeadDao.class, dao -> dao.findById(id));
    }

    public Optional<LeadEntity> findByEmail(String email) {
        return jdbi.withExtension(LeadDao.class, dao -> dao.findByEmail(email));
    }

    public void save(LeadEntity lead) {
        jdbi.useExtension(LeadDao.class, dao -> dao.insert(lead));
    }

    public void update(LeadEntity lead) {
        int rows = jdbi.withExtension(LeadDao.class, dao -> dao.update(lead));
        if (rows != 1) {
            log.error("Lead update failed for id: {}", lead.getId());
            throw new RuntimeException("Failed to update lead with id: " + lead.getId());
        }
    }

    public void updateStatus(String id, String status) {
        int rows = jdbi.withExtension(LeadDao.class, dao -> dao.updateStatus(id, status));
        if (rows != 1) {
            log.error("Lead status update failed for id: {}", id);
            throw new RuntimeException("Failed to update status for lead with id: " + id);
        }
    }

    public void deleteById(String id) {
        jdbi.useExtension(LeadDao.class, dao -> dao.deleteById(id));
    }

    public long count() {
        return jdbi.withExtension(LeadDao.class, LeadDao::count);
    }
}
