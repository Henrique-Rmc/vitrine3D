package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Cart;
import com.store.vitrine3d.domain.model.CartStatus;
import com.store.vitrine3d.domain.model.Order;
import com.store.vitrine3d.domain.model.OrderItem;
import com.store.vitrine3d.domain.model.OrderStatus;
import com.store.vitrine3d.domain.repository.CartRepository;
import com.store.vitrine3d.domain.repository.OrderRepository;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

// DORMANT — not wired into the system yet.
// To activate: create OrderController and expose endpoints.
@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    // Converts an ACTIVE cart into an Order. Cart becomes CONVERTED.
    public Order checkout(UUID cartId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId.toString()));

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new BusinessRuleException("CART_NOT_ACTIVE", "This cart is no longer active.");
        }
        if (cart.getItems().isEmpty()) {
            throw new BusinessRuleException("CART_EMPTY", "Cannot place an order from an empty cart.");
        }
        if (cart.getCustomerName() == null || cart.getCustomerName().isBlank()) {
            throw new BusinessRuleException("CUSTOMER_NAME_REQUIRED", "Customer name is required to place an order.");
        }

        Order order = new Order();
        order.setStore(cart.getStore());
        order.setCustomerName(cart.getCustomerName());
        order.setCustomerEmail(cart.getCustomerEmail());
        order.setCustomerPhone(cart.getCustomerPhone());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (var cartItem : cart.getItems()) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductId(cartItem.getProduct() != null ? cartItem.getProduct().getId() : null);
            item.setProductName(cartItem.getProductName());
            item.setUnitPrice(cartItem.getUnitPrice());
            item.setQuantity(cartItem.getQuantity());
            order.getItems().add(item);
            total = total.add(cartItem.getSubtotal());
        }
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        cart.setStatus(CartStatus.CONVERTED);
        cartRepository.save(cart);

        return saved;
    }

    public Order updateStatus(UUID orderId, UUID storeId, OrderStatus newStatus) {
        Order order = orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId.toString()));
        order.setStatus(newStatus);
        return orderRepository.save(order);
    }

    public Order cancel(UUID orderId, UUID storeId) {
        return updateStatus(orderId, storeId, OrderStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public Page<Order> listByStore(UUID storeId, OrderStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return status != null
                ? orderRepository.findByStoreIdAndStatus(storeId, status, pageable)
                : orderRepository.findByStoreId(storeId, pageable);
    }

    @Transactional(readOnly = true)
    public Order getById(UUID orderId, UUID storeId) {
        return orderRepository.findByIdAndStoreId(orderId, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId.toString()));
    }

    @Transactional(readOnly = true)
    public long countPending(UUID storeId) {
        return orderRepository.countByStoreIdAndStatus(storeId, OrderStatus.PENDING);
    }
}
