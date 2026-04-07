package se.mo.xarbetemonolitisk.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mo.xarbetemonolitisk.dto.order.CreateOrderRequest;
import se.mo.xarbetemonolitisk.dto.order.OrderItemRequest;
import se.mo.xarbetemonolitisk.dto.order.OrderItemResponse;
import se.mo.xarbetemonolitisk.dto.order.OrderResponse;
import se.mo.xarbetemonolitisk.entity.Order;
import se.mo.xarbetemonolitisk.entity.OrderItem;
import se.mo.xarbetemonolitisk.entity.Product;
import se.mo.xarbetemonolitisk.entity.User;
import se.mo.xarbetemonolitisk.exception.BusinessException;
import se.mo.xarbetemonolitisk.exception.ResourceNotFoundException;
import se.mo.xarbetemonolitisk.repository.OrderRepository;
import se.mo.xarbetemonolitisk.repository.ProductRepository;
import se.mo.xarbetemonolitisk.repository.UserRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                        ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Order must contain at least one item");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Order order = new Order();
        order.setUser(user);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + itemRequest.getProductId()));

            if (product.getStock() < itemRequest.getQuantity()) {
                throw new BusinessException(
                        "Insufficient stock for product id " + product.getId() + ", available: " + product.getStock());
            }

            product.setStock(product.getStock() - itemRequest.getQuantity());

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemRequest.getQuantity());
            order.getItems().add(item);

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(lineTotal);
        }

        order.setTotalPrice(total);
        Order saved = orderRepository.save(order);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUser().getId());
        response.setCreatedAt(order.getCreatedAt());
        response.setTotalPrice(order.getTotalPrice());

        List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> {
            OrderItemResponse ir = new OrderItemResponse();
            ir.setProductId(item.getProduct().getId());
            ir.setProductName(item.getProduct().getName());
            ir.setUnitPrice(item.getProduct().getPrice());
            ir.setQuantity(item.getQuantity());
            BigDecimal lineTotal = item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            ir.setLineTotal(lineTotal);
            return ir;
        }).toList();

        response.setItems(itemResponses);
        return response;
    }
}
