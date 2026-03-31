package se.mo.xarbetemonolitisk.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import se.mo.xarbetemonolitisk.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
}
