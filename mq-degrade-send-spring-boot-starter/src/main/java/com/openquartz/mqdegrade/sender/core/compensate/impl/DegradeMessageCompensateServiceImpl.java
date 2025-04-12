package com.openquartz.mqdegrade.sender.core.compensate.impl;

import com.openquartz.mqdegrade.sender.common.Pair;
import com.openquartz.mqdegrade.sender.common.alert.DegradeAlert;
import com.openquartz.mqdegrade.sender.common.utils.DateUtils;
import com.openquartz.mqdegrade.sender.common.utils.SerdeUtils;
import com.openquartz.mqdegrade.sender.common.utils.StringUtils;
import com.openquartz.mqdegrade.sender.core.compensate.DegradeMessageCompensateService;
import com.openquartz.mqdegrade.sender.core.compensate.condition.CompensateDegradeMessageCondition;
import com.openquartz.mqdegrade.sender.core.config.DegradeMessageConfig;
import com.openquartz.mqdegrade.sender.core.context.ThreadContextSerializer;
import com.openquartz.mqdegrade.sender.core.factory.SendRouterFactory;
import com.openquartz.mqdegrade.sender.persist.mapper.DegradeMessageSummaryResult;
import com.openquartz.mqdegrade.sender.persist.mapper.GetDegradeMessageEntityCondition;
import com.openquartz.mqdegrade.sender.persist.model.DegradeMessageEntity;
import com.openquartz.mqdegrade.sender.persist.service.DegradeMessageStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Predicate;

/**
 * DegradeMessageCompensateServiceImpl
 *
 * @author svnee
 */
@Slf4j
@Service
public class DegradeMessageCompensateServiceImpl implements DegradeMessageCompensateService {

    private final DegradeMessageConfig degradeMessageConfig;
    private final DegradeMessageStorageService degradeMessageStorageService;
    private final ThreadContextSerializer threadContextSerializer;
    private final DegradeAlert degradeAlert;

    private static final Integer MAX_CIRCULATE_LOOP_COUNT = 1024;
    private static final Integer DEFAULT_STEP_MINUTES = 10;
    private static final Integer DEFAULT_MAX_RETRY_COUNT = 15;

    private static final String ALERT_TEMPLATE = "标题：MQ-降级消息传输\n描述：超出最大重试次数%d的失败的总条数为%d.详情：%s";
    private static final String ALERT_ITEM_TEMPLATE = "资源：%s -> %d";

    public DegradeMessageCompensateServiceImpl(DegradeMessageConfig degradeMessageConfig,
                                               DegradeMessageStorageService degradeMessageStorageService,
                                               ThreadContextSerializer threadContextSerializer,
                                               DegradeAlert degradeAlert) {
        this.degradeMessageConfig = degradeMessageConfig;
        this.degradeMessageStorageService = degradeMessageStorageService;
        this.threadContextSerializer = threadContextSerializer;
        this.degradeAlert = degradeAlert;
    }


    @Override
    public void compensate() {

        List<DegradeMessageEntity> degradeMessageEntityList =
                degradeMessageStorageService.getAllMachineMessages(degradeMessageConfig.getCompensateMaxRetryCount(),
                        Pair.of(DateUtils.addSeconds(new Date(), -degradeMessageConfig.getCompensateGlobalBackOffIntervalTime()), new Date()),
                        degradeMessageConfig.getCompensateLimitCount());

        if (degradeMessageEntityList.isEmpty()) {
            return;
        }

        for (DegradeMessageEntity degradeMessageEntity : degradeMessageEntityList) {
            doProcessCompensate(degradeMessageEntity);
        }

    }

