package com.kalsym.report.service.controller;

import com.kalsym.report.service.model.Order;
import com.kalsym.report.service.model.Product;
import com.kalsym.report.service.model.ProductInventory;
import com.kalsym.report.service.model.Response;
import com.kalsym.report.service.model.repository.OrderItemRepository;
import com.kalsym.report.service.model.repository.OrderRepository;
import com.kalsym.report.service.model.repository.ProductInventoryRepository;
import com.kalsym.report.service.model.repository.ProductRepository;
import com.kalsym.report.service.model.repository.StoreDailySalesRepository;
import com.kalsym.report.service.model.repository.StoreDailyTopProductsRepository;
import com.kalsym.report.service.model.repository.StoreSettlementsRepository;
import com.kalsym.report.service.utils.HttpResponse;
import com.kalsym.report.service.utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping(path = "store/{storeId}")
public class StoreReportsController {

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

    @Autowired
    StoreDailySalesRepository storeDailySalesRepository;

    @Autowired
    StoreDailyTopProductsRepository storeDailyTopProductsRepository;

    @Autowired
    StoreSettlementsRepository storeSettlementsRepository;

    @GetMapping(value = "/report/dailySales", name = "store-report-dailySale-get")
    public ResponseEntity<HttpResponse> dailySales(HttpServletRequest request, @RequestParam(required = false, defaultValue = "") String startDate, @RequestParam(required = false, defaultValue = "") String endDate, @RequestParam(defaultValue = "date", required = false) String sortBy,
                                                   @RequestParam(defaultValue = "ASC", required = false) String sortingOrder,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int pageSize,@PathVariable("storeId") String storeId) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        long diffInMillis = myFormat.parse(endDate).getTime() - myFormat.parse(startDate).getTime();
        long diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        Set<Response.DailySalesReportResponse> reportResponseList = new HashSet<>();
        for (int i = 0; i <= diff; i++) {
            Calendar sDate = Calendar.getInstance();
            try {
                sDate.setTime(myFormat.parse(startDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            sDate.add(Calendar.DAY_OF_MONTH, i);
            String stDate = myFormat.format(sDate.getTime()) + " 00:00:00";
            String enDate = myFormat.format(sDate.getTime()) + " 23:59:59";

            List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBeforeAndPaymentStatus(storeId, stDate, enDate, "Paid");
//            List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBefore(storeId, stDate, enDate);
            float totalValue = 0.00f;
            Response.DailySalesReportResponse data = new Response.DailySalesReportResponse();

            data.setDate(myFormat.format(sDate.getTime()));
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

    @GetMapping(value = "/report/detailedDailySales", name = "store-detail-report-sale-get")
    public ResponseEntity<HttpResponse> sales(HttpServletRequest request, @RequestParam(required = false, defaultValue = "") String startDate, @RequestParam(required = false, defaultValue = "") String endDate,
                                              @RequestParam(defaultValue = "created", required = false) String sortBy,
                                              @RequestParam(defaultValue = "DESC", required = false) String sortingOrder,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int pageSize, @PathVariable("storeId") String storeId) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        long diffInMillis = myFormat.parse(endDate).getTime() - myFormat.parse(startDate).getTime();
        long diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        Pageable pageable = null;
        if (sortingOrder.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending());
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending());
        }
        Set<Response.DetailedSalesReportResponse> lists = new HashSet<>();
        for (int i = 0; i <= diff; i++) {
            Response.DetailedSalesReportResponse list = new Response.DetailedSalesReportResponse();
            List<Response.Sales> reportResponseList = new ArrayList<>();
            Calendar sDate = Calendar.getInstance();
            try {
                sDate.setTime(myFormat.parse(startDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            sDate.add(Calendar.DAY_OF_MONTH, i);
            String stDate = myFormat.format(sDate.getTime()) + " 00:00:00";
            String enDate = myFormat.format(sDate.getTime()) + " 23:59:59";
            System.err.println("stDate: " + stDate);
            List<Object[]> orders = orderRepository.findAllByStoreIdAndDateRangeAndPaymentStatus(storeId, stDate, enDate, "Paid", pageable);
//            System.err.println("Orders: " + orders.get(1)[0].toString());
            for (int k = 0; k < orders.size(); k++) {
                Response.Sales sale = new Response.Sales();
                sale.setStoreId(orders.get(k)[1].toString());
                sale.setMerchantName(orders.get(k)[2].toString());
                sale.setStoreName(orders.get(k)[4].toString());
                String total = orders.get(k)[5].toString();
                sale.setTotal(Float.parseFloat(total));
                sale.setCustomerName(orders.get(k)[7].toString());
                if (orders.get(k)[8] != null){
                    String commission = orders.get(k)[8].toString();
                    sale.setCommission(Float.parseFloat(commission));
                } else {
                    String emptyValue = "0.0";
                    sale.setCommission(Float.parseFloat(emptyValue));
                }
                if (orders.get(k)[9] != null) {
                    String deliveryCharge = orders.get(k)[9].toString();
                    sale.setDeliveryCharge(Float.parseFloat(deliveryCharge));
                } else {
                    String emptyValue = "0.0";
                    sale.setDeliveryCharge(Float.parseFloat(emptyValue));
                }
                if (orders.get(k)[10] != null) {
                    String serviceCharge = orders.get(k)[10].toString();
                    sale.setServiceCharge(Float.parseFloat(serviceCharge));
                } else {
                    String emptyValue = "0.0";
                    sale.setServiceCharge(Float.parseFloat(emptyValue));
                }
                if (orders.get(k)[13] != null) {
                    String subTotal = orders.get(k)[13].toString();
                    sale.setSubTotal(Float.parseFloat(subTotal));
                } else {
                    String emptyValue = "0.0";
                    sale.setSubTotal(Float.parseFloat(emptyValue));
                }
                sale.setOrderStatus(orders.get(k)[11].toString());
                sale.setDeliveryStatus(orders.get(k)[12].toString());
                reportResponseList.add(sale);
            }
            list.setDate(myFormat.format(sDate.getTime()));
            list.setSales(reportResponseList);
            lists.add(list);
        }
        response.setSuccessStatus(HttpStatus.OK);
        response.setData(lists);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

//    @GetMapping(value = "/report/weeks/sales", name = "store-report-sale-get")
//    public ResponseEntity<HttpResponse> salesWeeks(HttpServletRequest request, @RequestParam(required = false, defaultValue = "") int startWeekNo, @RequestParam(required = false, defaultValue = "") int endWeekNo, @PathVariable("storeId") String storeId) throws Exception {
//        HttpResponse response = new HttpResponse(request.getRequestURI());
//        Date date = new Date();
//
//        int weekNo = endWeekNo - startWeekNo;
//        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
//        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
//
//        Set<Response.DetailedWeekSalesReportResponse> lists = new HashSet<>();
//        for (int i = 0; i <= weekNo; i++) {
//            Response.DetailedWeekSalesReportResponse list = new Response.DetailedWeekSalesReportResponse();
//            List<Response.Sales> reportResponseList = new ArrayList<>();
//            Calendar cal = Calendar.getInstance();
//            cal.setTime(date);
//            int year = cal.get(Calendar.YEAR);
//
//            Calendar calendar = Calendar.getInstance();
//            calendar.clear();
//            calendar.set(Calendar.YEAR, year);
//
//            calendar.set(Calendar.WEEK_OF_YEAR, startWeekNo + i);
//            calendar.set(Calendar.DAY_OF_WEEK, 1);
//            // Now get the first day of week.
//            Date sDate = calendar.getTime();
//            System.out.println("Start Week Date :" + sDate);
//
//            calendar.set(Calendar.WEEK_OF_YEAR, startWeekNo + i);
//            calendar.set(Calendar.DAY_OF_WEEK, 7);
//            Date eDate = calendar.getTime();
//            System.out.println("End week Date :" + eDate);
//
//            String stDate = myFormat.format(sDate.getTime()) + " 00:00:00";
//            String endDate = myFormat.format(eDate.getTime()) + " 23:59:59";
//
//            List<Object[]> orders = orderRepository.findAllByStoreIdAndDateRangeAndPaymentStatus(storeId, stDate, endDate, "Paid");
//
//            for (int k = 0; k < orders.size(); k++) {
//                Response.Sales sale = new Response.Sales();
//                sale.setStoreId(orders.get(k)[1].toString());
//                sale.setMerchantName(orders.get(k)[2].toString());
//                sale.setStoreName(orders.get(k)[4].toString());
//                String total = orders.get(k)[5].toString();
//                sale.setTotal(Float.parseFloat(total));
//                sale.setCustomerName(orders.get(k)[7].toString());
//                if (orders.get(k)[8] != null){
//                    String commission = orders.get(k)[8].toString();
//                    sale.setCommission(Float.parseFloat(commission));
//                } else {
//                    String emptyValue = "0.0";
//                    sale.setCommission(Float.parseFloat(emptyValue));
//                }
//                if (orders.get(k)[9] != null) {
//                    String deliveryCharge = orders.get(k)[9].toString();
//                    sale.setDeliveryCharge(Float.parseFloat(deliveryCharge));
//                } else {
//                    String emptyValue = "0.0";
//                    sale.setDeliveryCharge(Float.parseFloat(emptyValue));
//                }
//                if (orders.get(k)[10] != null) {
//                    String serviceCharge = orders.get(k)[10].toString();
//                    sale.setServiceCharge(Float.parseFloat(serviceCharge));
//                } else {
//                    String emptyValue = "0.0";
//                    sale.setServiceCharge(Float.parseFloat(emptyValue));
//                }
//                sale.setOrderStatus(orders.get(k)[11].toString());
//                sale.setDeliveryStatus(orders.get(k)[12].toString());
//                reportResponseList.add(sale);
//            }
//            list.setDate(myFormat.format(sDate.getTime()));
//            list.setSales(reportResponseList);
//            lists.add(list);
//        }
//
//        response.setSuccessStatus(HttpStatus.OK);
//        response.setData(lists);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }

    @GetMapping(value = "/report/weeklySales", name = "store-report-weeklySale-get")
    public ResponseEntity<HttpResponse> weeklySales(HttpServletRequest request, @RequestParam(required = false, defaultValue = "") int startWeekNo, @RequestParam(required = false, defaultValue = "") int endWeekNo, @PathVariable("storeId") String storeId) {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        Date date = new Date();

        int weekNo = endWeekNo - startWeekNo;
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

            calendar.set(Calendar.WEEK_OF_YEAR, startWeekNo + i);
            calendar.set(Calendar.DAY_OF_WEEK, 1);
            // Now get the first day of week.
            Date sDate = calendar.getTime();
            System.out.println("Start Week Date :" + sDate);

            calendar.set(Calendar.WEEK_OF_YEAR, startWeekNo + i);
            calendar.set(Calendar.DAY_OF_WEEK, 7);
            Date eDate = calendar.getTime();
            System.out.println("End week Date :" + eDate);

            String stDate = myFormat.format(sDate.getTime()) + " 00:00:00";
            String endDate = myFormat.format(eDate.getTime()) + " 23:59:59";
//            List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBefore(storeId, stDate, endDate);
            List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBeforeAndPaymentStatus(storeId, stDate, endDate, "Paid");
            float totalValue = 0.00f;
            Response.WeeklySalesReportResponse weeklyReport = new Response.WeeklySalesReportResponse();
            weeklyReport.setWeekNo(startWeekNo + i);
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

    @GetMapping(value = "/report/monthlySales", name = "store-report-monthlySale-get")
    public ResponseEntity<HttpResponse> monthlySales(HttpServletRequest request, @RequestParam(required = false, defaultValue = "") String startMonth, @RequestParam(required = false, defaultValue = "") String endMonth, @PathVariable("storeId") String storeId) {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        String[] m1 = startMonth.split("-");
        String[] m2 = endMonth.split("-");
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
                List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBeforeAndPaymentStatus(storeId, stDate, endDate, "Paid");
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
                List<Order> orders = orderRepository.findAllByStoreIdAndCreatedAfterAndCreatedBeforeAndPaymentStatus(storeId, stDate, endDate, "Paid");
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

    @GetMapping(value = "/report/dailyTopProducts", name = "store-report-dailyTopProducts-get")
    public ResponseEntity<HttpResponse> dailyTopProducts(HttpServletRequest request, @RequestParam(required = false, defaultValue = "") String startDate, @RequestParam(required = false, defaultValue = "") String endDate, @RequestParam(defaultValue = "date", required = false) String sortBy,
                                                         @RequestParam(defaultValue = "DESC", required = false) String sortingOrder,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int pageSize, @PathVariable("storeId") String storeId) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        long diffInMillis = myFormat.parse(endDate).getTime() - myFormat.parse(startDate).getTime();
        long diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        Set<Response.DailyTopProductResponse> lists = new HashSet<>();
        for (int i = 0; i <= diff; i++) {
            Response.DailyTopProductResponse list = new Response.DailyTopProductResponse();
            List<Response.DailyTopProduct> reportResponseList = new ArrayList<>();

            Calendar sDate = Calendar.getInstance();
            try {
                sDate.setTime(myFormat.parse(startDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            sDate.add(Calendar.DAY_OF_MONTH, i);
            String stDate = myFormat.format(sDate.getTime()) + " 00:00:00";
            String enDate = myFormat.format(sDate.getTime()) + " 23:59:59";

            List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, enDate, storeId, "PAID", 5);
//            System.err.println("test obe: " + objects.get(1)[0].toString());
//            List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, enDate, 5);

            for (int k = 0; k < objects.size(); k++) {
                Response.DailyTopProduct product = new Response.DailyTopProduct();
                String productCode = objects.get(k)[0].toString();
                System.err.println("productCode: " + productCode);
                Product product1 = productRepository.getOne(productCode);
                String totalValue = objects.get(k)[1].toString();
                product.setProductName(product1.getName());
                product.setTotalTransaction(Integer.parseInt(totalValue));
                product.setRank(k + 1);
                reportResponseList.add(product);
            }
            list.setDate(myFormat.format(sDate.getTime()));
            list.setTopProduct(reportResponseList);
            lists.add(list);
        }

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(lists);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "/report/weeklyTopProducts", name = "store-report-weeklyTopProducts-get")
    public ResponseEntity<HttpResponse> weeklyTopProducts(HttpServletRequest request, @RequestParam(required = false, defaultValue = "") int startWeekNo, @RequestParam(required = false, defaultValue = "") int endWeekNo, @PathVariable("storeId") String storeId) throws Exception {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        Date date = new Date();

        int weekNo = endWeekNo - startWeekNo;
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

            calendar.set(Calendar.WEEK_OF_YEAR, startWeekNo + i);
            calendar.set(Calendar.DAY_OF_WEEK, 1);
            // Now get the first day of week.
            Date sDate = calendar.getTime();
            System.out.println("Start Week Date :" + sDate);

            calendar.set(Calendar.WEEK_OF_YEAR, startWeekNo + i);
            calendar.set(Calendar.DAY_OF_WEEK, 7);
            Date eDate = calendar.getTime();
            System.out.println("End week Date :" + eDate);

            String stDate = myFormat.format(sDate.getTime()) + " 00:00:00";
            String endDate = myFormat.format(eDate.getTime()) + " 23:59:59";
            List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, endDate, storeId, "PAID", 5);
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
            list.setWeekNo(startWeekNo + i);
            list.setWeek(f.format(sDate.getTime()) + " to " + f.format(eDate.getTime()));
            list.setTopProduct(reportResponseList);
            lists.add(list);
        }

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(lists);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "/report/monthlyTopProducts", name = "store-report-monthlyTopProducts-get")
    public ResponseEntity<HttpResponse> monthlyTopProducts(HttpServletRequest request, @RequestParam(required = false, defaultValue = "") String startMonth, @RequestParam(required = false, defaultValue = "") String endMonth, @PathVariable("storeId") String storeId) throws Exception {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        String[] m1 = startMonth.split("-");
        String[] m2 = endMonth.split("-");
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

                List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, endDate, storeId, "PAID", 5);
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

                List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate, endDate, storeId, "PAID", 5);
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

    @GetMapping(value="/settlement", name="store-report-settlement-get")
    public ResponseEntity<HttpResponse> settlement(HttpServletRequest request, @RequestParam(required = false, defaultValue = "2021-01-01") String startDate, @RequestParam(required = false, defaultValue = "2021-12-31") String endDate,  @RequestParam(defaultValue = "startDate", required = false) String sortBy,
            @RequestParam(defaultValue = "ASC", required = false) String sortingOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,@PathVariable("storeId") String storeId) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
        Set<Response.SettlementResponse> lists = new HashSet<>();
        Response.SettlementResponse list = new Response.SettlementResponse();
        Calendar sDate = Calendar.getInstance();
        Calendar eDate = Calendar.getInstance();
        try {
            sDate.setTime(myFormat.parse(startDate));
            eDate.setTime(myFormat.parse(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String stDate = myFormat.format(sDate.getTime()) + " 00:00:00";
        String enDate = myFormat.format(eDate.getTime()) + " 23:59:59";
        List<Object[]> settlements = storeSettlementsRepository.findByStoreIdAndDate(storeId, stDate, enDate);

        for (int k = 0; k < settlements.size(); k++) {
            list.setMerchantName(settlements.get(k)[3].toString());
            String gross = settlements.get(k)[5].toString();
            list.setGross(Float.parseFloat(gross));
            String serviceFee = settlements.get(k)[6].toString();
            list.setServiceFee(Float.parseFloat(serviceFee));
            String commissionFee = settlements.get(k)[7].toString();
            list.setCommission(Float.parseFloat(commissionFee));
            String refund = settlements.get(k)[8].toString();
            list.setRefund(Float.parseFloat(refund));
            Float fee = Float.parseFloat(commissionFee) + Float.parseFloat(refund);
            list.setFees(fee);
            String nett = settlements.get(k)[9].toString();
            list.setNett(Float.parseFloat(nett));
            if (settlements.get(k)[13] != null){
                list.setSettlementDate(settlements.get(k)[13].toString());
            } else {
                String emptyString = "NULL";
                list.setSettlementDate(emptyString);
            }
        }

        lists.add(list);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(lists);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "productInventory", name = "store-report-productInventory-get")
    public ResponseEntity<HttpResponse> productInventory(HttpServletRequest request, @RequestParam(required = false, defaultValue = "") String storeId, @RequestParam(required = false, defaultValue = "") Integer minTotal, @RequestParam(required = false, defaultValue = "") Integer maxTotal) throws IOException {
        HttpResponse response = new HttpResponse(request.getRequestURI());
        List<Product> products = productRepository.findAllByStoreId(storeId);
        Set<Response.ProductInventoryResponse> inventoryResponse = new HashSet<>();
        for (Product product : products) {
            List<ProductInventory> inventories = productInventoryRepository.findAllByProductIdAndQuantityGreaterThanEqualAndQuantityLessThanEqual(product.getId(), minTotal, maxTotal);
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

    @GetMapping(value = "/monthlyStatement")
    public ResponseEntity<HttpResponse> monthlyStatement(HttpServletRequest request, @RequestParam(required = false, defaultValue = "") String startMonth,
            @RequestParam(required = false, defaultValue = "") String endMonth,
            @PathVariable("storeId") String storeId) throws IOException {
        //TODO: Need to add Order Refund Value.
        HttpResponse response = new HttpResponse(request.getRequestURI());
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        String[] m1 = startMonth.split("-");
        String[] m2 = endMonth.split("-");
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

    @GetMapping(value = "/weeklyStatement")
    public ResponseEntity<HttpResponse> weeklyStatement(HttpServletRequest request, @RequestParam(required = false, defaultValue = "") Integer startWeekNo, @RequestParam(required = false, defaultValue = "") Integer endWeekNo, @PathVariable("storeId") String storeId) throws IOException {
        //TODO: Need to add Order Refund Value.

        HttpResponse response = new HttpResponse(request.getRequestURI());

        Date date = new Date();
        int weekNo = endWeekNo - startWeekNo;
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

            calendar.set(Calendar.WEEK_OF_YEAR, startWeekNo + i);
            calendar.set(Calendar.DAY_OF_WEEK, 1);
            // Now get the first day of week.
            Date sDate = calendar.getTime();
            System.out.println("Start Week Date :" + sDate);

            calendar.set(Calendar.WEEK_OF_YEAR, startWeekNo + i);
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
            weeklyStatement.setWeekNo(startWeekNo + i);
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

    @GetMapping(value = "/daily_sales")
    public ResponseEntity<HttpResponse> dailyReport(HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
            @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
            @RequestParam(defaultValue = "date", required = false) String sortBy,
            @RequestParam(defaultValue = "ASC", required = false) String sortingOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @PathVariable("storeId") String storeId) throws IOException {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logPrefix = request.getRequestURI();
        Logger.application.info(logPrefix, "", "");
        Logger.application.info("querystring: " + request.getQueryString(), "");
        Logger.application.info("from: " + from.toString(), "");
        Logger.application.info("to: " + to.toString(), "");

        Pageable pageable = null;
        if (sortingOrder.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending());
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending());
        }
        Logger.application.info("pageable: " + pageable, "");

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(storeDailySalesRepository.findByStoreIdAndDateBetween(storeId, from, to, pageable));
        //response.setData(storeDailySalesRepository.findByStoreIdAndDateBetween(from, to, pageable));
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping(value = "/daily_top_products")
    public ResponseEntity<HttpResponse> dailytTopProducts(HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
            @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
            @RequestParam(defaultValue = "date", required = false) String sortBy,
            @RequestParam(defaultValue = "ASC", required = false) String sortingOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @PathVariable("storeId") String storeId) throws IOException {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
        String logPrefix = request.getRequestURI();
        Logger.application.info(logPrefix, "", "");
        Logger.application.info("querystring: " + request.getQueryString(), "");
        Logger.application.info("from: " + from.toString(), "");
        Logger.application.info("to: " + to.toString(), "");
        Logger.application.info("storeId: " + storeId, "");

        Pageable pageable = null;
        if (sortingOrder.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending());
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending());
        }
        Logger.application.info("pageable: " + pageable, "");

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(storeDailyTopProductsRepository.findByStoreIdAndDateBetween(storeId, from, to, pageable));
        return ResponseEntity.status(response.getStatus()).body(response);
    }

//    @GetMapping(value = "/settlement")
//    public ResponseEntity<HttpResponse> settlement(HttpServletRequest request,
//            @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
//            @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
//            @RequestParam(defaultValue = "startDate", required = false) String sortBy,
//            @RequestParam(defaultValue = "ASC", required = false) String sortingOrder,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int pageSize,
//            @PathVariable("storeId") String storeId) throws IOException {
//
//        HttpResponse response = new HttpResponse(request.getRequestURI());
//        String logPrefix = request.getRequestURI();
//        Logger.application.info(logPrefix, "", "");
//        Logger.application.info("querystring: " + request.getQueryString(), "");
//        Logger.application.info("from: " + from.toString(), "");
//        Logger.application.info("to: " + to.toString(), "");
//        Logger.application.info("storeId: " + storeId, "");
//
//        Pageable pageable = null;
//        if (sortingOrder.equalsIgnoreCase("desc")) {
//            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending());
//        } else {
//            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending());
//        }
//        Logger.application.info("pageable: " + pageable, "");
//
//        response.setSuccessStatus(HttpStatus.OK);
//        response.setData(storeSettlementsRepository.findByStoreIdAndDateBetween(storeId, from, to, pageable));
//        return ResponseEntity.status(response.getStatus()).body(response);
//    }

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
