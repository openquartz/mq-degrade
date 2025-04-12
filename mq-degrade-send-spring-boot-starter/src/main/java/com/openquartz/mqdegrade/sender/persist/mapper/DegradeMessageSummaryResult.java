package com.openquartz.mqdegrade.sender.persist.mapper;

import lombok.Data;

@Data
public class DegradeMessageSummaryResult {

    private String resource;

    private Integer count;

}
