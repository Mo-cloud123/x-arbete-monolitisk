package se.mo.xarbetemonolitisk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.mo.xarbetemonolitisk.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
