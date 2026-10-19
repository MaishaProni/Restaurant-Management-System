package com.restaurant.service;

import com.restaurant.exception.BusinessException;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.Order;
import com.restaurant.model.OrderStatus;
import com.restaurant.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KitchenService {

    private final OrderRepository orderRepo;

    public List<Order> getActiveQueue() {
        return orderRepo.findByStatusIn(List.of(OrderStatus.CONFIRMED, OrderStatus.PREPARING));
    }

    @Transactional
    public Order updateStatus(Integer orderId, String statusStr) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Invalid status: " + statusStr);
        }

        if (newStatus == OrderStatus.PREPARING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("Order must be CONFIRMED before PREPARING.");
        }
        if (newStatus == OrderStatus.READY && order.getStatus() != OrderStatus.PREPARING) {
            throw new BusinessException("Order must be PREPARING before READY.");
        }

        order.setStatus(newStatus);
        return orderRepo.save(order);
    }
}
