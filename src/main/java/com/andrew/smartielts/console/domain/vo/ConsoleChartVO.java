package com.andrew.smartielts.console.domain.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ConsoleChartVO {

    private String code;
    private String title;
    private String chartType;
    private String dimensionKey;
    private String xKey;
    private String yKey;
    private List<ConsoleChartSeriesVO> series;
    private List<String> indicators;
    private List<?> values;
    private List<?> rows;
    private Map<String, Object> meta;
}
