package se.mo.xarbetemonolitisk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.mo.xarbetemonolitisk.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
