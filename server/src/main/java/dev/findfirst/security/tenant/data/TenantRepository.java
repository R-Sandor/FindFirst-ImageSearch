package dev.findfirst.security.tenant.data;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.findfirst.security.tenant.model.Tenant;

interface TenantRepository extends JpaRepository<Tenant, Integer> {}
