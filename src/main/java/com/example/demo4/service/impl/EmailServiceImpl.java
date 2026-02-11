package com.example.demo4.service.impl;

import com.example.demo4.config.AppEmailProperties;
import com.example.demo4.dto.response.OrderItemResponse;
import com.example.demo4.dto.response.OrderResponse;
import com.example.demo4.event.OrderEmailEventType;
import com.example.demo4.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final AppEmailProperties props;

    @Override
    public void sendOrderEmail(String to, OrderResponse order, OrderEmailEventType type) {
        if (!props.enabled() || !isValidRequest(to, order, type))
            return;

        SimpleMailMessage message = new SimpleMailMessage();
        if (props.from() != null && !props.from().isBlank()) {
            message.setFrom(props.from());
        }
        message.setTo(to);
        message.setSubject(buildSubject(order, type));
        message.setText(buildBody(order, type));
        mailSender.send(message);
    }

    // ---------------------------------------------------------------- helpers

    private boolean isValidRequest(String to, OrderResponse order, OrderEmailEventType type) {
        return to != null && !to.isBlank() && order != null && type != null;
    }

    private String buildSubject(OrderResponse order, OrderEmailEventType type) {
        String prefix = Objects.requireNonNullElse(props.subjectPrefix(), "").trim();
        String base = switch (type) {
            case ORDER_CREATED -> "Order received";
            case ORDER_PAID -> "Order confirmed";
        };
        String orderPart = order.id() != null ? " #" + order.id() : "";
        return (prefix.isEmpty() ? "" : prefix + " ") + base + orderPart;
    }

    private String buildBody(OrderResponse order, OrderEmailEventType type) {
        StringBuilder sb = new StringBuilder();

        sb.append(greeting(type)).append("\n\n");
        appendOrderDetails(sb, order);
        appendItemList(sb, order);
        appendFooter(sb, order);

        return sb.toString();
    }

    private String greeting(OrderEmailEventType type) {
        return switch (type) {
            case ORDER_CREATED -> "Thanks! We received your order.";
            case ORDER_PAID -> "Thanks! Your payment was successful and your order is confirmed.";
        };
    }

    private void appendOrderDetails(StringBuilder sb, OrderResponse order) {
        appendIfNotNull(sb, "Order ID", order.id());
        appendIfNotNull(sb, "Created at", order.createdAt());
        appendIfNotNull(sb, "Status", order.status());
        appendIfNotNull(sb, "Payment", order.paymentStatus());
        appendIfNotNull(sb, "Transaction", order.transactionId());
    }

    private void appendItemList(StringBuilder sb, OrderResponse order) {
        sb.append("\nItems:\n");
        if (order.items() == null)
            return;

        for (OrderItemResponse item : order.items()) {
            if (item == null)
                continue;
            String name = Objects.requireNonNullElse(item.productName(), "(unknown)");
            sb.append("- ").append(name);
            if (item.quantity() != null)
                sb.append(" x").append(item.quantity());
            if (item.subtotal() != null)
                sb.append(" = ").append(item.subtotal());
            sb.append("\n");
        }
    }

    private void appendFooter(StringBuilder sb, OrderResponse order) {
        if (order.totalPrice() != null) {
            sb.append("\nTotal: ").append(order.totalPrice()).append("\n");
        }
        sb.append("\nIf you did not place this order, please contact support.");
    }

    /** Appends "Label: value\n" only when value is non-null. */
    private void appendIfNotNull(StringBuilder sb, String label, Object value) {
        if (value != null)
            sb.append(label).append(": ").append(value).append("\n");
    }
}