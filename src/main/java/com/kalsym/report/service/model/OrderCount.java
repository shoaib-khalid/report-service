package com.kalsym.report.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderCount {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String date;
    int total;
    String completionStatus;
}
