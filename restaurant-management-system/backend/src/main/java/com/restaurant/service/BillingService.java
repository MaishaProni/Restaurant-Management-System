package com.restaurant.service;

import com.restaurant.dto.PaymentRequest;
import com.restaurant.exception.BusinessException;
import com.restaurant.exception.ResourceNotFoundException;
import com.restaurant.model.*;
import com.restaurant.repository.*;
import com.restaurant.service.payment.PaymentStrategy;
import com.restaurant.service.payment.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final OrderRepository orderRepo;
    private final BillRepository billRepo;
    private final PaymentRepository paymentRepo;
    private final TableRepository tableRepo;
    private final PaymentStrategyFactory strategyFactory;
    private final InventoryService inventoryService;

    @Value("${billing.tax.rate:0.05}")
    private double taxRate;

    @Value("${billing.service.charge.rate:0.10}")
    private double serviceChargeRate;

    /**
     * Factory Pattern — creates a Bill entity from an order's items.
     * Idempotent: returns existing bill if already generated.
     */
    public Bill generateBill(Integer orderId) {
        Order order = getServedOrder(orderId);

        return billRepo.findByOrder(order).orElseGet(() -> {
            double subtotal = order.getItems().stream()
                    .mapToDouble(OrderItem::getSubtotal)
                    .sum();
            double tax = Math.round(subtotal * taxRate * 100.0) / 100.0;
            double serviceCharge = Math.round(subtotal * serviceChargeRate * 100.0) / 100.0;
            double grandTotal = Math.round((subtotal + tax + serviceCharge) * 100.0) / 100.0;

            Bill bill = new Bill();
            bill.setOrder(order);
            bill.setSubtotal(subtotal);
            bill.setTaxAmount(tax);
            bill.setServiceCharge(serviceCharge);
            bill.setGrandTotal(grandTotal);
            bill.setGeneratedAt(LocalDateTime.now());
            return billRepo.save(bill);
        });
    }

    @Transactional
    public String processPayment(Integer orderId, PaymentRequest req) {
        Order order = getServedOrder(orderId);

        if (billRepo.findByOrder(order).map(b -> b.getPayment() != null).orElse(false)) {
            throw new BusinessException("Order #" + orderId + " has already been paid.");
        }

        Bill bill = generateBill(orderId);

        // Strategy Pattern — select payment behaviour at runtime
        PaymentStrategy strategy = strategyFactory.getStrategy(req.getMethod());
        strategy.validate(req, bill.getGrandTotal());

        Payment payment = new Payment();
        payment.setBill(bill);
        payment.setAmount(bill.getGrandTotal());
        payment.setMethod(req.getMethod());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepo.save(payment);

        order.setStatus(OrderStatus.PAID);
        order.getTable().setStatus(TableStatus.AVAILABLE);
        orderRepo.save(order);
        tableRepo.save(order.getTable());

        // Deduct inventory (atomic, within same transaction — FR-19)
        inventoryService.deductIngredients(order);

        return buildReceipt(order, bill, payment);
    }

    private Order getServedOrder(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order #" + orderId + " not found"));
        if (order.getStatus() != OrderStatus.SERVED && order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("Order must be in SERVED status to generate a bill.");
        }
        return order;
    }

    private String buildReceipt(Order order, Bill bill, Payment payment) {
        String line = "─".repeat(36);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("\n").append(line).append("\n");
        sb.append("       RESTAURANT RECEIPT\n");
        sb.append(line).append("\n");
        sb.append("Order #: ").append(order.getOrderId()).append("\n");
        sb.append("Table  : ").append(order.getTable().getTableNumber()).append("\n");
        sb.append("Date   : ").append(payment.getPaidAt().format(fmt)).append("\n");
        sb.append(line).append("\n");
        for (OrderItem item : order.getItems()) {
            sb.append(String.format("%-20s x%d  %6.2f%n",
                    item.getMenuItem().getName(), item.getQuantity(), item.getSubtotal()));
        }
        sb.append(line).append("\n");
        sb.append(String.format("%-27s %6.2f%n", "Subtotal:", bill.getSubtotal()));
        sb.append(String.format("%-27s %6.2f%n", "Tax (5%):", bill.getTaxAmount()));
        sb.append(String.format("%-27s %6.2f%n", "Service Charge (10%):", bill.getServiceCharge()));
        sb.append(String.format("%-27s %6.2f%n", "TOTAL:", bill.getGrandTotal()));
        sb.append(line).append("\n");
        sb.append("Payment: ").append(payment.getMethod()).append("\n");
        sb.append(line).append("\n");
        sb.append("     Thank you for dining with us!\n");
        sb.append(line).append("\n");
        return sb.toString();
    }
    public String getReceipt(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order #" + orderId + " not found"));
        Bill bill = billRepo.findByOrder(order)
                .orElseThrow(() -> new BusinessException("Bill not generated for this order."));
        Payment payment = bill.getPayment();
        if (payment == null) {
            throw new BusinessException("Payment not completed for this order.");
        }
        return buildReceipt(order, bill, payment);
    }
}
