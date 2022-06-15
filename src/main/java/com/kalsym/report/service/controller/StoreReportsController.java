package com.kalsym.report.service.controller;

import com.kalsym.report.service.ReportServiceApplication;
import com.kalsym.report.service.model.*;
import com.kalsym.report.service.model.report.ProductDailySale;
import com.kalsym.report.service.model.report.StoreDailySale;
import com.kalsym.report.service.model.report.StoreSettlement;
import com.kalsym.report.service.model.repository.*;
import com.kalsym.report.service.service.ReportsGenerator;
import com.kalsym.report.service.service.enums.OrderStatus;
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
import java.util.stream.Collectors;

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
    ProductDailySalesRepository productDailySalesRepository;

    @Autowired
    StoreSettlementsRepository storeSettlementsRepository;

    @Autowired
    ReportsGenerator reportsGenerator;
    @Autowired
    StoreRepository storeRepository;

    public static Specification<StoreSettlement> getStoreSettlementsSpec(
            String from, String to, Example<StoreSettlement> example) {
        return (root, query, builder) -> {
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
    public ResponseEntity<HttpResponse> sales(HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false, defaultValue = "") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @PathVariable("storeId") String storeId,
            @RequestParam(defaultValue = "created", required = false) String sortBy,
            @RequestParam(defaultValue = "DESC", required = false) String sortingOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());


        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        long diffInMillis = endDate.getTime() - startDate.getTime();
        long diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        List<Response.DetailedSalesReportResponse> lists = new ArrayList<>();

        for (int i = 0; i <= diff; i++) {
            Response.DetailedSalesReportResponse list = new Response.DetailedSalesReportResponse();
            List<Response.Sales> reportResponseList = new ArrayList<>();
            Calendar sDate = Calendar.getInstance();
            sDate.setTime(startDate);
            sDate.add(Calendar.DAY_OF_MONTH, i);
            String stDate = myFormat.format(sDate.getTime()) + " 00:00:00";
            String enDate = myFormat.format(sDate.getTime()) + " 23:59:59";
            // System.err.println(" myFormat.format(sDate.getTime()) : " +
            // myFormat.format(sDate.getTime()));
            List<Object[]> orders;

            if (!storeId.equals("null")) {
                // System.err.println("START DATE : " + startDate + " END DATE : " + enDate);
                orders = orderRepository.findAllByStoreIdAndDateRangeAndPaymentStatus(storeId, stDate, enDate, "PAID",
                        sortBy, sortingOrder);
            } else {
                orders = orderRepository.findAllByDateRangeAndPaymentStatus(stDate, enDate, OrderStatus.PAID.name(),
                        sortBy, sortingOrder);
            }
            System.out.println("orders.size() : " + orders.size());
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
                if (orders.get(k)[14] != null) {
                    String orderDiscount = orders.get(k)[14].toString();
                    sale.setOrderDiscount(Float.parseFloat(orderDiscount));
                } else {
                    String emptyValue = "0.0";
                    sale.setOrderDiscount(Float.parseFloat(emptyValue));
                }
                if (orders.get(k)[15] != null) {
                    String deliveryDiscount = orders.get(k)[15].toString();
                    sale.setDeliveryDiscount(Float.parseFloat(deliveryDiscount));
                } else {
                    String emptyValue = "0.0";
                    sale.setDeliveryDiscount(Float.parseFloat(emptyValue));
                }
                if (orders.get(k)[16] != null) {
                    String storeVoucherDiscount = orders.get(k)[16].toString();
                    sale.setStoreVoucherDiscount(Float.parseFloat(storeVoucherDiscount));
                } else {
                    String emptyValue = "0.0";
                    sale.setStoreVoucherDiscount(Float.parseFloat(emptyValue));
                }
                if (orders.get(k)[17] != null) {
                    String platformDiscount = orders.get(k)[17].toString();
                    sale.setPlatformDiscount(Float.parseFloat(platformDiscount));
                } else {
                    String emptyValue = "0.0";
                    sale.setPlatformDiscount(Float.parseFloat(emptyValue));
                }
                if (orders.get(k)[18] != null) {
                    String voucherType = orders.get(k)[18].toString();
                    sale.setVoucherType(voucherType);
                } else {
                    String emptyValue = "";
                    sale.setVoucherType(emptyValue);
                }
                sale.setOrderStatus(orders.get(k)[11].toString());
                sale.setDeliveryStatus(orders.get(k)[12].toString());
                reportResponseList.add(sale);
            }
            list.setDate(myFormat.format(sDate.getTime()));
            list.setSales(reportResponseList);
            lists.add(list);
            // if (!reportResponseList.isEmpty()) {
            // list.setDate(myFormat.format(sDate.getTime()));
            // list.setSales(reportResponseList);
            // lists.add(list);
            // }
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

    @GetMapping(value = "/report/merchantDetailedDailySales", name = "store-merchant-detail-report-sale-get")
    public ResponseEntity<HttpResponse> merchantDailySales(HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false, defaultValue = "") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @PathVariable("storeId") String storeId,
            @RequestParam(defaultValue = "created", required = false) String sortBy,
            @RequestParam(defaultValue = "DESC", required = false) String sortingOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Order orderMatch = new Order();
        if (!storeId.contains("null")) {
            Store store = storeRepository.getOne(storeId);
            orderMatch.setStore(store);
        }

        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Order> orderExample = Example.of(orderMatch, matcher);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("created").descending());
        Page<Order> orders = orderRepository
                .findAll(getSpecWithDatesBetween(startDate, endDate, OrderStatus.PAID, orderExample), pageable);

        response.setData(orders);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "/report/dailyTopProducts", name = "store-report-dailyTopProducts-get")
    public ResponseEntity<HttpResponse> dailyTopProducts(HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "") String startDate,
            @RequestParam(required = false, defaultValue = "") String endDate,
            @RequestParam(defaultValue = "date", required = false) String sortBy,
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
            // System.err.println("test obe: " + objects.get(1)[0].toString());
            // List<Object[]> objects = orderItemRepository.findAllByTopSaleProduct(stDate,
            // enDate, 5);

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

    @GetMapping(value = "/report/merchantDailyTopProducts", name = "store-merchant-report-dailyTopProducts-get")
    public ResponseEntity<HttpResponse> merchantDailyTopProducts(HttpServletRequest request,
            @RequestParam(required = false, defaultValue = "") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false, defaultValue = "") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
            @RequestParam(defaultValue = "date", required = false) String sortBy,
            @RequestParam(defaultValue = "DESC", required = false) String sortingOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int pageSize, @PathVariable("storeId") String storeId) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        ProductDailySale productDailySale = new ProductDailySale();
        if (storeId != null && !storeId.isEmpty()) {
            productDailySale.setStoreId(storeId);
        }

        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<ProductDailySale> example = Example.of(productDailySale, matcher);

        // Pageable pageable = PageRequest.of(page, pageSize,
        // Sort.by(sortBy).descending());
        Pageable pageable = null;
        if (sortingOrder.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending().and(Sort.by("ranking").ascending()));
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending().and(Sort.by("ranking").ascending()));
        }
        Page<ProductDailySale> products = productDailySalesRepository
                .findAll(getSpecDailySaleWithDatesBetween(startDate, endDate, example), pageable);

        response.setData(products);
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
        Logger.application.info(Logger.pattern, ReportServiceApplication.VERSION, logprefix,
                "before from : " + from + ", to : " + to);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String startDate = simpleDateFormat.format(from);
        String endDate = simpleDateFormat.format(to);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(to);
        calendar.add(Calendar.HOUR, 23);
        calendar.add(Calendar.MINUTE, 59);
        to = calendar.getTime();
        Logger.application.info(Logger.pattern, ReportServiceApplication.VERSION, logprefix,
                "before from : " + startDate + ", to : " + endDate);
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
        Page<StoreSettlement> settlements = storeSettlementsRepository
                .findAll(getStoreSettlementsSpec(startDate, endDate, example), pageable);

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

    @GetMapping(value = "/merchant_daily_sales", name = "store-report-settlement-get")
    public ResponseEntity<HttpResponse> merchant_daily_sales(HttpServletRequest request,
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

        // Pageable pageable = null;
        // if (sortingOrder.equalsIgnoreCase("desc")) {
        // pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending());
        // } else {
        // pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending());
        // }
        // Logger.application.info("pageable: " + pageable, "");

        StoreDailySale example = new StoreDailySale();

        if (storeId != null && !storeId.isEmpty()) {
            Store store = storeRepository.getOne(storeId);
            example.setStore(store);
        }

        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<StoreDailySale> orderExample = Example.of(example, matcher);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("date").descending());
        Page<StoreDailySale> storeDailySale = storeDailySalesRepository
                .findAll(getMerchantDailySaleWithDateBetween(from, to, orderExample), pageable);

        // response.setSuccessStatus(HttpStatus.OK);
        // Logger.application.info("Storeid: " + storeId, "");
        //
        // StoreDailySale example = new StoreDailySale();
        // if
        //
        // if (!storeId.equals("null")) {
        // response.setData(storeDailySalesRepository.findAll(getMerchantDailySaleWithDateBetween(storeId,
        // from, to, pageable));
        // } else {
        // response.setData(storeDailySalesRepository.findByDateBetween(from, to,
        // pageable));
        // }
        response.setData(storeDailySale);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
        Logger.application.info("Pageable", pageable, "");

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

    @GetMapping(value = "/totalSales", name = "store-total-sales-count-pending")
    public ResponseEntity<Object> totalSalesCount(HttpServletRequest request, @PathVariable("storeId") String storeId,
            @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
            @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to)
            throws IOException {
        // TODO: not completed
        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logPrefix = request.getRequestURI();
        DashboardViewTotal viewTotal = new DashboardViewTotal();
        Date date = new Date();
        Date enddate = new Date();
        // daily sales
        enddate.setDate(date.getDate() + 1);

        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String todayDate = simpleDateFormat.format(date);

        String todayEndDate = simpleDateFormat.format(enddate);

        List<Object[]> dailyOrder = orderRepository.fineAllByStatusAndDateRange(storeId, todayDate, todayEndDate);
        Set<OrderCount> todaySales = new HashSet<>();

        for (Object[] element : dailyOrder) {

            OrderCount dailyOrderCount = new OrderCount();
            dailyOrderCount.setCompletionStatus(element[0].toString());
            dailyOrderCount.setTotal(Integer.parseInt(element[1].toString()));
            todaySales.add(dailyOrderCount);
        }
        // weekly sales
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() + 1);
        Date firstDayOfWeek = cal.getTime();
        Date lastDayOfWeek = cal.getTime();
        firstDayOfWeek.setDate(firstDayOfWeek.getDate());
        lastDayOfWeek.setDate(firstDayOfWeek.getDate() + 7);
        Logger.application.info("First Day of The Week  : " + firstDayOfWeek.getDate());
        Logger.application.info("First week  : " + simpleDateFormat.format(firstDayOfWeek));
        Logger.application.info("last week  : " + simpleDateFormat.format(lastDayOfWeek));

        List<Object[]> weeklyOrder = orderRepository.fineAllByStatusAndDateRange(storeId,
                simpleDateFormat.format(firstDayOfWeek), simpleDateFormat.format(lastDayOfWeek));
        Set<OrderCount> weeklySales = new HashSet<>();

        for (Object[] item : weeklyOrder) {

            OrderCount weeklyOrderCount = new OrderCount();
            weeklyOrderCount.setCompletionStatus(item[0].toString());
            weeklyOrderCount.setTotal(Integer.parseInt(item[1].toString()));
            weeklySales.add(weeklyOrderCount);
        }
        // monthly sales

        Calendar c = Calendar.getInstance(); // this takes current date
        c.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = c.getTime();
        Date lastDayOfMonth = c.getTime();
        lastDayOfMonth.setDate(firstDayOfMonth.getDate() + c.getActualMaximum(Calendar.DAY_OF_MONTH));

        Logger.application.info("First Month  : " + firstDayOfMonth);
        Logger.application.info("Second Month : " + lastDayOfMonth);
        List<Object[]> montlyOrder = orderRepository.fineAllByStatusAndDateRange(storeId,
                simpleDateFormat.format(firstDayOfMonth), simpleDateFormat.format(lastDayOfMonth));
        Set<OrderCount> monthlySales = new HashSet<>();

        for (Object[] value : montlyOrder) {

            OrderCount monthlyOrderCount = new OrderCount();
            monthlyOrderCount.setCompletionStatus(value[0].toString());
            monthlyOrderCount.setTotal(Integer.parseInt(value[1].toString()));
            monthlySales.add(monthlyOrderCount);

        }

        cal.set(Calendar.DAY_OF_YEAR, 1);
        Date firstDayOfYear = cal.getTime();

        Logger.application.info("First Month  : " + firstDayOfMonth);
        Logger.application.info("Second Month : " + lastDayOfMonth);
        List<Object[]> yearlyOrder = orderRepository.fineAllByStatusAndDateRange(storeId,
                simpleDateFormat.format(firstDayOfYear), todayEndDate);
        Set<OrderCount> yearlyOrders = new HashSet<>();

        for (Object[] value : yearlyOrder) {

            OrderCount yearlyOrderCount = new OrderCount();
            yearlyOrderCount.setCompletionStatus(value[0].toString());
            yearlyOrderCount.setTotal(Integer.parseInt(value[1].toString()));
            yearlyOrders.add(yearlyOrderCount);

        }

        viewTotal.setDailySales(todaySales);
        viewTotal.setWeeklySales(weeklySales);
        viewTotal.setMonthlySales(monthlySales);
        viewTotal.setTotalSales(yearlyOrders);

        response.setData(viewTotal);
        response.setSuccessStatus(HttpStatus.OK);

        return ResponseEntity.status(response.getStatus()).body(response.getData());
    }

    @GetMapping(value = "/weeklySale", name = "store-weekly-sales-count")
    public ResponseEntity<Object> weeklySale(HttpServletRequest request, @PathVariable("storeId") String storeId,
            @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
            @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to)
            throws IOException {
        // TODO: not completed
        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logPrefix = request.getRequestURI();
        DashboardViewTotal viewTotal = new DashboardViewTotal();
        //
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        // weekly sales
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Date firstDayOfWeek = from;
        Date lastDayOfWeek = to;
        // firstDayOfWeek.setDate(firstDayOfWeek.getDate());
        // lastDayOfWeek.setDate(firstDayOfWeek.getDate() + 7);
        Logger.application.info("First Day of The Week  : " + firstDayOfWeek.getDate());

        List<Object[]> weeklyOrder = orderRepository.fineAllByStatusAndDateRange(storeId, simpleDateFormat.format(from),
                simpleDateFormat.format(to));
        Set<OrderCount> weeklySales = new HashSet<>();

        for (Object[] item : weeklyOrder) {

            OrderCount weeklyOrderCount = new OrderCount();
            weeklyOrderCount.setCompletionStatus(item[0].toString());
            weeklyOrderCount.setTotal(Integer.parseInt(item[1].toString()));
            weeklySales.add(weeklyOrderCount);
        }

        viewTotal.setWeeklySales(weeklySales);

        response.setData(viewTotal);
        response.setSuccessStatus(HttpStatus.OK);

        return ResponseEntity.status(response.getStatus()).body(response.getData());
    }

    @GetMapping(value = "/weeklyGraph", name = "store-weekly-sales-count")
    public ResponseEntity<Object> weeklyGraph(HttpServletRequest request, @PathVariable("storeId") String storeId,
            @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
            @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
            @RequestParam(defaultValue = "created", required = false) String sortBy,
            @RequestParam(defaultValue = "ASC", required = false) String sortingOrder) throws IOException {
        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logPrefix = request.getRequestURI();
        DashboardViewTotal viewTotal = new DashboardViewTotal();
        //
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        // weekly sales
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -7);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        Date firstDayOfWeek = from;
        Date lastDayOfWeek = to;
        // firstDayOfWeek.setDate(firstDayOfWeek.getDate());
        // lastDayOfWeek.setDate(firstDayOfWeek.getDate() + 7);
        Logger.application.info("First Day of The Week  : " + firstDayOfWeek.getDate());

        List<Object[]> weeklyOrder = orderRepository.fineAllByStatusAndDateRangeAndGroup(storeId,
                simpleDateFormat.format(from), simpleDateFormat.format(to));
        // List<OrderCount> weeklySales = new HashSet<>();
        List<OrderCount> weeklySales = new ArrayList<>();

        for (Object[] item : weeklyOrder) {

            OrderCount weeklyOrderCount = new OrderCount();
            weeklyOrderCount.setCompletionStatus(item[0].toString());
            weeklyOrderCount.setTotal(Integer.parseInt(item[1].toString()));
            weeklyOrderCount.setDate(item[2].toString());
            weeklySales.add(weeklyOrderCount);
        }

        //
        // weeklySales.stream()
        // .sorted(Comparator.comparing(OrderCount::getDate)) //comparator - how you
        // want to sort it
        // .collect(Collectors.toList());
        // Collections.reverse(weeklySales);

        Collections.sort(weeklySales, new Comparator<OrderCount>() {
            public int compare(OrderCount o1, OrderCount o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });
        // Collections.reverse(weeklySales);

        viewTotal.setDashboardGraph(weeklySales);
        response.setData(viewTotal);
        response.setSuccessStatus(HttpStatus.OK);

        return ResponseEntity.status(response.getStatus()).body(response.getData());
    }

    public Specification<Order> getSpecWithDatesBetween(
            Date from, Date to, OrderStatus completionStatus, Example<Order> example) {

        return (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("created"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("created"), to));
            }
            if (completionStatus == OrderStatus.PAID) {
                Predicate predicateForOnlinePayment = builder.equal(root.get("paymentStatus"), "PAID");
                Predicate predicateForCompletionStatus = builder.equal(root.get("paymentStatus"), "PAID");
                Predicate predicateForCOD = builder.and(predicateForCompletionStatus);
                Predicate finalPredicate = builder.or(predicateForOnlinePayment, predicateForCOD);
                predicates.add(finalPredicate);
            } else if (completionStatus != null) {
                predicates.add(builder.equal(root.get("paymentStatus"), "PAID"));
            }
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public Specification<ProductDailySale> getSpecDailySaleWithDatesBetween(
            Date from, Date to, Example<ProductDailySale> example) {

        return (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("date"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("date"), to));
            }
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public Specification<StoreDailySale> getMerchantDailySaleWithDateBetween(
            Date from, Date to, Example<StoreDailySale> example) {

        return (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (from != null && to != null) {
                // to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("date"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("date"), to));
            }
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
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
