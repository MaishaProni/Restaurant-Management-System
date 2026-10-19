package com.restaurant.repository;

import com.restaurant.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    @Query("""
        SELECT p
        FROM Payment p
        JOIN FETCH p.bill b
        JOIN FETCH b.order o
        WHERE p.paidAt BETWEEN :start AND :end
        """)
    List<Payment> findByPaidAtBetween(@Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    @Query("""
        SELECT oi.menuItem.name, SUM(oi.quantity)
        FROM Payment p
        JOIN p.bill b
        JOIN b.order o
        JOIN o.items oi
        WHERE p.paidAt BETWEEN :start AND :end
        GROUP BY oi.menuItem.itemId, oi.menuItem.name
        ORDER BY SUM(oi.quantity) DESC
        """)
    List<Object[]> findTopSellingItemsByPaidAtBetween(@Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);
}
