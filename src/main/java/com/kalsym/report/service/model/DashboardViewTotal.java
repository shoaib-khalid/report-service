package com.kalsym.report.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class DashboardViewTotal {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Set<OrderCount> dailySales;
    public Set<OrderCount> weeklySales;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Set<OrderCount> monthlySales;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Set<OrderCount> totalSales;
}
