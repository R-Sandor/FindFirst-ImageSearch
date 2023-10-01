package dev.findfirst.security.listeners;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;
import org.springframework.beans.factory.annotation.Autowired;

import dev.findfirst.security.contexts.TenantContext;
import dev.findfirst.security.model.Tenantable;

public class TenantEntityListener {

  @Autowired private TenantContext tenantContext;

  @PrePersist
  @PreUpdate
  public void prePersistAndUpdate(Object object) {
    if (object instanceof Tenantable) {
      ((Tenantable) object).setTenantId(tenantContext.getTenantId());
    }
  }

  @PreRemove
  public void preRemove(Object object) {
    if (object instanceof Tenantable
        && ((Tenantable) object).getTenantId() != tenantContext.getTenantId()) {
      throw new EntityNotFoundException();
    }
  }
}