    private void doProcessCompensate(DegradeMessageEntity degradeMessageEntity) {
        Pair<Class<Object>, Predicate<Object>> sendPair = SendRouterFactory.get(degradeMessageEntity.getResource());
        boolean sendResult = false;
        try {
            threadContextSerializer.deserializeContext(degradeMessageEntity.getContext());
            sendResult = sendPair.getValue().test(SerdeUtils.fromJson(degradeMessageEntity.getMessage(), sendPair.getKey()));
        } catch (Exception ex) {
            log.error("[DegradeMessageCompensateServiceImpl#doProcessCompensate] send fail,resource:{},msgKey:{},msg:{}",
                    degradeMessageEntity.getResource(), degradeMessageEntity.getMsgKey(), degradeMessageEntity.getMessage(), ex);
        }

        if (sendResult) {
            degradeMessageStorageService.delete(degradeMessageEntity.getId());
            return;
        }

        degradeMessageEntity.setRetryCount(degradeMessageEntity.getRetryCount() + 1);
        degradeMessageStorageService.retryFail(degradeMessageEntity.getId());
    }

    @Override
    public void compensate(String ip) {

        if (StringUtils.isBlank(ip)) {
            return;
        }

        List<DegradeMessageEntity> degradeMessageEntityList =
                degradeMessageStorageService.getSpecMachineMessages(ip, degradeMessageConfig.getCompensateMaxRetryCount(),
                        Pair.of(DateUtils.addSeconds(new Date(), -degradeMessageConfig.getCompensateSelfBackOffIntervalTime()), new Date()),
                        degradeMessageConfig.getCompensateLimitCount());

        if (degradeMessageEntityList.isEmpty()) {
            return;
        }

        for (DegradeMessageEntity degradeMessageEntity : degradeMessageEntityList) {
            doProcessCompensate(degradeMessageEntity);
        }

    }

    @Override
    public void compensateAlert() {

        CompensateDegradeMessageCondition condition = CompensateDegradeMessageCondition
                .builder()
                .maxRetryCount(degradeMessageConfig.getCompensateMaxRetryCount())
                .build();
        compensateAlert(condition);
    }

    @Override
    public void compensate(CompensateDegradeMessageCondition condition) {

        if (condition == null) {
            return;
        }

        // 矫正实际的时间范围
        Pair<Date, Date> actualTimeRange = correctActualTimeRange(condition);
        if (Objects.isNull(actualTimeRange) || actualTimeRange.getKey().after(condition.getStartCreateTime())) {
            return;
        }

        Integer stepMinutes = Optional.ofNullable(condition.getStepMinutes()).orElse(DEFAULT_STEP_MINUTES);
        Date currentTime = actualTimeRange.getKey();

        int count = 0;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < MAX_CIRCULATE_LOOP_COUNT; i++) {

            if (currentTime.after(actualTimeRange.getValue())) {
                break;
            }

            Date afterStepMinutes = DateUtils.addMinutes(currentTime, stepMinutes);
            Date endTime = afterStepMinutes.after(actualTimeRange.getValue()) ? actualTimeRange.getValue() : afterStepMinutes;

            Long minId = 0L;
            for (int j = 0; j < MAX_CIRCULATE_LOOP_COUNT; j++) {

                GetDegradeMessageEntityCondition entityCondition = buildGetDegradeMessageEntityCondition(condition, minId, currentTime, endTime);
                List<DegradeMessageEntity> messageEntityList = degradeMessageStorageService.getByCondition(entityCondition);
                if (messageEntityList.isEmpty()) {
                    break;
                }
                for (DegradeMessageEntity degradeMessageEntity : messageEntityList) {
                    doProcessCompensate(degradeMessageEntity);
                }
                count += messageEntityList.size();
                minId = messageEntityList.get(messageEntityList.size() - 1).getId();
            }
            currentTime = afterStepMinutes;
        }

