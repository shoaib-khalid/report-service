package com.kalsym.report.service.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

@Getter
@Setter
public class DashboardViewTotal {

    public Set<OrderCount> dailySales;
    public Set<OrderCount> weeklySales;
    public Set<OrderCount> monthlySales;
    public Set<OrderCount> totalSales;
}
