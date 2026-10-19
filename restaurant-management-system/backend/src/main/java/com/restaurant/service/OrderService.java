package com.restaurant.service;

import com.restaurant.dto.OrderItemRequest;
import com.restaurant.dto.OrderRequest;
import com.restaurant.exception.BusinessException;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.*;
import com.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final TableRepository tableRepo;
    private final MenuItemRepository menuItemRepo;
    private final UserRepository userRepo;
    private final IngredientRepository ingredientRepo;
    private final RecipeItemRepository recipeItemRepo;

    @Transactional
    public Order createOrder(OrderRequest req, Integer waiterId) {
        RestaurantTable table = tableRepo.findById(req.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new BusinessException("Table " + table.getTableNumber() + " is not available.");
        }
        User waiter = userRepo.findById(waiterId)
                .orElseThrow(() -> new ResourceNotFoundException("Waiter not found"));

        Order order = new Order();
        order.setTable(table);
        order.setWaiter(waiter);
        order.setStatus(OrderStatus.PENDING);

        table.setStatus(TableStatus.OCCUPIED);
        tableRepo.save(table);

        return orderRepo.save(order);
    }

    @Transactional
    public Order addItem(Integer orderId, OrderItemRequest req) {
        Order order = getOrderOrThrow(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Items can only be added to PENDING orders.");
        }

        MenuItem menuItem = menuItemRepo.findById(req.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

        if (!menuItem.getIsAvailable()) {
            throw new BusinessException("'" + menuItem.getName() + "' is currently unavailable.");
        }

        validateStock(menuItem, req.getQuantity());

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setMenuItem(menuItem);
        item.setQuantity(req.getQuantity());
        item.setUnitPrice(menuItem.getPrice());
        item.setNotes(req.getNotes());
        OrderItem savedItem = orderItemRepo.save(item);
        order.getItems().add(savedItem);

        return order;
    }

    @Transactional
    public Order removeItem(Integer orderId, Integer orderItemId) {
        Order order = getOrderOrThrow(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Items can only be removed from PENDING orders.");
        }
        OrderItem item = orderItemRepo.findById(orderItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Order item not found"));
        orderItemRepo.delete(item);
        order.getItems().removeIf(oi -> Objects.equals(oi.getOrderItemId(), orderItemId));
        return order;
    }

    @Transactional
    public Order confirmOrder(Integer orderId) {
        Order order = getOrderOrThrow(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Only PENDING orders can be confirmed.");
        }
        if (order.getItems().isEmpty()) {
            throw new BusinessException("Cannot confirm an empty order.");
        }
        order.setStatus(OrderStatus.CONFIRMED);
        return orderRepo.save(order);
    }

    @Transactional
    public Order cancelOrder(Integer orderId) {
        Order order = getOrderOrThrow(orderId);
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("Only PENDING or CONFIRMED orders can be cancelled.");
        }
        order.setStatus(OrderStatus.CANCELLED);
        
        RestaurantTable table = order.getTable();
        if (table != null) {
            table.setStatus(TableStatus.AVAILABLE);
            tableRepo.save(table);
        }
        
        return orderRepo.save(order);
    }

    @Transactional
    public Order markServed(Integer orderId) {
        Order order = getOrderOrThrow(orderId);
        if (order.getStatus() != OrderStatus.READY) {
            throw new BusinessException("Only READY orders can be marked as SERVED.");
        }
        order.setStatus(OrderStatus.SERVED);
        return orderRepo.save(order);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepo.findByStatus(status);
    }

    public List<Order> getAllOrders() {
        return orderRepo.findAll(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    public Order getOrder(Integer orderId) {
        return getOrderOrThrow(orderId);
    }

    public Order getActiveOrderForTable(Integer tableId) {
        RestaurantTable table = tableRepo.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
        return orderRepo.findByTable(table).stream()
                .filter(o -> o.getStatus() != OrderStatus.PAID)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No active order for this table"));
    }

    private Order getOrderOrThrow(Integer orderId) {
        return orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order #" + orderId + " not found"));
    }

    private void validateStock(MenuItem menuItem, int quantity) {
        List<RecipeItem> recipe = recipeItemRepo.findByMenuItem(menuItem);
        for (RecipeItem ri : recipe) {
            double required = ri.getQuantityRequired() * quantity;
            if (ri.getIngredient().getCurrentStock() < required) {
                throw new BusinessException(
                        "Insufficient stock for ingredient '" + ri.getIngredient().getName() +
                        "'. Required: " + required + " " + ri.getIngredient().getUnit() +
                        ", Available: " + ri.getIngredient().getCurrentStock());
            }
        }
    }
}
