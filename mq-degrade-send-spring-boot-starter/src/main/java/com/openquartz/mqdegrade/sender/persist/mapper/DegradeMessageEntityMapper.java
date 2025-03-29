package com.openquartz.mqdegrade.sender.persist.mapper;

import com.openquartz.mqdegrade.sender.common.utils.StringUtils;
import com.openquartz.mqdegrade.sender.persist.model.DegradeMessageEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.PreparedStatement;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * DegradeMessageEntityMapper
 */
public class DegradeMessageEntityMapper {

    private final JdbcTemplate jdbcTemplate;

    public DegradeMessageEntityMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String INSERT_SQL = "insert into mq_degrade_message_entity (`resource`,`message`,`msg_key`,`context`,`ip_addr`,create_time, retry_count, last_retry_time) values (? ,? ,? ,?, ? ,? ,? ,? )";

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




}
