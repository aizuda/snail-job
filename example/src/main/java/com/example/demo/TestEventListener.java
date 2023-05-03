package com.example.demo;

import com.example.model.TransactionalEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * @author: www.byteblogs.com
 * @date : 2023-04-25 22:46
 */
@Component
@Slf4j
public class TestEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void beforeConfirmSell(TransactionalEvent<String> event) {
        log.info("触发beforeConfirmSell事件 [{}]", event);

    }
}
