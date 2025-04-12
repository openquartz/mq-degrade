package com.openquartz.mqdegrade.sender.persist.mapper;

import com.openquartz.mqdegrade.sender.common.Pair;
import com.openquartz.mqdegrade.sender.common.utils.StringUtils;
import com.openquartz.mqdegrade.sender.core.compensate.condition.CompensateDegradeMessageCondition;
import com.openquartz.mqdegrade.sender.persist.model.DegradeMessageEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * DegradeMessageEntityMapper
 */
public class DegradeMessageEntityMapper {

    private final JdbcTemplate jdbcTemplate;

    public DegradeMessageEntityMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String INSERT_SQL = "insert into mq_degrade_message_entity (`resource`,`message`,`msg_key`,`context`,`ip_addr`,create_time, retry_count, last_retry_time) values (? ,? ,? ,?, ? ,? ,? ,? )";

    private static final String QUERY_BY_IP_SQL = "select id, `resource`,`message`,`msg_key`,`context`,`ip_addr`,create_time, retry_count, last_retry_time from mq_degrade_message_entity where ip_addr = ? and retry_count < ? and last_retry_time >= ? and last_retry_time < ? order by last_retry_time asc limit ? ";

    private static final String QUERY_BY_TIME_RANGE_SQL = "select id, `resource`,`message`,`msg_key`,`context`,`ip_addr`,create_time, retry_count, last_retry_time from mq_degrade_message_entity where retry_count < ? and last_retry_time >= ? and last_retry_time < ? order by last_retry_time asc limit ? ";

