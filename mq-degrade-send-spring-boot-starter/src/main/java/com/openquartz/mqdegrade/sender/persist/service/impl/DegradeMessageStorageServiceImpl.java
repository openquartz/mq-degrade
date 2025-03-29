package com.openquartz.mqdegrade.sender.persist.service.impl;

import com.openquartz.mqdegrade.sender.common.TransactionProxy;
import com.openquartz.mqdegrade.sender.common.utils.IpUtils;
import com.openquartz.mqdegrade.sender.core.context.ThreadContextSerializer;
import com.openquartz.mqdegrade.sender.persist.mapper.DegradeMessageEntityMapper;
import com.openquartz.mqdegrade.sender.persist.model.DegradeMessageEntity;
import com.openquartz.mqdegrade.sender.persist.service.DegradeMessageStorageService;

public class DegradeMessageStorageServiceImpl implements DegradeMessageStorageService {

    private final DegradeMessageEntityMapper degradeMessageEntityMapper;
    private final ThreadContextSerializer threadContextSerializer;
    private final TransactionProxy transactionProxy;

    public DegradeMessageStorageServiceImpl(DegradeMessageEntityMapper degradeMessageEntityMapper,
                                            ThreadContextSerializer threadContextSerializer,
                                            TransactionProxy transactionProxy) {
        this.degradeMessageEntityMapper = degradeMessageEntityMapper;
        this.threadContextSerializer = threadContextSerializer;
        this.transactionProxy = transactionProxy;
    }

    @Override
    public void save(String resource, String message, String key) {
        transactionProxy
                .runInTransaction(() -> {
                    DegradeMessageEntity degradeMessageEntity = new DegradeMessageEntity();
                    degradeMessageEntity.setResource(resource);
                    degradeMessageEntity.setContext(threadContextSerializer.serializeContext());
                    degradeMessageEntity.setMessage(message);
                    degradeMessageEntity.setMsgKey(key);
                    degradeMessageEntity.setIpAddr(IpUtils.getIp());
                    degradeMessageEntityMapper.insert(degradeMessageEntity);
                });
    }


}
