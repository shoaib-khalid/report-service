package com.kalsym.report.service.controller;

import com.kalsym.report.service.ReportServiceApplication;
import com.kalsym.report.service.model.Product;
import com.kalsym.report.service.model.Response;
import com.kalsym.report.service.model.Store;
import com.kalsym.report.service.model.report.StoreSettlement;
import com.kalsym.report.service.model.repository.*;
import com.kalsym.report.service.service.ReportsGenerator;
import com.kalsym.report.service.utils.HttpResponse;
import com.kalsym.report.service.utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Predicate;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    @Autowired
    ReportsGenerator reportsGenerator;
    @Autowired
    StoreRepository storeRepository;

    public static Specification<StoreSettlement> getStoreSettlementsSpec(
            String from, String to, Example<StoreSettlement> example) {
        return (Specification<StoreSettlement>) (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            if (from != null && to != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("cycleStartDate"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("cycleStartDate"), to));
            }
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    @GetMapping(value = "/report/detailedDailySales", name = "store-detail-report-sale-get")
    public ResponseEntity<HttpResponse> sales(HttpServletRequest request, @RequestParam(required = false, defaultValue = "") String startDate, @RequestParam(required = false, defaultValue = "") String endDate, @PathVariable("storeId") String storeId,
                                              @RequestParam(defaultValue = "created", required = false) String sortBy,
                                              @RequestParam(defaultValue = "DESC", required = false) String sortingOrder) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        long diffInMillis = myFormat.parse(endDate).getTime() - myFormat.parse(startDate).getTime();
        long diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        List<Response.DetailedSalesReportResponse> lists = new ArrayList<>();
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
            System.err.println("storeId: " + storeId);
            List<Object[]> orders;
            if (!storeId.equals("null")) {
                orders = orderRepository.findAllByStoreIdAndDateRangeAndPaymentStatus(storeId, stDate, enDate, "Paid", sortBy, sortingOrder);
            } else {
                orders = orderRepository.findAllByDateRangeAndPaymentStatus(stDate, enDate, "Paid", sortBy, sortingOrder);
            }
//            System.err.println("Orders: " + orders.get(1)[0].toString());
            for (int k = 0; k < orders.size(); k++) {
                Response.Sales sale = new Response.Sales();
                sale.setStoreId(orders.get(k)[1].toString());
                sale.setMerchantName(orders.get(k)[2].toString());
                sale.setStoreName(orders.get(k)[4].toString());
                String total = orders.get(k)[5].toString();
                sale.setTotal(Float.parseFloat(total));
                sale.setCustomerName(orders.get(k)[7].toString());
                if (orders.get(k)[8] != null) {
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
          /*  list.setDate(myFormat.format(sDate.getTime()));
            list.setSales(reportResponseList);
            lists.add(list);*/
            if (!reportResponseList.isEmpty()) {
                list.setDate(myFormat.format(sDate.getTime()));
                list.setSales(reportResponseList);
                lists.add(list);
            }
        }


        response.setSuccessStatus(HttpStatus.OK);
        if (sortingOrder.equalsIgnoreCase("desc")) {
            Collections.sort(lists, new Comparator<Response.DetailedSalesReportResponse>() {
                public int compare(Response.DetailedSalesReportResponse o1, Response.DetailedSalesReportResponse o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
            Collections.reverse(lists);
        } else {
            Collections.sort(lists, new Comparator<Response.DetailedSalesReportResponse>() {
                public int compare(Response.DetailedSalesReportResponse o1, Response.DetailedSalesReportResponse o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
        }
        response.setData(lists);
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

            if (!reportResponseList.isEmpty()) {
                list.setDate(myFormat.format(sDate.getTime()));
                list.setTopProduct(reportResponseList);
                lists.add(list);
            }
        }

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(lists);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "/settlement", name = "store-report-settlement-get")
    public ResponseEntity<HttpResponse> settlement(HttpServletRequest request,
                                                   @RequestParam(required = false, defaultValue = "2021-01-01") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                                                   @RequestParam(required = false, defaultValue = "2021-12-01") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
                                                   @PathVariable String storeId,
                                                   @RequestParam(defaultValue = "cycleStartDate", required = false) String sortBy,
                                                   @RequestParam(defaultValue = "DESC", required = false) String sortingOrder,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logprefix = request.getRequestURI() + " ";


        StoreSettlement storeSettlement = new StoreSettlement();
        if (!storeId.equals("null")) {
            storeSettlement.setStoreId(storeId);
        }
        Logger.application.info(Logger.pattern, ReportServiceApplication.VERSION, logprefix, "before from : " + from + ", to : " + to);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd");
        String startDate = simpleDateFormat.format(from);
        String endDate = simpleDateFormat.format(to);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(to);
        calendar.add(Calendar.HOUR, 23);
        calendar.add(Calendar.MINUTE, 59);
        to = calendar.getTime();
        Logger.application.info(Logger.pattern, ReportServiceApplication.VERSION, logprefix, "before from : " + startDate + ", to : " + endDate);
        if (sortBy.contains("from")) {
            sortBy = "cycleStartDate";
        } else {
            sortBy = "cycleEndDate";
        }


        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<StoreSettlement> example = Example.of(storeSettlement, matcher);

        Pageable pageable = null;
        if (sortingOrder.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending());
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending());
        }
        Page<StoreSettlement> settlements = storeSettlementsRepository.findAll(getStoreSettlementsSpec(startDate, endDate, example), pageable);

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(settlements);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "/daily_sales")
    public ResponseEntity<HttpResponse> dailyReport(HttpServletRequest request,
                                                    @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                                                    @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
                                                    @RequestParam(defaultValue = "date", required = false) String sortBy,
                                                    @RequestParam(defaultValue = "DESC", required = false) String sortingOrder,
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
        Logger.application.info("Storeid: " + storeId, "");

        if (!storeId.equals("null")) {
            response.setData(storeDailySalesRepository.findByStoreIdAndDateBetween(storeId, from, to, pageable));
        } else {
            response.setData(storeDailySalesRepository.findByDateBetween(from, to, pageable));
        }
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

    @PostMapping(value = "/settlement", name = "store-report-settlement-post")
    public ResponseEntity<HttpResponse> settlement(HttpServletRequest request,
                                                   @PathVariable("storeId") String storeId) throws ParseException {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        reportsGenerator.dailySalesScheduler();

        response.setSuccessStatus(HttpStatus.OK);
        return ResponseEntity.status(response.getStatus()).body(response);

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

//    static class SortByDate implements Comparator<DateItem> {
//
//        public int compare(DateItem a, DateItem b) {
//            return a.datetime.compareTo(b.datetime);
//        }
//    }
}
