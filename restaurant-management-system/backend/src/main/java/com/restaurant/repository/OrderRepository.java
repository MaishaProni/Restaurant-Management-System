package com.restaurant.repository;

import com.restaurant.model.Order;
import com.restaurant.model.OrderStatus;
import com.restaurant.model.RestaurantTable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @EntityGraph(attributePaths = {"table", "waiter", "items", "items.menuItem"})
    List<Order> findByStatus(OrderStatus status);

    @EntityGraph(attributePaths = {"table", "waiter", "items", "items.menuItem"})
    List<Order> findByStatusIn(List<OrderStatus> statuses);

    @EntityGraph(attributePaths = {"table", "waiter", "items", "items.menuItem"})
    List<Order> findByTable(RestaurantTable table);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end AND o.status = 'PAID'")
    List<Order> findPaidOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
