package com.openquartz.mqdegrade.sender.persist.service;

import com.openquartz.mqdegrade.sender.common.Pair;
import com.openquartz.mqdegrade.sender.core.compensate.condition.CompensateDegradeMessageCondition;
import com.openquartz.mqdegrade.sender.persist.mapper.DegradeMessageSummaryResult;
import com.openquartz.mqdegrade.sender.persist.mapper.GetDegradeMessageEntityCondition;
import com.openquartz.mqdegrade.sender.persist.model.DegradeMessageEntity;

import java.util.Date;
import java.util.List;

/**
 * 降级消息存储服务
 *
 * @author svnee
 */
public interface DegradeMessageStorageService {

    /**
     * 存储消息
     *
     * @param resource 降级资源
     * @param message  消息
     * @param key      key
     */
    void save(String resource, String message, String key);

    /**
     * 获取所有机器发出的消息
     *
     * @param ip            machine ip
     * @param maxRetryCount 最大重试次数
     * @param timeRange     时间范围
     * @param limit         限制条数
     * @return 降级消息
     */
    List<DegradeMessageEntity> getSpecMachineMessages(String ip, Integer maxRetryCount, Pair<Date, Date> timeRange, Integer limit);


    /**
     * 获取所有机器发出的消息
     *
     * @param maxRetryCount 最大重试次数
     * @param timeRange     时间范围
     * @param limit         限制条数
     * @return 降级消息
     */
    List<DegradeMessageEntity> getAllMachineMessages(Integer maxRetryCount, Pair<Date, Date> timeRange, Integer limit);

    /**
     * 根据ID 删除消息
     *
     * @param id id
     */
    void delete(Long id);

    /**
     * 根据id重试失败
     *
     * @param id id
     */
    void retryFail(Long id);

    /**
     * 获取重试时间范围
     *
     * @return 重试时间范围。key:最小时间，value:最大时间
     */
    Pair<Date, Date> getCreateTimeRange();

    /**
     * 获取消息key 的时间单位
     *
     * @param msgKeyList 消息key
     * @return 重试时间范围。key:最小时间，value:最大时间
     */
    Pair<Date, Date> getCreateTimeRange(List<String> msgKeyList);

    /**
     * 根据条件获取降级消息
     *
     * @param condition 条件
     * @return 降级消息
     */
    List<DegradeMessageEntity> getByCondition(GetDegradeMessageEntityCondition condition);

    /**
     * 根据条件汇总信息
     *
     * @param condition 查询条件
     * @return 汇总结果
     */
    List<DegradeMessageSummaryResult> countByCondition(GetDegradeMessageEntityCondition condition);
}