    public void insert(DegradeMessageEntity degradeMessageEntity) {

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        this.jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
            ps.setString(1, degradeMessageEntity.getResource());
            ps.setString(2, Optional.ofNullable(degradeMessageEntity.getMessage()).orElse(StringUtils.EMPTY));
            ps.setString(3, Optional.ofNullable(degradeMessageEntity.getMsgKey()).orElse(StringUtils.EMPTY));
            ps.setString(4, Optional.ofNullable(degradeMessageEntity.getContext()).orElse(StringUtils.EMPTY));
            ps.setString(5, Optional.ofNullable(degradeMessageEntity.getIpAddr()).orElse(StringUtils.EMPTY));
            ps.setDate(6, new java.sql.Date(Optional.ofNullable(degradeMessageEntity.getCreateTime()).orElse(new Date()).getTime()));
            ps.setInt(7, Optional.ofNullable(degradeMessageEntity.getRetryCount()).orElse(0));
            ps.setDate(8, new java.sql.Date(Optional.ofNullable(degradeMessageEntity.getLastRetryTime()).orElse(new Date()).getTime()));
            return ps;
        }, keyHolder);

        long generatedId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        degradeMessageEntity.setId(generatedId);
    }


    public List<DegradeMessageEntity> selectByIpAddr(String ip, Integer maxRetryCount, Pair<Date, Date> timeRange, Integer limit) {
        return jdbcTemplate.query(QUERY_BY_IP_SQL, (rs, row) -> buildDegradeMessageEntity(rs), ip, maxRetryCount, timeRange.getKey(), timeRange.getValue(), limit);
    }

    private DegradeMessageEntity buildDegradeMessageEntity(ResultSet rs) throws SQLException {
        DegradeMessageEntity degradeMessageEntity = new DegradeMessageEntity();
        degradeMessageEntity.setId(rs.getLong("id"));
        degradeMessageEntity.setResource(rs.getString("resource"));
        degradeMessageEntity.setMessage(rs.getString("message"));
        degradeMessageEntity.setMsgKey(rs.getString("msg_key"));
        degradeMessageEntity.setContext(rs.getString("context"));
        degradeMessageEntity.setIpAddr(rs.getString("ip_addr"));
        degradeMessageEntity.setCreateTime(rs.getDate("create_time"));
        degradeMessageEntity.setRetryCount(rs.getInt("retry_count"));
        degradeMessageEntity.setLastRetryTime(rs.getDate("last_retry_time"));
        return degradeMessageEntity;
    }

    public List<DegradeMessageEntity> selectByTimeRange(Integer maxRetryCount, Pair<Date, Date> timeRange, Integer limit) {
        return jdbcTemplate.query(QUERY_BY_TIME_RANGE_SQL, (rs, row) -> buildDegradeMessageEntity(rs), maxRetryCount, timeRange.getKey(), timeRange.getValue(), limit);
    }

    public void deleteById(Long id) {
        jdbcTemplate.update("delete from mq_degrade_message_entity where id = ?", id);
    }

    public void updateRetryCount(Long id) {
        jdbcTemplate.update("update mq_degrade_message_entity set retry_count = retry_count + 1,last_retry_time = now(), where id = ?", id);
    }

    public Pair<Date, Date> selectCreateTimeRange() {
        return jdbcTemplate.queryForObject("select min(create_time) as min_time,max(create_time) as max_time from mq_degrade_message_entity", (rs, row) -> {
            java.sql.Date minCreateTime = rs.getDate("min_time");
            java.sql.Date maxCreateTime = rs.getDate("max_time");
            if (Objects.isNull(minCreateTime) && Objects.isNull(maxCreateTime)) {
                return null;
            }
            return Pair.of(minCreateTime, maxCreateTime);
        });
    }

    public Pair<Date, Date> selectCreateTimeRange(List<String> msgKeyList) {
        String placeholders = String.join(",", Collections.nCopies(msgKeyList.size(), "?"));
        return jdbcTemplate.queryForObject(String.format("select min(create_time) as min_time,max(create_time) as max_time from mq_degrade_message_entity where msg_key in (%s)", placeholders), (rs, row) -> {
            java.sql.Date minCreateTime = rs.getDate("min_time");
            java.sql.Date maxCreateTime = rs.getDate("max_time");
            if (Objects.isNull(minCreateTime) && Objects.isNull(maxCreateTime)) {
                return null;
            }
            return Pair.of(minCreateTime, maxCreateTime);
        }, String.join(",", msgKeyList));
    }

    public List<DegradeMessageEntity> listByCondition(GetDegradeMessageEntityCondition condition) {

        Pair<String, List<Object>> sql2DynamicCondition = buildDynamicCondition(condition);
        
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder
                .append("select id, `resource`, `message`, `msg_key`, `context`, `ip_addr`, create_time, retry_count, last_retry_time ")
                .append("from mq_degrade_message_entity where 1=1 ");
        
        if (!StringUtils.isBlank(sql2DynamicCondition.getKey())) {
            sqlBuilder.append(" AND ").append(sql2DynamicCondition.getKey());
        }
        
        sqlBuilder.append(" order by create_time desc");
        
        // 如果设置了步长，添加限制条件
        if (condition.getStepMinutes() != null) {
            sqlBuilder.append(" limit ?");
            sql2DynamicCondition.getValue().add(condition.getStepMinutes());
        }
        
        String sql = sqlBuilder.toString();
        Object[] params = sql2DynamicCondition.getValue().toArray();
        
        return jdbcTemplate.query(sql, (rs, row) -> buildDegradeMessageEntity(rs), params);
    }

    /**
     * 按照条件构建动态条件。动态拼接返回的sql与占位值
     * @param condition 动态条件
     * @return key: 动态sql条件,value: 占位值
     */
    private Pair<String, List<Object>> buildDynamicCondition(GetDegradeMessageEntityCondition condition) {
        StringBuilder sqlBuilder = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        // 添加重试次数条件
        if (condition.getMaxRetryCount() != null) {
            sqlBuilder.append(" AND retry_count < ?");
            params.add(condition.getMaxRetryCount());
        }
        
        // 添加创建时间范围条件
        if (condition.getStartCreateTime() != null) {
            sqlBuilder.append(" AND create_time >= ?");
            params.add(condition.getStartCreateTime());
        }
        
        if (condition.getEndCreateTime() != null) {
            sqlBuilder.append(" AND create_time <= ?");
            params.add(condition.getEndCreateTime());
        }
        
        // 添加资源列表条件
        if (condition.getResourceList() != null && !condition.getResourceList().isEmpty()) {
            sqlBuilder.append(" AND resource IN (")
                    .append(String.join(",", Collections.nCopies(condition.getResourceList().size(), "?")))
                    .append(")");
            params.addAll(condition.getResourceList());
        }
        
        // 添加消息key列表条件
        if (condition.getMsgKeyList() != null && !condition.getMsgKeyList().isEmpty()) {
            sqlBuilder.append(" AND msg_key IN (")
                    .append(String.join(",", Collections.nCopies(condition.getMsgKeyList().size(), "?")))
                    .append(")");
            params.addAll(condition.getMsgKeyList());
        }
        
        // 添加IP地址列表条件
        if (condition.getIpList() != null && !condition.getIpList().isEmpty()) {
            sqlBuilder.append(" AND ip_addr IN (")
                    .append(String.join(",", Collections.nCopies(condition.getIpList().size(), "?")))
                    .append(")");
            params.addAll(condition.getIpList());
        }
        if (condition.getMinId() != null && condition.getMinId() > 0){
            sqlBuilder.append(" AND id > ?");
        }
        
        // 如果没有添加任何条件，移除开头的 AND
        String sql = sqlBuilder.toString();
        if (sql.startsWith(" AND ")) {
            sql = sql.substring(5);
        }
        
        return Pair.of(sql, params);
    }

    public List<DegradeMessageSummaryResult> countByCondition(GetDegradeMessageEntityCondition condition) {

        Pair<String, List<Object>> sql2DynamicCondition = buildDynamicCondition(condition);
        
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("select `resource`, count(1) as count ")
                .append("from mq_degrade_message_entity where 1=1 ");
        
        if (!StringUtils.isBlank(sql2DynamicCondition.getKey())) {
            sqlBuilder.append(" AND ").append(sql2DynamicCondition.getKey());
        }
        
        sqlBuilder.append(" group by `resource` order by count desc");
        
        String sql = sqlBuilder.toString();
        Object[] params = sql2DynamicCondition.getValue().toArray();
        
        return jdbcTemplate.query(sql, (rs, row) -> {
            DegradeMessageSummaryResult result = new DegradeMessageSummaryResult();
            result.setResource(rs.getString("resource"));
            result.setCount(rs.getInt("count"));
            return result;
        }, params);
    }
}
