package com.kalsym.report.service.controller;


import com.kalsym.report.service.model.Order;
import com.kalsym.report.service.model.Product;
import com.kalsym.report.service.model.ProductInventory;
import com.kalsym.report.service.model.Response;
import com.kalsym.report.service.model.repository.OrderItemRepository;
import com.kalsym.report.service.model.repository.OrderRepository;
import com.kalsym.report.service.model.repository.ProductInventoryRepository;
import com.kalsym.report.service.model.repository.ProductRepository;
import com.kalsym.report.service.utils.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping(path = "/")
public class ReportController {

    @Value("${services.user-service.session_details:not-known}")
    String userServiceUrl;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ProductInventoryRepository productInventoryRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @PostMapping(value = "store/{storeId}/report/dailySales", name = "store-report-dailySale-get")
    public ResponseEntity<HttpResponse> dailySales(HttpServletRequest request, @RequestBody SalesReport dailySalesReport, @PathVariable("storeId") String storeId) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        long diffInMillis = myFormat.parse(dailySalesReport.getEndDate()).getTime() - myFormat.parse(dailySalesReport.getStartDate()).getTime();
        long diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        Set<Response.DailySalesReportResponse> reportResponseList = new HashSet<>();
        for (int i = 0; i <= diff; i++) {
            Calendar startDate = Calendar.getInstance();
            try {
                startDate.setTime(myFormat.parse(dailySalesReport.getStartDate().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            startDate.add(Calendar.DAY_OF_MONTH, i);
            String stDate = myFormat.format(startDate.getTime()) + " 00:00:00";
            String endDate = myFormat.format(startDate.getTime()) + " 23:59:59";

            List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBeforeAndPaymentStatus(storeId, stDate, endDate, "SUCCESS");
//            List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBefore(storeId, stDate, endDate);
            float totalValue = 0.00f;
            Response.DailySalesReportResponse data = new Response.DailySalesReportResponse();

            data.setDate(myFormat.format(startDate.getTime()));
            data.setTotalTrx(orders.size());
            for (Order order : orders) {
                totalValue = totalValue + order.getTotal();
            }
            data.setTotalAmount(totalValue);
            reportResponseList.add(data);
        }
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(reportResponseList);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }


    @PostMapping(value = "store/{storeId}/report/weeklySales", name = "store-report-weeklySale-get")
    public ResponseEntity<HttpResponse> weeklySales(HttpServletRequest request, @RequestBody SalesReport dailySalesReport, @PathVariable("storeId") String storeId) throws Exception {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        Date date = new Date();

        int weekNo = dailySalesReport.getEndWeekNo() - dailySalesReport.getStartWeekNo();
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        Set<Response.WeeklySalesReportResponse> reportResponseList = new HashSet<>();

        for (int i = 0; i <= weekNo; i++) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int year = cal.get(Calendar.YEAR);

            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(Calendar.YEAR, year);

            calendar.set(Calendar.WEEK_OF_YEAR, dailySalesReport.startWeekNo + i);
            calendar.set(Calendar.DAY_OF_WEEK, 1);
            // Now get the first day of week.
            Date sDate = calendar.getTime();
            System.out.println("Start Week Date :" + sDate);

            calendar.set(Calendar.WEEK_OF_YEAR, dailySalesReport.startWeekNo + i);
            calendar.set(Calendar.DAY_OF_WEEK, 7);
            Date eDate = calendar.getTime();
            System.out.println("End week Date :" + eDate);

            String stDate = myFormat.format(sDate.getTime()) + " 00:00:00";
            String endDate = myFormat.format(eDate.getTime()) + " 23:59:59";
//            List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBefore(storeId, stDate, endDate);
            List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBeforeAndPaymentStatus(storeId, stDate, endDate, "SUCCESS");
            float totalValue = 0.00f;
            Response.WeeklySalesReportResponse weeklyReport = new Response.WeeklySalesReportResponse();
            weeklyReport.setWeekNo(dailySalesReport.startWeekNo + i);
            weeklyReport.setTotalTrx(orders.size());
            weeklyReport.setWeekLabel(f.format(sDate.getTime()) + " to " + f.format(eDate.getTime()));
            for (Order order : orders) {
                totalValue = totalValue + order.getTotal();
            }
            weeklyReport.setTotalAmount(totalValue);
            reportResponseList.add(weeklyReport);

        }


        response.setSuccessStatus(HttpStatus.OK);
        response.setData(reportResponseList);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @PostMapping(value = "store/{storeId}/report/monthlySales", name = "store-report-monthlySale-get")
    public ResponseEntity<HttpResponse> monthlySales(HttpServletRequest request, @RequestBody SalesReport dailySalesReport, @PathVariable("storeId") String storeId) throws IOException {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        String[] m1 = dailySalesReport.getStartMonth().split("-");
        String[] m2 = dailySalesReport.getEndMonth().split("-");
        int month1 = Integer.parseInt(m1[0]);
        int month2 = Integer.parseInt(m2[0]);
        int year1 = Integer.parseInt(m1[1]);
        Integer year2 = Integer.parseInt(m2[1]);
        System.out.println("First Day of month: " + month1 + " First year " + year1 + " : Last month: " + month2 + " :last year :" + year2);

        Set<Response.MonthlySalesReportResponse> reportResponseList = new HashSet<>();

        int totalMonth;
        if (year2.equals(year1)) {
            totalMonth = month2 - month1;

            for (int i = 0; i <= totalMonth; i++) {
                int m = month1 + i - 1;

                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(Calendar.YEAR, year1);
                cal.set(Calendar.MONTH, m);
                cal.set(Calendar.DAY_OF_MONTH, 1);

                int numOfDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                String stDate = myFormat.format(cal.getTime()) + " 00:00:00";
                cal.add(Calendar.DAY_OF_MONTH, numOfDaysInMonth - 1);

                String endDate = myFormat.format(cal.getTime()) + " 23:59:59";

//            List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, endDate, storeId, "SUCCESS", 5);
                List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBeforeAndPaymentStatus(storeId, stDate, endDate, "SUCCESS");
//                List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBefore(storeId, stDate, endDate);
                float totalValue = 0.00f;
                Response.MonthlySalesReportResponse monthlyReport = new Response.MonthlySalesReportResponse();
                monthlyReport.setMonthNo(new SimpleDateFormat("MM-yyyy").format(cal.getTime()));
                monthlyReport.setMonthLabel(new SimpleDateFormat("MMM-yyyy").format(cal.getTime()));
                monthlyReport.setTotalTrx(orders.size());
                for (Order order : orders) {
                    totalValue = totalValue + order.getTotal();
                }
                monthlyReport.setTotalAmount(totalValue);
                reportResponseList.add(monthlyReport);
            }
        } else {
            totalMonth = (12 + month2) - month1;

            for (int i = 0; i <= totalMonth; i++) {
                int m = month1 + i - 1;
                int year = year1;
                if (m > 12) {
                    m = month1 - 12;
                    year = year + 1;
                }
                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, m);
                cal.set(Calendar.DAY_OF_MONTH, 1);

                int numOfDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                String stDate = myFormat.format(cal.getTime()) + " 00:00:00";
                cal.add(Calendar.DAY_OF_MONTH, numOfDaysInMonth - 1);

                String endDate = myFormat.format(cal.getTime()) + " 23:59:59";

//            List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, endDate, storeId, "SUCCESS", 5);
//                List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBefore(storeId, stDate, endDate);
                List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBeforeAndPaymentStatus(storeId, stDate, endDate, "SUCCESS");
                float totalValue = 0.00f;
                Response.MonthlySalesReportResponse monthlyReport = new Response.MonthlySalesReportResponse();
                monthlyReport.setMonthNo(new SimpleDateFormat("MM-yyyy").format(cal.getTime()));
                monthlyReport.setMonthLabel(new SimpleDateFormat("MMM-yyyy").format(cal.getTime()));
                monthlyReport.setTotalTrx(orders.size());
                for (Order order : orders) {
                    totalValue = totalValue + order.getTotal();
                }
                monthlyReport.setTotalAmount(totalValue);
                reportResponseList.add(monthlyReport);

            }
        }

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(reportResponseList);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    @PostMapping(value = "store/{storeId}/report/dailyTopProducts", name = "store-report-dailyTopProducts-get")
    public ResponseEntity<HttpResponse> dailyTopProducts(HttpServletRequest request, @RequestBody TopProductSale topProductSale, @PathVariable("storeId") String storeId) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());


        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        long diffInMillis = myFormat.parse(topProductSale.getEndDate()).getTime() - myFormat.parse(topProductSale.getStartDate()).getTime();
        long diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        Set<Response.DailyTopProductResponse> lists = new HashSet<>();
        for (int i = 0; i <= diff; i++) {
            Response.DailyTopProductResponse list = new Response.DailyTopProductResponse();
            List<Response.DailyTopProduct> reportResponseList = new ArrayList<>();

            Calendar startDate = Calendar.getInstance();
            try {
                startDate.setTime(myFormat.parse(topProductSale.getStartDate().toString()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            startDate.add(Calendar.DAY_OF_MONTH, i);
            String stDate = myFormat.format(startDate.getTime()) + " 00:00:00";
            String endDate = myFormat.format(startDate.getTime()) + " 23:59:59";

            List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, endDate, storeId, "SUCCESS", 5);
//            List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, endDate, 5);

            for (int k = 0; k < objects.size(); k++) {
                Response.DailyTopProduct product = new Response.DailyTopProduct();
                String productCode = objects.get(k)[0].toString();
                Product product1 = productRepository.getOne(productCode);
                String totalValue = objects.get(k)[1].toString();
                product.setProductName(product1.getName());
                product.setTotalTransaction(Integer.parseInt(totalValue));
                product.setRank(k + 1);
                reportResponseList.add(product);
            }
            list.setDate(myFormat.format(startDate.getTime()));
            list.setTopProduct(reportResponseList);
            lists.add(list);
        }

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(lists);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(value = "store/{storeId}/report/weeklyTopProducts", name = "store-report-weeklyTopProducts-get")
    public ResponseEntity<HttpResponse> weeklyTopProducts(HttpServletRequest request, @RequestBody TopProductSale topProductSale, @PathVariable("storeId") String storeId) throws Exception {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        Date date = new Date();

        int weekNo = topProductSale.getEndWeekNo() - topProductSale.getStartWeekNo();
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        Set<Response.WeeklyTopProductResponse> lists = new HashSet<>();

        for (int i = 0; i <= weekNo; i++) {

            Response.WeeklyTopProductResponse list = new Response.WeeklyTopProductResponse();
            List<Response.WeeklyTopProduct> reportResponseList = new ArrayList<>();
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int year = cal.get(Calendar.YEAR);

            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(Calendar.YEAR, year);

            calendar.set(Calendar.WEEK_OF_YEAR, topProductSale.startWeekNo + i);
            calendar.set(Calendar.DAY_OF_WEEK, 1);
            // Now get the first day of week.
            Date sDate = calendar.getTime();
            System.out.println("Start Week Date :" + sDate);

            calendar.set(Calendar.WEEK_OF_YEAR, topProductSale.startWeekNo + i);
            calendar.set(Calendar.DAY_OF_WEEK, 7);
            Date eDate = calendar.getTime();
            System.out.println("End week Date :" + eDate);

            String stDate = myFormat.format(sDate.getTime()) + " 00:00:00";
            String endDate = myFormat.format(eDate.getTime()) + " 23:59:59";
            List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, endDate, storeId, "SUCCESS", 5);
//            List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, endDate, 5);

            for (int k = 0; k < objects.size(); k++) {
                Response.WeeklyTopProduct product = new Response.WeeklyTopProduct();
                String productCode = objects.get(k)[0].toString();
                Product product1 = productRepository.getOne(productCode);
                String totalValue = objects.get(k)[1].toString();
                product.setProductName(product1.getName());
                product.setTotalTransaction(Integer.parseInt(totalValue));
                product.setRank(k + 1);
                reportResponseList.add(product);
            }
            list.setWeekNo(topProductSale.startWeekNo + i);
            list.setWeek(f.format(sDate.getTime()) + " to " + f.format(eDate.getTime()));
            list.setTopProduct(reportResponseList);
            lists.add(list);
        }

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(lists);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(value = "store/{storeId}/report/monthlyTopProducts", name = "store-report-monthlyTopProducts-get")
    public ResponseEntity<HttpResponse> monthlyTopProducts(HttpServletRequest request, @RequestBody TopProductSale topProductSale, @PathVariable("storeId") String storeId) throws Exception {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        String[] m1 = topProductSale.getStartMonth().split("-");
        String[] m2 = topProductSale.getEndMonth().split("-");
        int month1 = Integer.parseInt(m1[0]);
        int month2 = Integer.parseInt(m2[0]);
        int year1 = Integer.parseInt(m1[1]);
        Integer year2 = Integer.parseInt(m2[1]);
        System.out.println("First Day of month: " + month1 + " First year " + year1 + " : Last month: " + month2 + " :last year :" + year2);

        List<Response.MonthlyTopProductResponse> lists = new ArrayList<>();
        int totalMonth;
        if (year2.equals(year1)) {
            totalMonth = month2 - month1;

            for (int i = 0; i <= totalMonth; i++) {
                List<Response.MonthlyTopProduct> reportResponseList = new ArrayList<>();
                Response.MonthlyTopProductResponse list = new Response.MonthlyTopProductResponse();
                int m = month1 + i - 1;

                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(Calendar.YEAR, year1);
                cal.set(Calendar.MONTH, m);
                cal.set(Calendar.DAY_OF_MONTH, 1);

                int numOfDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                String stDate = myFormat.format(cal.getTime()) + " 00:00:00";
                cal.add(Calendar.DAY_OF_MONTH, numOfDaysInMonth - 1);

                String endDate = myFormat.format(cal.getTime()) + " 23:59:59";

                List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, endDate, storeId, "SUCCESS", 5);
//                List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, endDate, 5);

                for (int k = 0; k < objects.size(); k++) {
                    Response.MonthlyTopProduct product = new Response.MonthlyTopProduct();
                    String productCode = objects.get(k)[0].toString();
                    Product product1 = productRepository.getOne(productCode);
                    String totalValue = objects.get(k)[1].toString();
                    product.setProductName(product1.getName());
                    product.setTotalTransaction(Integer.parseInt(totalValue));
                    product.setRank(k + 1);
                    reportResponseList.add(product);

                }
                System.out.println("Day: " + reportResponseList.toString());


                list.setMonthNo(new SimpleDateFormat("MM-yyyy").format(cal.getTime()));
                list.setMonthLabel(new SimpleDateFormat("MMM-yyyy").format(cal.getTime()));
                list.setTopProduct(reportResponseList);
                lists.add(list);

            }
        } else {
            totalMonth = (12 + month2) - month1;

            for (int i = 0; i <= totalMonth; i++) {
                List<Response.MonthlyTopProduct> reportResponseList = new ArrayList<>();
                Response.MonthlyTopProductResponse list = new Response.MonthlyTopProductResponse();
                int m = month1 + i - 1;
                int year = year1;
                if (m > 12) {
                    m = month1 - 12;
                    year = year + 1;
                }
                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, m);
                cal.set(Calendar.DAY_OF_MONTH, 1);

                int numOfDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                String stDate = myFormat.format(cal.getTime()) + " 00:00:00";
                cal.add(Calendar.DAY_OF_MONTH, numOfDaysInMonth - 1);

                String endDate = myFormat.format(cal.getTime()) + " 23:59:59";

                List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, endDate, storeId, "SUCCESS", 5);
//                List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, endDate, 5);

                for (int k = 0; k < objects.size(); k++) {
                    Response.MonthlyTopProduct product = new Response.MonthlyTopProduct();
                    String productCode = objects.get(k)[0].toString();
                    Product product1 = productRepository.getOne(productCode);
                    String totalValue = objects.get(k)[1].toString();
                    product.setProductName(product1.getName());
                    product.setTotalTransaction(Integer.parseInt(totalValue));
                    product.setRank(k + 1);
                    reportResponseList.add(product);

                }

                list.setMonthNo(new SimpleDateFormat("MM-yyyy").format(cal.getTime()));
                list.setMonthLabel(new SimpleDateFormat("MMM-yyyy").format(cal.getTime()));
                list.setTopProduct(reportResponseList);
                lists.add(list);

            }
        }

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(lists);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(value = "productInventory", name = "store-report-productInventory-get")
    public ResponseEntity<HttpResponse> productInventory(HttpServletRequest request, @RequestBody InventoryRequest inventoryRequest) throws IOException {
        HttpResponse response = new HttpResponse(request.getRequestURI());
        List<Product> products = productRepository.findAllByStoreId(inventoryRequest.getStoreId());
        Set<Response.ProductInventoryResponse> inventoryResponse = new HashSet<>();
        for (Product product : products) {
            List<ProductInventory> inventories = productInventoryRepository.findAllByProductIdAndQuantityGreaterThanEqualAndQuantityLessThanEqual(product.getId(), inventoryRequest.getMinTotal(), inventoryRequest.getMaxTotal());
            Response.ProductInventoryResponse res = new Response.ProductInventoryResponse();
            for (ProductInventory in : inventories) {
                res.setProductName(in.getProduct().getName());
                res.setTotalStock(in.getQuantity());
                inventoryResponse.add(res);
            }
        }
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(inventoryResponse);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(value = "store/{storeId}/monthlyStatement")
    public ResponseEntity<HttpResponse> monthlyStatement(HttpServletRequest request, @RequestBody Statement statement, @PathVariable("storeId") String storeId) throws IOException {
        //TODO: Need to add Order Refund Value.
        HttpResponse response = new HttpResponse(request.getRequestURI());
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        String[] m1 = statement.getStartMonth().split("-");
        String[] m2 = statement.getEndMonth().split("-");
        int month1 = Integer.parseInt(m1[0]);
        int month2 = Integer.parseInt(m2[0]);
        int year1 = Integer.parseInt(m1[1]);
        Integer year2 = Integer.parseInt(m2[1]);
        System.out.println("First Day of month: " + month1 + " First year " + year1 + " : Last month: " + month2 + " :last year :" + year2);

        List<Response.MonthlyStatement> lists = new ArrayList<>();
        int totalMonth;
        if (year2.equals(year1)) {
            totalMonth = month2 - month1;

            for (int i = 0; i <= totalMonth; i++) {

                int m = month1 + i - 1;

                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(Calendar.YEAR, year1);
                cal.set(Calendar.MONTH, m);
                cal.set(Calendar.DAY_OF_MONTH, 1);

                int numOfDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                String stDate = myFormat.format(cal.getTime()) + " 00:00:00";
                cal.add(Calendar.DAY_OF_MONTH, numOfDaysInMonth - 1);

                String endDate = myFormat.format(cal.getTime()) + " 23:59:59";

                List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBeforeAndPaymentStatus(storeId, stDate, endDate, "Completed");

                float totalValue = 0.00f;
                for (Order o : orders) {
                    if (o.getPaymentStatus().equals("Completed")) {
                        totalValue = totalValue + o.getTotal();
                    }
                }
                Response.MonthlyStatement monthlyStatement = new Response.MonthlyStatement();
                monthlyStatement.setMonth(new SimpleDateFormat("MM-yyyy").format(cal.getTime()));
                monthlyStatement.setMonthLabel(new SimpleDateFormat("MMM-yyyy").format(cal.getTime()));
                monthlyStatement.setTotalTrx(orders.size());

                monthlyStatement.setTotalAmount(totalValue);
                monthlyStatement.setType("sales");
                lists.add(monthlyStatement);

            }
        } else {
            totalMonth = (12 + month2) - month1;

            for (int i = 0; i <= totalMonth; i++) {
                int m = month1 + i - 1;
                int year = year1;
                if (m > 12) {
                    m = month1 - 12;
                    year = year + 1;
                }
                Calendar cal = Calendar.getInstance();
                cal.clear();
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, m);
                cal.set(Calendar.DAY_OF_MONTH, 1);

                int numOfDaysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

                String stDate = myFormat.format(cal.getTime()) + " 00:00:00";
                cal.add(Calendar.DAY_OF_MONTH, numOfDaysInMonth - 1);

                String endDate = myFormat.format(cal.getTime()) + " 23:59:59";

                List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBeforeAndPaymentStatus(storeId, stDate, endDate, "Completed");

                float totalValue = 0.00f;
                for (Order o : orders) {
                    if (o.getPaymentStatus().equals("Completed")) {
                        totalValue = totalValue + o.getTotal();
                    }
                }
                Response.MonthlyStatement monthlyStatement = new Response.MonthlyStatement();
                monthlyStatement.setMonth(new SimpleDateFormat("MM-yyyy").format(cal.getTime()));
                monthlyStatement.setMonthLabel(new SimpleDateFormat("MMM-yyyy").format(cal.getTime()));
                monthlyStatement.setTotalTrx(orders.size());

                monthlyStatement.setTotalAmount(totalValue);
                monthlyStatement.setType("sales");
                lists.add(monthlyStatement);

            }

        }

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(lists);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(value = "store/{storeId}/weeklyStatement")
    public ResponseEntity<HttpResponse> weeklyStatement(HttpServletRequest request, @RequestBody Statement Statement, @PathVariable("storeId") String storeId) throws IOException {
        //TODO: Need to add Order Refund Value.

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Date date = new Date();
        int weekNo = Statement.getEndWeekNo() - Statement.getStartWeekNo();
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
        Set<Response.WeeklyStatement> reportResponseList = new HashSet<>();

        for (int i = 0; i <= weekNo; i++) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int year = cal.get(Calendar.YEAR);

            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(Calendar.YEAR, year);

            calendar.set(Calendar.WEEK_OF_YEAR, Statement.startWeekNo + i);
            calendar.set(Calendar.DAY_OF_WEEK, 1);
            // Now get the first day of week.
            Date sDate = calendar.getTime();
            System.out.println("Start Week Date :" + sDate);

            calendar.set(Calendar.WEEK_OF_YEAR, Statement.startWeekNo + i);
            calendar.set(Calendar.DAY_OF_WEEK, 7);
            Date eDate = calendar.getTime();
            System.out.println("End week Date :" + eDate);

            String stDate = myFormat.format(sDate.getTime()) + " 00:00:00";
            String endDate = myFormat.format(eDate.getTime()) + " 23:59:59";
            List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBeforeAndPaymentStatus(storeId, stDate, endDate, "Completed");

            float totalValue = 0.00f;
            for (Order o : orders) {
                if (o.getPaymentStatus().equals("Completed")) {
                    totalValue = totalValue + o.getTotal();
                }
            }
            Response.WeeklyStatement weeklyStatement = new Response.WeeklyStatement();
            weeklyStatement.setWeekNo(Statement.startWeekNo + i);
            weeklyStatement.setTotalTrx(orders.size());
            weeklyStatement.setWeekLabel(f.format(sDate.getTime()) + " to " + f.format(eDate.getTime()));

            weeklyStatement.setTotalAmount(totalValue);
            weeklyStatement.setType("sales");
            reportResponseList.add(weeklyStatement);


        }

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(reportResponseList);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    public static class SalesReport {
        public String startDate;
        public String endDate;
        public int startWeekNo;
        public int endWeekNo;
        public String startMonth;
        public String endMonth;
        public String storeId;


        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public int getStartWeekNo() {
            return startWeekNo;
        }

        public void setStartWeekNo(int startWeekNo) {
            this.startWeekNo = startWeekNo;
        }

        public int getEndWeekNo() {
            return endWeekNo;
        }

        public void setEndWeekNo(int endWeekNo) {
            this.endWeekNo = endWeekNo;
        }

        public String getStartMonth() {
            return startMonth;
        }

        public void setStartMonth(String startMonth) {
            this.startMonth = startMonth;
        }

        public String getEndMonth() {
            return endMonth;
        }

        public void setEndMonth(String endMonth) {
            this.endMonth = endMonth;
        }

        public String getStoreId() {
            return storeId;
        }

        public void setStoreId(String storeId) {
            this.storeId = storeId;
        }

        @Override
        public String toString() {
            return "SalesReport{" +
                    "startDate='" + startDate + '\'' +
                    ", endDate='" + endDate + '\'' +
                    ", startWeekNo='" + startWeekNo + '\'' +
                    ", endWeekNo='" + endWeekNo + '\'' +
                    ", startMonth='" + startMonth + '\'' +
                    ", endMonth='" + endMonth + '\'' +
                    ", storeId='" + storeId + '\'' +
                    '}';
        }
    }

    public static class InventoryRequest {
        private String productCategory;
        private Integer minTotal;
        private Integer maxTotal;
        private String storeId;

        public String getProductCategory() {
            return productCategory;
        }

        public void setProductCategory(String productCategory) {
            this.productCategory = productCategory;
        }

        public Integer getMinTotal() {
            return minTotal;
        }

        public void setMinTotal(Integer minTotal) {
            this.minTotal = minTotal;
        }

        public Integer getMaxTotal() {
            return maxTotal;
        }

        public void setMaxTotal(Integer maxTotal) {
            this.maxTotal = maxTotal;
        }

        public String getStoreId() {
            return storeId;
        }

        public void setStoreId(String storeId) {
            this.storeId = storeId;
        }
    }

    public static class TopProductSale {
        private String startDate;
        private String endDate;
        private int startWeekNo;
        private int endWeekNo;
        private String startMonth;
        private String endMonth;
        private Integer pageSize;

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public int getStartWeekNo() {
            return startWeekNo;
        }

        public void setStartWeekNo(int startWeekNo) {
            this.startWeekNo = startWeekNo;
        }

        public int getEndWeekNo() {
            return endWeekNo;
        }

        public void setEndWeekNo(int endWeekNo) {
            this.endWeekNo = endWeekNo;
        }

        public String getStartMonth() {
            return startMonth;
        }

        public void setStartMonth(String startMonth) {
            this.startMonth = startMonth;
        }

        public String getEndMonth() {
            return endMonth;
        }

        public void setEndMonth(String endMonth) {
            this.endMonth = endMonth;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }
    }


    public static class Statement {

        private int startWeekNo;
        private int endWeekNo;
        private String startMonth;
        private String endMonth;
        private Integer pageSize;

        public int getStartWeekNo() {
            return startWeekNo;
        }

        public void setStartWeekNo(int startWeekNo) {
            this.startWeekNo = startWeekNo;
        }

        public int getEndWeekNo() {
            return endWeekNo;
        }

        public void setEndWeekNo(int endWeekNo) {
            this.endWeekNo = endWeekNo;
        }

        public String getStartMonth() {
            return startMonth;
        }

        public void setStartMonth(String startMonth) {
            this.startMonth = startMonth;
        }

        public String getEndMonth() {
            return endMonth;
        }

        public void setEndMonth(String endMonth) {
            this.endMonth = endMonth;
        }

        public Integer getPageSize() {
            return pageSize;
        }

        public void setPageSize(Integer pageSize) {
            this.pageSize = pageSize;
        }
    }
}
