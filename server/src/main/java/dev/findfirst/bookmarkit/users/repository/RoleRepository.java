package dev.findfirst.bookmarkit.users.repository;

import dev.findfirst.bookmarkit.users.model.user.Role;
import dev.findfirst.bookmarkit.users.model.user.URole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
  Optional<Role> findByName(URole name);

  Optional<Role> findById(Integer id);
}
