package com.example.ecommerce.backend.event;

import com.example.ecommerce.backend.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEmailEventListener {

    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAfterCommit(OrderEmailEvent event) {
        log.info("Sending {} email to {}", event.type(), event.to());
        emailService.sendOrderEmail(event.to(), event.order(), event.type());
    }
}