package com.restaurant.repository;

import com.restaurant.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    @Query("""
        SELECT oi.menuItem.name, SUM(oi.quantity)
        FROM OrderItem oi
        JOIN oi.order o
        WHERE o.status = 'PAID' AND o.createdAt BETWEEN :start AND :end
        GROUP BY oi.menuItem.itemId, oi.menuItem.name
        ORDER BY SUM(oi.quantity) DESC
        """)
    List<Object[]> findTopSellingItems(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);
}
