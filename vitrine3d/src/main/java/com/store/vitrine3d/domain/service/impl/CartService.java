package com.store.vitrine3d.domain.service.impl;

import com.store.vitrine3d.domain.model.Cart;
import com.store.vitrine3d.domain.model.CartItem;
import com.store.vitrine3d.domain.model.CartStatus;
import com.store.vitrine3d.domain.model.Product;
import com.store.vitrine3d.domain.repository.CartRepository;
import com.store.vitrine3d.domain.repository.ProductRepository;
import com.store.vitrine3d.domain.repository.StoreRepository;
import com.store.vitrine3d.rest.dto.CartItemRequest;
import com.store.vitrine3d.rest.exception.BusinessRuleException;
import com.store.vitrine3d.rest.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

// DORMANT — not wired into the system yet.
// To activate: create CartController and expose endpoints.
@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;

    public CartService(CartRepository cartRepository,
                       StoreRepository storeRepository,
                       ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
    }

    public Cart create(UUID storeId) {
        Cart cart = new Cart();
        cart.setStore(storeRepository.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", storeId.toString())));
        return cartRepository.save(cart);
    }

    public Cart addItem(UUID cartId, CartItemRequest request) {
        Cart cart = requireCart(cartId);
        assertActive(cart);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));

        if (!product.getStore().getId().equals(cart.getStore().getId())) {
            throw new AccessDeniedException("Product does not belong to this store's cart.");
        }

        cart.getItems().stream()
                .filter(i -> i.getProduct() != null && i.getProduct().getId().equals(product.getId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + request.getQuantity()),
                        () -> {
                            CartItem item = new CartItem();
                            item.setCart(cart);
                            item.setProduct(product);
                            item.setProductName(product.getName());
                            item.setUnitPrice(product.getPrice());
                            item.setQuantity(request.getQuantity());
                            cart.getItems().add(item);
                        }
                );

        return cartRepository.save(cart);
    }

    public Cart removeItem(UUID cartId, Long itemId) {
        Cart cart = requireCart(cartId);
        assertActive(cart);
        boolean removed = cart.getItems().removeIf(i -> i.getId().equals(itemId));
        if (!removed) throw new ResourceNotFoundException("CartItem", itemId);
        return cartRepository.save(cart);
    }

    public Cart updateCustomerInfo(UUID cartId, String customerName, String customerEmail, String customerPhone) {
        Cart cart = requireCart(cartId);
        assertActive(cart);
        if (customerName != null) cart.setCustomerName(customerName);
        if (customerEmail != null) cart.setCustomerEmail(customerEmail);
        if (customerPhone != null) cart.setCustomerPhone(customerPhone);
        return cartRepository.save(cart);
    }

    public void abandon(UUID cartId) {
        Cart cart = requireCart(cartId);
        cart.setStatus(CartStatus.ABANDONED);
        cartRepository.save(cart);
    }

    @Transactional(readOnly = true)
    public Page<Cart> listByStore(UUID storeId, CartStatus status, int page, int size) {
        return cartRepository.findByStoreIdAndStatus(storeId, status,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Transactional(readOnly = true)
    public Cart getById(UUID cartId) {
        return requireCart(cartId);
    }

    private Cart requireCart(UUID cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", cartId.toString()));
    }

    private void assertActive(Cart cart) {
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new BusinessRuleException("CART_NOT_ACTIVE", "This cart is no longer active.");
        }
    }
}