        log.info("[DegradeMessageCompensateService#compensate] {},{} degrade-exe-count:{},costTime:{},",
                DateUtils.format(actualTimeRange.getKey(), DateUtils.DEFAULT_DATE_FORMAT),
                DateUtils.format(actualTimeRange.getValue(), DateUtils.DEFAULT_DATE_FORMAT),
                count,
                System.currentTimeMillis() - startTime
        );
    }

    private Pair<Date, Date> correctActualTimeRange(CompensateDegradeMessageCondition condition) {

        // 指定MsgKey 不指定时间范围时
        if (Objects.isNull(condition.getStartCreateTime())
                && Objects.isNull(condition.getEndCreateTime())
                && Objects.nonNull(condition.getMsgKeyList())
                && !condition.getMsgKeyList().isEmpty()) {
            Pair<Date, Date> createTimeRange = degradeMessageStorageService.getCreateTimeRange(condition.getMsgKeyList());
            if (Objects.isNull(createTimeRange)) {
                return null;
            }
            // 向前后各拨动一分钟，防止时间范围遗漏
            return Pair.of(DateUtils.addMinutes(createTimeRange.getKey(), -1), DateUtils.addMinutes(createTimeRange.getValue(), 1));
        }

        Pair<Date, Date> createTimeRange = degradeMessageStorageService.getCreateTimeRange();
        if (Objects.isNull(createTimeRange)) {
            return null;
        }

        Date startCreateTime = createTimeRange.getKey();
        if (Objects.nonNull(condition.getStartCreateTime())) {
            // 取用户输入的和实际的数据中的最大值开始时间。
            startCreateTime = startCreateTime.before(condition.getStartCreateTime()) ? condition.getStartCreateTime() : startCreateTime;
        }

        Date endCreateTime = createTimeRange.getValue();
        if (Objects.nonNull(condition.getEndCreateTime())) {
            // 取用户输入的和实际的数据中的最小值结束时间。
            endCreateTime = endCreateTime.after(condition.getEndCreateTime()) ? condition.getEndCreateTime() : DateUtils.addMinutes(endCreateTime, 1);
        }

        return Pair.of(startCreateTime, endCreateTime);
    }

    private GetDegradeMessageEntityCondition buildGetDegradeMessageEntityCondition(CompensateDegradeMessageCondition condition, Long minId, Date startTime, Date endTime) {

        GetDegradeMessageEntityCondition entityCondition = new GetDegradeMessageEntityCondition();
        entityCondition.setMaxRetryCount(degradeMessageConfig.getCompensateMaxRetryCount())
                .setStartCreateTime(startTime)
                .setEndCreateTime(endTime)
                .setMinId(minId)
                .setIpList(condition.getIpList())
                .setResourceList(condition.getResourceList())
                .setMaxRetryCount(Optional.ofNullable(degradeMessageConfig.getCompensateMaxRetryCount()).orElse(DEFAULT_MAX_RETRY_COUNT))
                .setMsgKeyList(condition.getMsgKeyList());
        return entityCondition;
    }

    @Override
    public void compensateAlert(CompensateDegradeMessageCondition degradeCondition) {

        if (degradeCondition == null) {
            return;
        }

        GetDegradeMessageEntityCondition entityCondition = buildGetDegradeMessageEntityCondition(degradeCondition, null, degradeCondition.getStartCreateTime(), degradeCondition.getEndCreateTime());
        List<DegradeMessageSummaryResult> degradeMessageSummaryResultList = degradeMessageStorageService.countByCondition(entityCondition);
        if (CollectionUtils.isEmpty(degradeMessageSummaryResultList)) {
            return;
        }

        StringJoiner alertItem = new StringJoiner("\n");
        for (DegradeMessageSummaryResult degradeMessageSummaryResult : degradeMessageSummaryResultList) {
            alertItem.add(String.format(ALERT_ITEM_TEMPLATE, degradeMessageSummaryResult.getResource(), degradeMessageSummaryResult.getCount()));
        }

        int total = degradeMessageSummaryResultList.stream().mapToInt(DegradeMessageSummaryResult::getCount).sum();
        String alertContent = String.format(ALERT_TEMPLATE, degradeCondition.getMaxRetryCount(), total, alertItem);
        degradeAlert.alert(alertContent);
    }
}
