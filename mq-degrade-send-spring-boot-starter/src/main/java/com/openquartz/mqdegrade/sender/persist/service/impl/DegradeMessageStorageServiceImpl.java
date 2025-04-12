package com.openquartz.mqdegrade.sender.persist.service.impl;

import com.openquartz.mqdegrade.sender.common.Pair;
import com.openquartz.mqdegrade.sender.common.TransactionProxy;
import com.openquartz.mqdegrade.sender.common.utils.IpUtils;
import com.openquartz.mqdegrade.sender.common.utils.StringUtils;
import com.openquartz.mqdegrade.sender.core.context.ThreadContextSerializer;
import com.openquartz.mqdegrade.sender.persist.mapper.DegradeMessageEntityMapper;
import com.openquartz.mqdegrade.sender.persist.mapper.DegradeMessageSummaryResult;
import com.openquartz.mqdegrade.sender.persist.mapper.GetDegradeMessageEntityCondition;
import com.openquartz.mqdegrade.sender.persist.model.DegradeMessageEntity;
import com.openquartz.mqdegrade.sender.persist.service.DegradeMessageStorageService;

import java.util.Collections;
import java.util.Date;
import java.util.List;

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

    @Override
    public List<DegradeMessageEntity> getSpecMachineMessages(String ip, Integer maxRetryCount, Pair<Date, Date> timeRange, Integer limit) {

        if (StringUtils.isBlank(ip)) {
            return Collections.emptyList();
        }

        return degradeMessageEntityMapper.selectByIpAddr(ip, maxRetryCount, timeRange, limit);
    }

    @Override
    public List<DegradeMessageEntity> getAllMachineMessages(Integer maxRetryCount, Pair<Date, Date> timeRange, Integer limit) {
        return degradeMessageEntityMapper.selectByTimeRange(maxRetryCount, timeRange, limit);
    }

    @Override
    public void delete(Long id) {
        transactionProxy.runInTransaction(() -> degradeMessageEntityMapper.deleteById(id));
    }

    @Override
    public void retryFail(Long id) {
        transactionProxy.runInTransaction(() -> degradeMessageEntityMapper.updateRetryCount(id));
    }

    @Override
    public Pair<Date, Date> getCreateTimeRange() {
        return degradeMessageEntityMapper.selectCreateTimeRange();
    }

    @Override
    public Pair<Date, Date> getCreateTimeRange(List<String> msgKeyList) {
        return degradeMessageEntityMapper.selectCreateTimeRange(msgKeyList);
    }

    @Override
    public List<DegradeMessageEntity> getByCondition(GetDegradeMessageEntityCondition condition) {
        return degradeMessageEntityMapper.listByCondition(condition);
    }

    @Override
    public List<DegradeMessageSummaryResult> countByCondition(GetDegradeMessageEntityCondition condition) {
        return degradeMessageEntityMapper.countByCondition(condition);
    }
}
