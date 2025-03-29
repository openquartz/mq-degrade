package com.openquartz.mqdegrade.sender.common;

import java.util.function.Supplier;

public interface TransactionProxy {

    /**
     * 在事务中运行
     * @param runnable 方法
     */
    void runInTransaction(Runnable runnable);

    /**
     * 在事务中运行
     */
    <T>T runInTransaction(Supplier<T> runnableSupplier);
}
