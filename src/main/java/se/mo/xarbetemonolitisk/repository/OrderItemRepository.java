package se.mo.xarbetemonolitisk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.mo.xarbetemonolitisk.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
