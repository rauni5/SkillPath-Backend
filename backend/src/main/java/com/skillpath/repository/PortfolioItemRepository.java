package com.skillpath.repository;
import com.skillpath.model.PortfolioItem.PortfolioItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface PortfolioItemRepository extends JpaRepository<PortfolioItem,
Long> {
 List<PortfolioItem> findByUserId(Long userId);
}
