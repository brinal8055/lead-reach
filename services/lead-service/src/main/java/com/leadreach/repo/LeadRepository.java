package com.leadreach.repo;

import com.leadreach.dao.LeadDao;
import com.leadreach.domain.LeadEntity;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.json.JsonPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Singleton
public class LeadRepository {
    private static final Logger LOG = LoggerFactory.getLogger(LeadRepository.class);

    @Inject
    private Jdbi jdbi;

    @PostConstruct
    public void installJsonPlugin() {
        LOG.info("Installing JSON plugin for JDBI");
        try {
            jdbi.installPlugin(new JsonPlugin());
            LOG.info("JSON plugin installed successfully");
        } catch (Exception e) {
            LOG.error("Failed to install JSON plugin: {}", e.getMessage(), e);
        }
    }

    public List<LeadEntity> findAll() {
        LOG.debug("Finding all leads");
        try {
            return jdbi.withExtension(LeadDao.class, LeadDao::findAll);
        } catch (Exception e) {
            LOG.error("Error finding all leads: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public Optional<LeadEntity> findById(String id) {
        LOG.debug("Finding lead by ID: {}", id);
        try {
            return jdbi.withExtension(LeadDao.class, dao -> dao.findById(id));
        } catch (Exception e) {
            LOG.error("Error finding lead by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Optional<LeadEntity> findByEmail(String email) {
        LOG.debug("Finding lead by email: {}", email);
        try {
            return jdbi.withExtension(LeadDao.class, dao -> dao.findByEmail(email));
        } catch (Exception e) {
            LOG.error("Error finding lead by email {}: {}", email, e.getMessage(), e);
            return Optional.empty();
        }
    }

    public void save(LeadEntity lead) {
        LOG.debug("Saving lead: {}", lead.getId());
        try {
            jdbi.useExtension(LeadDao.class, dao -> dao.insert(lead));
            LOG.debug("Lead saved successfully: {}", lead.getId());
        } catch (Exception e) {
            LOG.error("Error saving lead {}: {}", lead.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save lead with id: " + lead.getId(), e);
        }
    }

    public void update(LeadEntity lead) {
        LOG.debug("Updating lead: {}", lead.getId());
        try {
            int rows = jdbi.withExtension(LeadDao.class, dao -> dao.update(lead));
            if (rows != 1) {
                LOG.error("Lead update failed for id: {}, rows affected: {}", lead.getId(), rows);
                throw new RuntimeException("Failed to update lead with id: " + lead.getId());
            }
            LOG.debug("Lead updated successfully: {}", lead.getId());
        } catch (Exception e) {
            LOG.error("Error updating lead {}: {}", lead.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to update lead with id: " + lead.getId(), e);
        }
    }

    public void updateStatus(String id, String status) {
        LOG.debug("Updating lead status: {} to {}", id, status);
        try {
            int rows = jdbi.withExtension(LeadDao.class, dao -> dao.updateStatus(id, status));
            if (rows != 1) {
                LOG.error("Lead status update failed for id: {}, rows affected: {}", id, rows);
                throw new RuntimeException("Failed to update status for lead with id: " + id);
            }
            LOG.debug("Lead status updated successfully: {}", id);
        } catch (Exception e) {
            LOG.error("Error updating lead status for {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update status for lead with id: " + id, e);
        }
    }

    public void deleteById(String id) {
        LOG.debug("Deleting lead: {}", id);
        try {
            jdbi.useExtension(LeadDao.class, dao -> dao.deleteById(id));
            LOG.debug("Lead deleted successfully: {}", id);
        } catch (Exception e) {
            LOG.error("Error deleting lead {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete lead with id: " + id, e);
        }
    }

    public long count() {
        LOG.debug("Counting leads");
        try {
            return jdbi.withExtension(LeadDao.class, LeadDao::count);
        } catch (Exception e) {
            LOG.error("Error counting leads: {}", e.getMessage(), e);
            return 0;
        }
    }
}
