package com.kalsym.report.service.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Sales {
    private List<DetailStoreSales> sales;
    private Date date;


}
