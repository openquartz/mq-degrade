package com.openquartz.mqdegrade.sender.starter.autoconfig.transaction;

import com.openquartz.mqdegrade.sender.common.TransactionProxy;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

public class DefaultTransactionProxyImpl implements TransactionProxy {

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public void runInTransaction(Runnable runnable) {
        runnable.run();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Throwable.class)
    public <T> T runInTransaction(Supplier<T> callable) {
        return callable.get();
    }
}
