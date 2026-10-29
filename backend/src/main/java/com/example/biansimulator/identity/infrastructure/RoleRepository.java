package com.example.biansimulator.identity.infrastructure;

import com.example.biansimulator.identity.domain.Role;
import com.example.biansimulator.identity.domain.RoleCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, RoleCode> {
}
