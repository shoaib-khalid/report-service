package com.kalsym.report.service.model;

import io.swagger.models.auth.In;

import java.util.List;
import java.util.Set;

public class Response {
    public static class DailySalesReportResponse {
        public String date;
        public Integer totalTrx;
        public Float totalAmount;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Integer getTotalTrx() {
            return totalTrx;
        }

        public void setTotalTrx(Integer totalTrx) {
            this.totalTrx = totalTrx;
        }

        public Float getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Float totalAmount) {
            this.totalAmount = totalAmount;
        }

    }

    public static class WeeklySalesReportResponse {
        public Integer weekNo;
        public String weekLabel;
        public Integer totalTrx;
        public Float totalAmount;

        public Integer getWeekNo() {
            return weekNo;
        }

        public void setWeekNo(Integer weekNo) {
            this.weekNo = weekNo;
        }

        public String getWeekLabel() {
            return weekLabel;
        }

        public void setWeekLabel(String weekLabel) {
            this.weekLabel = weekLabel;
        }

        public Integer getTotalTrx() {
            return totalTrx;
        }

        public void setTotalTrx(Integer totalTrx) {
            this.totalTrx = totalTrx;
        }

        public Float getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Float totalAmount) {
            this.totalAmount = totalAmount;
        }
    }

    public static class MonthlySalesReportResponse {
        public String monthNo;
        public String monthLabel;
        public Integer totalTrx;
        public Float totalAmount;

        public String getMonthNo() {
            return monthNo;
        }

        public void setMonthNo(String monthNo) {
            this.monthNo = monthNo;
        }

        public String getMonthLabel() {
            return monthLabel;
        }

        public void setMonthLabel(String monthLabel) {
            this.monthLabel = monthLabel;
        }

        public Integer getTotalTrx() {
            return totalTrx;
        }

        public void setTotalTrx(Integer totalTrx) {
            this.totalTrx = totalTrx;
        }

        public Float getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(Float totalAmount) {
            this.totalAmount = totalAmount;
        }
    }


    public static class ProductInventoryResponse {
        public String productName;
        public Integer totalStock;

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getTotalStock() {
            return totalStock;
        }

        public void setTotalStock(Integer totalStock) {
            this.totalStock = totalStock;
        }
    }

    public static class DailyTopProduct{
        private String productName;
        private Integer totalTransaction;
        private Integer rank;

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getTotalTransaction() {
            return totalTransaction;
        }

        public void setTotalTransaction(Integer totalTransaction) {
            this.totalTransaction = totalTransaction;
        }

        public Integer getRank() {
            return rank;
        }

        public void setRank(Integer rank) {
            this.rank = rank;
        }
    }

    public static class DailyTopProductResponse{
        private String date;
        private List<DailyTopProduct> topProduct;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public List<DailyTopProduct> getTopProduct() {
            return topProduct;
        }

        public void setTopProduct(List<DailyTopProduct> topProduct) {
            this.topProduct = topProduct;
        }
    }


    public static class WeeklyTopProduct{
        private String productName;
        private Integer totalTransaction;
        private Integer rank;

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getTotalTransaction() {
            return totalTransaction;
        }

        public void setTotalTransaction(Integer totalTransaction) {
            this.totalTransaction = totalTransaction;
        }

        public Integer getRank() {
            return rank;
        }

        public void setRank(Integer rank) {
            this.rank = rank;
        }
    }

    public static class WeeklyTopProductResponse{
        private Integer weekNo;
        private String week;
        private List<WeeklyTopProduct> topProduct;

        public Integer getWeekNo() {
            return weekNo;
        }

        public void setWeekNo(Integer weekNo) {
            this.weekNo = weekNo;
        }

        public String getWeek() {
            return week;
        }

        public void setWeek(String week) {
            this.week = week;
        }

        public List<WeeklyTopProduct> getTopProduct() {
            return topProduct;
        }

        public void setTopProduct(List<WeeklyTopProduct> topProduct) {
            this.topProduct = topProduct;
        }
    }



    public static class MonthlyTopProduct{
        private String productName;
        private Integer totalTransaction;
        private Integer rank;

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public Integer getTotalTransaction() {
            return totalTransaction;
        }

        public void setTotalTransaction(Integer totalTransaction) {
            this.totalTransaction = totalTransaction;
        }

        public Integer getRank() {
            return rank;
        }

        public void setRank(Integer rank) {
            this.rank = rank;
        }

        @Override
        public String toString() {
            return "{" +
                    "productName:'" + productName + '\'' +
                    ", totalTransaction:" + totalTransaction +
                    ", rank:" + rank +
                    '}';
        }
    }

    public static class MonthlyTopProductResponse{
        private String monthNo;
        private String monthLabel;
        private List<MonthlyTopProduct> topProduct;

        public String getMonthNo() {
            return monthNo;
        }

        public void setMonthNo(String monthNo) {
            this.monthNo = monthNo;
        }

        public String getMonthLabel() {
            return monthLabel;
        }

        public void setMonthLabel(String monthLabel) {
            this.monthLabel = monthLabel;
        }

        public List<MonthlyTopProduct> getTopProduct() {
            return topProduct;
        }

        public void setTopProduct(List<MonthlyTopProduct> topProduct) {
            this.topProduct = topProduct;
        }

        @Override
        public String toString() {
            return "{" +
                    "monthNo:'" + monthNo + '\'' +
                    ", monthLabel:'" + monthLabel + '\'' +
                    ", topProduct:" + topProduct +
                    '}';
        }
    }

    public static class MonthlyStatement{

        private String month;
        private String monthLabel;
        private String type;
        private int totalTrx;
        private float totalAmount;

        public String getMonth() {
            return month;
        }

        public void setMonth(String month) {
            this.month = month;
        }

        public String getMonthLabel() {
            return monthLabel;
        }

        public void setMonthLabel(String monthLabel) {
            this.monthLabel = monthLabel;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getTotalTrx() {
            return totalTrx;
        }

        public void setTotalTrx(int totalTrx) {
            this.totalTrx = totalTrx;
        }

        public float getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(float totalAmount) {
            this.totalAmount = totalAmount;
        }
    }

    public static class WeeklyStatement{

        private int weekNo;
        private String weekLabel;
        private String type;
        private int totalTrx;
        private float totalAmount;

        public int getWeekNo() {
            return weekNo;
        }

        public void setWeekNo(int weekNo) {
            this.weekNo = weekNo;
        }

        public String getWeekLabel() {
            return weekLabel;
        }

        public void setWeekLabel(String weekLabel) {
            this.weekLabel = weekLabel;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getTotalTrx() {
            return totalTrx;
        }

        public void setTotalTrx(int totalTrx) {
            this.totalTrx = totalTrx;
        }

        public float getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(float totalAmount) {
            this.totalAmount = totalAmount;
        }
    }
}
