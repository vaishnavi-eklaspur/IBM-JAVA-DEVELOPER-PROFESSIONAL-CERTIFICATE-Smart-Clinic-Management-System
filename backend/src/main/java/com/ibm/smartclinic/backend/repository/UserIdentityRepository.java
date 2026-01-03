package com.ibm.smartclinic.backend.repository;

import com.ibm.smartclinic.backend.model.UserIdentity;
import com.ibm.smartclinic.backend.model.UserRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {

    Optional<UserIdentity> findByEmail(@NonNull String email);

    Optional<UserIdentity> findByEmailAndRole(@NonNull String email, @NonNull UserRole role);

    boolean existsByEmail(@NonNull String email);
}
