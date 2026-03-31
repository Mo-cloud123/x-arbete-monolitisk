package se.mo.xarbetemonolitisk.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import se.mo.xarbetemonolitisk.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
