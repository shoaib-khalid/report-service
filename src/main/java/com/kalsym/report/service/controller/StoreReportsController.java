package com.kalsym.report.service.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Subquery;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(path = "store/{storeId}")
public class StoreReportsController {

    @Autowired
    OrderRepository orderRepository;

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

    @Autowired
    OrderGroupRepository orderGroupRepository;

    @Autowired
    StoreUsersRepository storeUsersRepository;

    @Autowired
    StoreShiftSummaryRepository storeShiftSummaryRepository;


    @GetMapping(value = "/report/detailedDailySales", name = "store-detail-report-sale-get")
    public ResponseEntity<HttpResponse> sales(HttpServletRequest request,
                                              @RequestParam(required = false, defaultValue = "") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                              @RequestParam(required = false, defaultValue = "") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                              @PathVariable("storeId") String storeId,
                                              @RequestParam(defaultValue = "created", required = false) String sortBy,
                                              @RequestParam(defaultValue = "DESC", required = false) String sortingOrder,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int pageSize,
                                              @RequestParam(defaultValue = "") String countryCode,
                                              @RequestParam(defaultValue = "") String serviceType,
                                              @RequestParam(defaultValue = "") String channel) {
        HttpResponse response = new HttpResponse(request.getRequestURI());


        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat finalDate = new SimpleDateFormat("dd-MM-yyyy");

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
            List<Object[]> orders;

            if (!storeId.equals("null")) {
                // System.err.println("START DATE : " + startDate + " END DATE : " + enDate);
                orders = orderRepository.findAllByStoreIdAndDateRangeAndPaymentStatusAndCountryCode(storeId, stDate, enDate,
                        sortBy, sortingOrder, countryCode, serviceType, channel);
            } else {
                if (!countryCode.isEmpty())
//                    if (!serviceType.equals("null"))
                    orders = orderRepository.findAllByDateRangeAndPaymentStatusAndCountryCode(stDate, enDate,
                            sortBy, sortingOrder, countryCode, serviceType, channel);
//                    else
//                        orders = orderRepository.findAllByDateRangeAndPaymentStatusAndCountryCode(stDate, enDate,
//                                sortBy, sortingOrder, countryCode, null, channel);
                else if (!channel.isEmpty())
                    orders = orderRepository.findAllByDateRangeAndPaymentStatusAndChannel(stDate, enDate,
                            sortBy, sortingOrder, serviceType, channel);
                else
                    orders = orderRepository.findAllByDateRangeAndPaymentStatus(stDate, enDate,
                            sortBy, sortingOrder, serviceType);
            }
            System.out.println("orders.size() : " + orders.size());
            for (Object[] order : orders) {

                Response.Sales sale = new Response.Sales();
                sale.setStoreId(order[1].toString());
                sale.setMerchantName(order[2].toString());
                sale.setStoreName(order[4].toString());
                String total = order[5].toString();
                sale.setTotal(Float.parseFloat(total));
                if (order[7] != null) {
                    String customerName = order[7].toString();
                    sale.setCustomerName(customerName);
                } else {
                    String emptyValue = "";
                    sale.setCustomerName(emptyValue);
                }
                if (order[8] != null) {
                    String commission = order[8].toString();
                    sale.setCommission(Float.parseFloat(commission));
                } else {
                    String emptyValue = "0.0";
                    sale.setCommission(Float.parseFloat(emptyValue));
                }
                if (order[9] != null) {
                    String deliveryCharge = order[9].toString();
                    sale.setDeliveryCharge(Float.parseFloat(deliveryCharge));
                } else {
                    String emptyValue = "0.0";
                    sale.setDeliveryCharge(Float.parseFloat(emptyValue));
                }
                if (order[10] != null) {
                    String serviceCharge = order[10].toString();
                    sale.setServiceCharge(Float.parseFloat(serviceCharge));
                } else {
                    String emptyValue = "0.0";
                    sale.setServiceCharge(Float.parseFloat(emptyValue));
                }
                if (order[13] != null) {
                    String subTotal = order[13].toString();
                    sale.setSubTotal(Float.parseFloat(subTotal));
                } else {
                    String emptyValue = "0.0";
                    sale.setSubTotal(Float.parseFloat(emptyValue));
                }
                if (order[14] != null) {
                    String orderDiscount = order[14].toString();
                    sale.setOrderDiscount(Float.parseFloat(orderDiscount));
                } else {
                    String emptyValue = "0.0";
                    sale.setOrderDiscount(Float.parseFloat(emptyValue));
                }
                if (order[15] != null) {
                    String deliveryDiscount = order[15].toString();
                    sale.setDeliveryDiscount(Float.parseFloat(deliveryDiscount));
                } else {
                    String emptyValue = "0.0";
                    sale.setDeliveryDiscount(Float.parseFloat(emptyValue));
                }
                if (order[16] != null) {
                    String storeVoucherDiscount = order[16].toString();
                    sale.setStoreVoucherDiscount(Float.parseFloat(storeVoucherDiscount));
                } else {
                    String emptyValue = "0.0";
                    sale.setStoreVoucherDiscount(Float.parseFloat(emptyValue));
                }
                if (order[17] != null) {
                    String voucherCode = order[17].toString();
                    sale.setVoucherCode(voucherCode);
                } else {
                    String emptyValue = "";
                    sale.setVoucherCode(emptyValue);
                }
//                if (order[20] != null) {
//                    String itemCount = order[21].toString();
//                    sale.setNoOfOrderItem(Integer.valueOf(itemCount));
//                } else {
//                    Integer emptyValue = 0;
//                    sale.setNoOfOrderItem(emptyValue);
//                }
                if (order[18] != null) {
                    String type = order[18].toString();
                    sale.setServiceType(type);
                } else {
                    String emptyValue = "";
                    sale.setServiceType(emptyValue);
                }
                if (order[19] != null) {
                    String channelType = order[19].toString();
                    sale.setChannel(channelType);
                } else {
                    String emptyValue = "";
                    sale.setChannel(emptyValue);
                }
                sale.setOrderStatus(order[11].toString());
                sale.setDeliveryStatus(order[12].toString());
                reportResponseList.add(sale);
            }
            list.setDate(myFormat.format(sDate.getTime()));
            list.setSales(reportResponseList);
            lists.add(list);
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
                                                           @RequestParam(required = false, defaultValue = "") String serviceType,
                                                           @RequestParam(defaultValue = "created", required = false) String sortBy,
                                                           @RequestParam(defaultValue = "DESC", required = false) String sortingOrder,
                                                           @RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "20") int pageSize,
                                                           @RequestParam(defaultValue = "") String countryCode) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        Order orderMatch = new Order();
        if (!storeId.contains("null")) {
            Store store = storeRepository.getOne(storeId);
            orderMatch.setStore(store);
        }
        if (!serviceType.isEmpty())
            orderMatch.setServiceType(serviceType);
        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withMatcher("serviceType", new ExampleMatcher.GenericPropertyMatcher().exact())
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Order> orderExample = Example.of(orderMatch, matcher);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("created").descending());
        Page<Order> orders = orderRepository
                .findAll(getSpecWithDatesBetween(startDate, endDate, OrderStatus.PAID, orderExample, countryCode), pageable);

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
                                                         @RequestParam(defaultValue = "20") int pageSize, @PathVariable("storeId") String storeId,
                                                         @RequestParam(defaultValue = "") String countryCode) throws Exception {
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
            List<Object[]> objects;
            if (!countryCode.isEmpty())
                objects = orderItemRepository.findAllByTopSaleProductByCountry(stDate, enDate, storeId, "PAID", 5, countryCode);
            else
                objects = orderItemRepository.findAllByTopSaleProduct(stDate, enDate, storeId, "PAID", 5);


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
                                                                 @RequestParam(defaultValue = "20") int pageSize, @PathVariable("storeId") String storeId,
                                                                 @RequestParam(defaultValue = "") String countryCode, @RequestParam(defaultValue = "") String serviceType) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");

        ProductDailySale productDailySale = new ProductDailySale();
        if (!storeId.contains("null")) {
            Store store = storeRepository.getOne(storeId);
            productDailySale.setStore(store);
        }

        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<ProductDailySale> example = Example.of(productDailySale, matcher);

        Pageable pageable = null;
        if (sortingOrder.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending().and(Sort.by("ranking").ascending()));
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending().and(Sort.by("ranking").ascending()));
        }
        Page<ProductDailySale> products = productDailySalesRepository
                .findAll(getSpecDailySaleWithDatesBetween(startDate, endDate, example, countryCode, serviceType), pageable);

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
                                                   @RequestParam(defaultValue = "20") int pageSize,
                                                   @RequestParam(defaultValue = "") String countryCode) throws Exception {
        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logprefix = request.getRequestURI() + " ";

        StoreSettlement storeSettlement = new StoreSettlement();
        if (!storeId.contains("null")) {
            Store store = storeRepository.getOne(storeId);
            storeSettlement.setStore(store);
        }
        Logger.application.info(Logger.pattern, ReportServiceApplication.VERSION, logprefix,
                "before from : " + from + ", to : " + to);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String startDate = simpleDateFormat.format(from);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(to);
        calendar.add(Calendar.HOUR, 23);
        calendar.add(Calendar.MINUTE, 59);
        to = calendar.getTime();
        String endDate = simpleDateFormat.format(to);

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
                .findAll(getStoreSettlementsSpec(startDate, endDate, example, countryCode), pageable);

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
                                                    @PathVariable("storeId") String storeId,
                                                    @RequestParam(defaultValue = "") String countryCode,
                                                    @RequestParam(defaultValue = "") String serviceType,
                                                    @RequestParam(defaultValue = "") String channel
    ) throws IOException {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logPrefix = request.getRequestURI();
        Logger.application.info(logPrefix, "", "");
        Logger.application.info("queryString: " + request.getQueryString(), "");
        Logger.application.info("from: " + from.toString(), "");
        Logger.application.info("to: " + to.toString(), "");


        StoreDailySale example = new StoreDailySale();

        if (!storeId.equals("null") && !storeId.isEmpty()) {
            Store store = storeRepository.getOne(storeId);
            example.setStoreId(store.getId());
        }

        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<StoreDailySale> orderExample = Example.of(example, matcher);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("date").descending());
        Page<StoreDailySale> storeDailySale = storeDailySalesRepository
                .findAll(getStoreDailySaleSpec(from, to, orderExample, countryCode, serviceType, channel), pageable);

//
//        Pageable pageable = null;
//        if (sortingOrder.equalsIgnoreCase("desc")) {
//            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending());
//        } else {
//            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending());
//        }
//        response.setSuccessStatus(HttpStatus.OK);
//        Logger.application.info("StoreId: " + storeId, "");
////
////        Page<StoreDailySale> storeDailySales = storeSettlementsRepository
////                .findAll(getStoreSettlementsSpec(startDate, endDate, example, countryCode), pageable);
////
////
//        if (!storeId.equals("null")) {
//            response.setData(storeDailySalesRepository.findByStoreIdAndDateBetween(storeId, from, to, pageable));
//        } else {
//            response.setData(storeDailySalesRepository.findByDateBetween(from, to, pageable));
//        }
        response.setData(storeDailySale);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "/merchant_daily_sales", name = "store-report-settlement-get")
    public ResponseEntity<HttpResponse> merchant_daily_sales(HttpServletRequest request,
                                                             @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                                                             @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
                                                             @RequestParam(defaultValue = "date", required = false) String sortBy,
                                                             @RequestParam(defaultValue = "DESC", required = false) String sortingOrder,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "20") int pageSize,
                                                             @PathVariable("storeId") String storeId,
                                                             @RequestParam(defaultValue = "") String countryCode,
                                                             @RequestParam(defaultValue = "") String serviceType) throws IOException {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logPrefix = request.getRequestURI();
        Logger.application.info(logPrefix, "", "");
        Logger.application.info("query string: " + request.getQueryString(), "");
        Logger.application.info("from: " + from.toString(), "");
        Logger.application.info("to: " + to.toString(), "");
        StoreDailySale example = new StoreDailySale();

        if (storeId != null && !storeId.isEmpty()) {
            Store store = storeRepository.getOne(storeId);
            example.setStoreId(store.getId());
        }

        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<StoreDailySale> orderExample = Example.of(example, matcher);

        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("date").descending());
        Page<StoreDailySale> storeDailySale = storeDailySalesRepository
                .findAll(getMerchantDailySaleWithDateBetween(from, to, orderExample, serviceType), pageable);

        response.setData(storeDailySale);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "/daily_top_products")
    public ResponseEntity<HttpResponse> dailyTopProducts(HttpServletRequest request,
                                                         @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                                                         @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
                                                         @RequestParam(defaultValue = "date", required = false) String sortBy,
                                                         @RequestParam(defaultValue = "ASC", required = false) String sortingOrder,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int pageSize,
                                                         @PathVariable("storeId") String storeId,
                                                         @RequestParam(defaultValue = "") String countryCode) throws IOException {

        HttpResponse response = new HttpResponse(request.getRequestURI());
        SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
        String logPrefix = request.getRequestURI();
        Logger.application.info(logPrefix, "", "");
        Logger.application.info("queryString: " + request.getQueryString(), "");
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
                                                   @PathVariable("storeId") String storeId,
                                                   @RequestParam(defaultValue = "") String countryCode) throws ParseException {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        reportsGenerator.dailySalesScheduler();

        response.setSuccessStatus(HttpStatus.OK);
        return ResponseEntity.status(response.getStatus()).body(response);

    }

    @GetMapping(value = "/totalSales", name = "store-total-sales-count-pending")
    public ResponseEntity<Object> totalSalesCount(HttpServletRequest request, @PathVariable("storeId") String storeId,
                                                  @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                                                  @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
                                                  @RequestParam(defaultValue = "") String countryCode,
                                                  @RequestParam(defaultValue = "") String serviceType)
            throws IOException {
        // TODO: not completed
        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logPrefix = request.getRequestURI();
        DashboardViewTotal viewTotal = new DashboardViewTotal();
        Date date = new Date();
        Date endDate = new Date();
        // daily sales
        endDate.setDate(date.getDate() + 1);

        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String todayDate = simpleDateFormat.format(date);

        String todayEndDate = simpleDateFormat.format(endDate);
        List<Object[]> dailyOrder;

        if (!serviceType.isEmpty()) {
            dailyOrder = orderRepository.findAllByStatusAndDateRangeAndServiceType(storeId, todayDate, todayEndDate, serviceType);
        } else {
            dailyOrder = orderRepository.findAllByStatusAndDateRange(storeId, todayDate, todayEndDate);
        }

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
        Logger.application.info("Service Type  : " + serviceType);
        List<Object[]> weeklyOrder;
        if (!serviceType.isEmpty()) {
            weeklyOrder = orderRepository.findAllByStatusAndDateRangeAndServiceType(storeId,
                    simpleDateFormat.format(firstDayOfWeek), simpleDateFormat.format(lastDayOfWeek), serviceType);
        } else {
            weeklyOrder = orderRepository.findAllByStatusAndDateRange(storeId,
                    simpleDateFormat.format(firstDayOfWeek), simpleDateFormat.format(lastDayOfWeek));
        }
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
        List<Object[]> monthlyOrder;
        if (!serviceType.isEmpty()) {
            monthlyOrder = orderRepository.findAllByStatusAndDateRangeAndServiceType(storeId,
                    simpleDateFormat.format(firstDayOfMonth), simpleDateFormat.format(lastDayOfMonth), serviceType);
        } else {
            monthlyOrder = orderRepository.findAllByStatusAndDateRange(storeId,
                    simpleDateFormat.format(firstDayOfMonth), simpleDateFormat.format(lastDayOfMonth));
        }
        Set<OrderCount> monthlySales = new HashSet<>();

        for (Object[] value : monthlyOrder) {

            OrderCount monthlyOrderCount = new OrderCount();
            monthlyOrderCount.setCompletionStatus(value[0].toString());
            monthlyOrderCount.setTotal(Integer.parseInt(value[1].toString()));
            monthlySales.add(monthlyOrderCount);

        }

        cal.set(Calendar.DAY_OF_YEAR, 1);
        Date firstDayOfYear = cal.getTime();

        Logger.application.info("First Month  : " + firstDayOfMonth);
        Logger.application.info("Second Month : " + lastDayOfMonth);

        List<Object[]> yearlyOrder;
        if (!serviceType.isEmpty()) {
            yearlyOrder = orderRepository.findAllByStatusAndDateRangeAndServiceType(storeId,
                    simpleDateFormat.format(firstDayOfYear), todayEndDate, serviceType);
        } else {
            yearlyOrder = orderRepository.findAllByStatusAndDateRange(storeId,
                    simpleDateFormat.format(firstDayOfYear), todayEndDate);
        }

        Set<OrderCount> yearlyOrders = new HashSet<>();

        for (
                Object[] value : yearlyOrder) {

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

        return ResponseEntity.status(response.getStatus()).

                body(response.getData());
    }

    @GetMapping(value = "/weeklySale", name = "store-weekly-sales-count")
    public ResponseEntity<Object> weeklySale(HttpServletRequest request, @PathVariable("storeId") String storeId,
                                             @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                                             @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
                                             @RequestParam(defaultValue = "") String countryCode, @RequestParam(defaultValue = "DELIVERIN") String serviceType)
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
        Logger.application.info("First Day of The Week  : " + firstDayOfWeek.getDate());

        List<Object[]> weeklyOrder = orderRepository.findAllByStatusAndDateRangeAndServiceType(storeId, simpleDateFormat.format(from),
                simpleDateFormat.format(to), serviceType);
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
                                              @RequestParam(defaultValue = "ASC", required = false) String sortingOrder,
                                              @RequestParam(defaultValue = "") String countryCode,
                                              @RequestParam(defaultValue = "") String serviceType) throws IOException {
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
        to.setDate(to.getDate() + 1);

        Logger.application.info("First Day of The Week  : " + firstDayOfWeek.getDate());
        Logger.application.info("Service TYPE   : " + serviceType.isEmpty());
        List<Object[]> weeklyOrder;
        if (!serviceType.isEmpty())
            weeklyOrder = orderRepository.fineAllByStatusAndDateRangeAndGroupAndServiceType(storeId,
                    simpleDateFormat.format(from), simpleDateFormat.format(to), serviceType);
        else
            weeklyOrder = orderRepository.fineAllByStatusAndDateRangeAndGroup(storeId,
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


    @GetMapping(value = "/voucherOrderGroupList", name = "group-order-with-voucher-list")
    public ResponseEntity<Object> voucherOrderGroupList(HttpServletRequest request,
                                                        @PathVariable("storeId") String storeId,
                                                        @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                                                        @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
                                                        @RequestParam(defaultValue = "created", required = false) String sortBy,
                                                        @RequestParam(defaultValue = "ASC", required = false) String sortingOrder,
                                                        @RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int pageSize,
                                                        @RequestParam(defaultValue = "") String countryCode) throws IOException {
        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logPrefix = request.getRequestURI();
        DashboardViewTotal viewTotal = new DashboardViewTotal();

        OrderGroup orderGroup = new OrderGroup();


        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<OrderGroup> example = Example.of(orderGroup, matcher);

        Pageable pageable = null;
        if (sortingOrder.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending());
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending());
        }
        Page<OrderGroup> products = orderGroupRepository
                .findAll(getSpecGroupOrderWithVoucherDailySaleWithDatesBetween(from, to, example, countryCode), pageable);

        response.setData(products);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping(value = "/orderGroupList", name = "group-order-sales-list")
    public ResponseEntity<Object> dailyGroupOrderList(HttpServletRequest request,
                                                      @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                                                      @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
                                                      @RequestParam(defaultValue = "created", required = false) String sortBy,
                                                      @RequestParam(defaultValue = "ASC", required = false) String sortingOrder,
                                                      @PathVariable("storeId") String storeId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int pageSize,
                                                      @RequestParam(defaultValue = "") String countryCode,
                                                      @RequestParam(defaultValue = "") String serviceType,
                                                      @RequestParam(defaultValue = "") String channel) throws IOException {
        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logPrefix = request.getRequestURI();
        DashboardViewTotal viewTotal = new DashboardViewTotal();

        OrderGroup orderGroup = new OrderGroup();


        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<OrderGroup> example = Example.of(orderGroup, matcher);

        Pageable pageable = null;
        if (sortingOrder.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending());
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending());
        }
        Page<OrderGroup> products = orderGroupRepository
                .findAll(getSpecGroupOrderListDailySaleWithDatesBetween(from, to, example, countryCode, serviceType, channel), pageable);

        response.setData(products);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
// STAFF SALES REPORT


    //NEW CHANGES VERSION 2.0 MERCHANT PORTAL INCLUDING STAFF REPORT AND NEW DASHBOARD DESIGN

    @GetMapping(value = "/report/staff", name = "staff-sales-report-list")
    public ResponseEntity<Object> staffReport(HttpServletRequest request,
                                              @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                                              @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
                                              @RequestParam(defaultValue = "created", required = false) String sortBy,
                                              @RequestParam(defaultValue = "ASC", required = false) String sortingOrder,
                                              @PathVariable("storeId") String storeId,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int pageSize,
                                              @RequestParam(defaultValue = "") String countryCode,
                                              @RequestParam(defaultValue = "") String serviceType,
                                              @RequestParam(defaultValue = "") String channel) throws IOException {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        StoreUser storeUser = new StoreUser();
        storeUser.setStoreId(storeId);


        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<StoreUser> example = Example.of(storeUser, matcher);

        Pageable pageable = null;
        if (sortingOrder.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending());
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending());
        }
        Page<StoreUser> products = storeUsersRepository
                .findAll(getSpecStaffReportSaleWithDatesBetween(from, to, example), pageable);

        response.setData(products);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "/report/staff/totalSales", name = "staff-total-sales-by-store")
    public ResponseEntity<Object> staffByRange(HttpServletRequest request,
                                               @RequestParam(required = false, defaultValue = "") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                                               @RequestParam(required = false, defaultValue = "") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
                                               @RequestParam(defaultValue = "created", required = false) String sortBy,
                                               @RequestParam(defaultValue = "ASC", required = false) String sortingOrder,
                                               @PathVariable("storeId") String storeId,
                                               @RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "20") int pageSize,
                                               @RequestParam(defaultValue = "DINEIN") String serviceType,
                                               @RequestParam(defaultValue = "") String staffName) throws IOException {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        StoreUser storeUser = new StoreUser();
        storeUser.setStoreId(storeId);
        storeUser.setDeactivated(false);
        if (!staffName.isEmpty())
            storeUser.setName(staffName);


        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withMatcher("name", new ExampleMatcher.GenericPropertyMatcher())
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<StoreUser> example = Example.of(storeUser, matcher);

        Pageable pageable = null;
        if (sortingOrder.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending());
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending());
        }
        Page<StoreUser> staffList = storeUsersRepository
                .findAll(getStaffDetails(from, to, example), pageable);

//daily
        Date date = new Date();
        Date endDate = new Date();
        // daily sales
        endDate.setDate(date.getDate() + 1);

        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String todayDate = simpleDateFormat.format(date);

        String todayEndDate = simpleDateFormat.format(endDate);
//week
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() + 1);
        Date firstDayOfWeek = cal.getTime();
        Date lastDayOfWeek = cal.getTime();
        firstDayOfWeek.setDate(firstDayOfWeek.getDate());
        lastDayOfWeek.setDate(firstDayOfWeek.getDate() + 7);
        Logger.application.info("First Day of The Week  : " + firstDayOfWeek.getDate());
        Logger.application.info("First week  : " + simpleDateFormat.format(firstDayOfWeek));
        Logger.application.info("last week  : " + simpleDateFormat.format(lastDayOfWeek));
        Logger.application.info("Service Type  : " + serviceType);


        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek() - 1);
        Date firstDayOfPreviousWeek = cal.getTime();
        Date lastDayOfPreviousWeek = cal.getTime();
        firstDayOfPreviousWeek.setDate(firstDayOfWeek.getDate());
        lastDayOfPreviousWeek.setDate(firstDayOfWeek.getDate() + 7);
        Logger.application.info("First Day of The Week  : " + firstDayOfPreviousWeek.getDate());
        Logger.application.info("First week  : " + simpleDateFormat.format(firstDayOfPreviousWeek));
        Logger.application.info("last week  : " + simpleDateFormat.format(lastDayOfPreviousWeek));
        Logger.application.info("Service Type  : " + serviceType);

//month

        Calendar c = Calendar.getInstance(); // this takes current date
        c.set(Calendar.DAY_OF_MONTH, 1);
        Date firstDayOfMonth = c.getTime();
        Date lastDayOfMonth = c.getTime();
        lastDayOfMonth.setDate(firstDayOfMonth.getDate() + c.getActualMaximum(Calendar.DAY_OF_MONTH));


        Calendar month = Calendar.getInstance(); // this takes current date
        month.set(Calendar.DAY_OF_MONTH, -1);
        Date firstDayOfPreviousMonth = month.getTime();
        Date lastDayOfPreviousMonth = month.getTime();
        lastDayOfPreviousMonth.setDate(firstDayOfPreviousMonth.getDate() + month.getActualMaximum(Calendar.DAY_OF_MONTH));


        List<StaffSalesReport> staffSalesReportList = new ArrayList<>();


        for (StoreUser staff : staffList.getContent()) {

            StaffSalesReport staffSalesReport = new StaffSalesReport();
            StaffDetails staffDetails = new StaffDetails();
            staffDetails.setName(staff.getName());
            staffDetails.setId(staff.getId());
            staffDetails.setStoreId(staff.getStoreId());
            staffSalesReport.setStaff(staffDetails);

            List<Object[]> dailyOrder = orderRepository.fineAllByStatusAndDateRangeAndStaffId(storeId, todayDate, todayEndDate, serviceType, staff.getId());

            for (Object[] element : dailyOrder) {
                StaffOrderCount daily = new StaffOrderCount();
                daily.setDate(todayDate);
                daily.setTotal(Integer.parseInt(element[0].toString()));
                staffSalesReport.setDailyCount(daily);
            }


            List<Object[]> weeklyOrder = orderRepository.fineAllByStatusAndDateRangeAndStaffId(storeId,
                    simpleDateFormat.format(firstDayOfWeek), simpleDateFormat.format(lastDayOfWeek), serviceType, staff.getId());

            for (Object[] item : weeklyOrder) {

                StaffOrderCount weekly = new StaffOrderCount();
                weekly.setWeekNo(cal.WEEK_OF_YEAR);
                weekly.setTotal(Integer.parseInt(item[0].toString()));
                staffSalesReport.setWeeklyCount(weekly);
            }


            List<Object[]> previousWeeklyOrder = orderRepository.fineAllByStatusAndDateRangeAndStaffId(storeId,
                    simpleDateFormat.format(firstDayOfPreviousWeek), simpleDateFormat.format(lastDayOfPreviousWeek), serviceType, staff.getId());

            for (Object[] item : previousWeeklyOrder) {

                StaffOrderCount previousWeek = new StaffOrderCount();
                previousWeek.setWeekNo(cal.WEEK_OF_YEAR);
                previousWeek.setTotal(Integer.parseInt(item[0].toString()));
                staffSalesReport.setPreviousWeeklyCount(previousWeek);
            }

            // monthly sales
            Logger.application.info("First Month  : " + firstDayOfMonth);
            Logger.application.info("Second Month : " + lastDayOfMonth);
            List<Object[]> monthlyOrder = orderRepository.fineAllByStatusAndDateRangeAndStaffId(storeId,
                    simpleDateFormat.format(firstDayOfMonth), simpleDateFormat.format(lastDayOfMonth), serviceType, staff.getId());

            for (Object[] value : monthlyOrder) {

                StaffOrderCount monthly = new StaffOrderCount();
                monthly.setMonth(c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH));
                monthly.setTotal(Integer.parseInt(value[0].toString()));
                staffSalesReport.setMonthlyCount(monthly);
            }


            List<Object[]> previousMonthOrder = orderRepository.fineAllByStatusAndDateRangeAndStaffId(storeId,
                    simpleDateFormat.format(firstDayOfPreviousMonth), simpleDateFormat.format(lastDayOfPreviousMonth), serviceType, staff.getId());

            for (Object[] value : previousMonthOrder) {

                StaffOrderCount previousMonth = new StaffOrderCount();
                previousMonth.setMonth(month.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH));
                previousMonth.setTotal(Integer.parseInt(value[0].toString()));
                staffSalesReport.setPreviousMonthlyCount(previousMonth);
            }


            staffSalesReportList.add(staffSalesReport);
        }
        Pageable paging = staffList.getPageable();

        Page<StaffSalesReport> staffSalesReports = new PageImpl<>(staffSalesReportList, paging, staffList.getTotalElements());

        response.setData(staffSalesReports);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping(value = "/report/staff/totalSales/{staffId}", name = "staff-total-sales-by-staffId")
    public ResponseEntity<Object> getStaffSalesReport(HttpServletRequest request,
                                                      @RequestParam(required = false, defaultValue = "") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                                                      @RequestParam(required = false, defaultValue = "") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
                                                      @RequestParam(defaultValue = "created", required = false) String sortBy,
                                                      @RequestParam(defaultValue = "ASC", required = false) String sortingOrder,
                                                      @PathVariable("storeId") String storeId,
                                                      @PathVariable("staffId") String staffId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int pageSize,
                                                      @RequestParam(defaultValue = "DINEIN") String serviceType) throws IOException {
        HttpResponse response = new HttpResponse(request.getRequestURI());

        StoreShiftSummary storeShiftSummary = new StoreShiftSummary();
        storeShiftSummary.setUserId(staffId);


        ExampleMatcher matcher = ExampleMatcher
                .matchingAll()
                .withIgnoreCase()
                .withIgnoreNullValues()
                .withMatcher("name", new ExampleMatcher.GenericPropertyMatcher())
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<StoreShiftSummary> example = Example.of(storeShiftSummary, matcher);

        Pageable pageable = null;
        if (sortingOrder.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending());
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending());
        }
        Page<StoreShiftSummary> storeShiftSummaries = storeShiftSummaryRepository.findAll(getStaffSalesSummary(from, to, example), pageable);
        List<StoreShiftSummary> l = new ArrayList<>();

        for (StoreShiftSummary summary : storeShiftSummaries.getContent()) {
            Double total = 0.00;
            for (StoreShiftSummaryDetails s : summary.getSummaryDetails()) {
                total = s.getSaleAmount() + total;
            }
            summary.setTotalSales(total);
            l.add(summary);
        }
        Page<StoreShiftSummary> storeShiftDetail = new PageImpl<>(l, storeShiftSummaries.getPageable(), storeShiftSummaries.getTotalElements());
        response.setData(storeShiftDetail);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping(value = "/report/staff/name", name = "staff-get-name-list")
    public ResponseEntity<Object> getStaffSalesReport(HttpServletRequest request, @PathVariable("storeId") String storeId) {
        HttpResponse response = new HttpResponse(request.getRequestURI());
        List<StoreUser> userList = storeUsersRepository.findByStoreId(storeId);

        response.setData(userList);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @GetMapping(value = "/totalSalesAmount", name = "store-total-sales-amount")
    public ResponseEntity<Object> totalSalesAmount(HttpServletRequest request, @PathVariable("storeId") String storeId,
                                                   @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                                                   @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
                                                   @RequestParam(defaultValue = "") String countryCode,
                                                   @RequestParam(defaultValue = "") String serviceType)
            throws IOException {
        // TODO: not completed
        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logPrefix = request.getRequestURI();
        DashboardViewTotal viewTotal = new DashboardViewTotal();
        Date date = new Date();
        Date endDate = new Date();
        // daily sales
        endDate.setDate(date.getDate() + 1);

        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String todayDate = simpleDateFormat.format(date);

        String todayEndDate = simpleDateFormat.format(endDate);
        List<Object[]> dailyOrder;

        if (!serviceType.isEmpty()) {
            dailyOrder = orderRepository.dailyTotalSalesAmountByServiceType(storeId, todayDate, todayEndDate, serviceType);
        } else {
            dailyOrder = orderRepository.dailyTotalSalesAmount(storeId, todayDate, todayEndDate);
        }

        Set<OrderCount> todaySales = new HashSet<>();

        for (Object[] element : dailyOrder) {
            OrderCount dailyOrderCount = new OrderCount();
            if (element[0] != null) {
                dailyOrderCount.setDate(element[0].toString());
                dailyOrderCount.setTotalSalesAmount(Double.parseDouble(element[1].toString()));
            } else {
                dailyOrderCount.setDate(todayDate);
                dailyOrderCount.setTotalSalesAmount(0.00);
            }
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
        Logger.application.info("Service Type  : " + serviceType);
        List<Object[]> weeklyOrder;
        if (!serviceType.isEmpty()) {
            weeklyOrder = orderRepository.dailyTotalSalesAmountByServiceType(storeId,
                    simpleDateFormat.format(firstDayOfWeek), simpleDateFormat.format(lastDayOfWeek), serviceType);
        } else {
            weeklyOrder = orderRepository.dailyTotalSalesAmount(storeId,
                    simpleDateFormat.format(firstDayOfWeek), simpleDateFormat.format(lastDayOfWeek));
        }
        Set<OrderCount> weeklySales = new HashSet<>();

        for (Object[] item : weeklyOrder) {
            OrderCount weeklyOrderCount = new OrderCount();

            if (item[0] != null) {
                weeklyOrderCount.setDate(item[0].toString());
                weeklyOrderCount.setTotalSalesAmount(Double.parseDouble(item[1].toString()));
            } else {
                weeklyOrderCount.setDate(simpleDateFormat.format(firstDayOfWeek));
                weeklyOrderCount.setTotalSalesAmount(0.00);
            }
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
        List<Object[]> monthlyOrder;
        if (!serviceType.isEmpty()) {
            monthlyOrder = orderRepository.dailyTotalSalesAmountByServiceType(storeId,
                    simpleDateFormat.format(firstDayOfMonth), simpleDateFormat.format(lastDayOfMonth), serviceType);
        } else {
            monthlyOrder = orderRepository.dailyTotalSalesAmount(storeId,
                    simpleDateFormat.format(firstDayOfMonth), simpleDateFormat.format(lastDayOfMonth));
        }
        Set<OrderCount> monthlySales = new HashSet<>();

        for (Object[] value : monthlyOrder) {

            OrderCount monthlyOrderCount = new OrderCount();
            if (value[0] != null) {
                monthlyOrderCount.setDate(value[0].toString());
                monthlyOrderCount.setTotalSalesAmount(Double.parseDouble(value[1].toString()));
            } else {
                monthlyOrderCount.setDate(simpleDateFormat.format(firstDayOfMonth));
                monthlyOrderCount.setTotalSalesAmount(0.00);
            }
            monthlySales.add(monthlyOrderCount);

        }

        cal.set(Calendar.DAY_OF_YEAR, 1);
        Date firstDayOfYear = cal.getTime();

        Logger.application.info("First Month  : " + firstDayOfMonth);
        Logger.application.info("Second Month : " + lastDayOfMonth);

        List<Object[]> yearlyOrder;
        if (!serviceType.isEmpty()) {
            yearlyOrder = orderRepository.dailyTotalSalesAmountByServiceType(storeId,
                    simpleDateFormat.format(firstDayOfYear), todayEndDate, serviceType);
        } else {
            yearlyOrder = orderRepository.dailyTotalSalesAmount(storeId,
                    simpleDateFormat.format(firstDayOfYear), todayEndDate);
        }

        Set<OrderCount> yearlyOrders = new HashSet<>();

        for (Object[] value : yearlyOrder) {

            OrderCount yearlyOrderCount = new OrderCount();
            if (value[0] != null) {
                yearlyOrderCount.setDate(value[0].toString());
                yearlyOrderCount.setTotalSalesAmount(Double.parseDouble(value[1].toString()));
            }else{
                yearlyOrderCount.setDate(simpleDateFormat.format(firstDayOfYear));
                yearlyOrderCount.setTotalSalesAmount(0.00);
            }
            yearlyOrders.add(yearlyOrderCount);

        }

        viewTotal.setDailySales(todaySales);
        viewTotal.setWeeklySales(weeklySales);
        viewTotal.setMonthlySales(monthlySales);
        viewTotal.setTotalSales(yearlyOrders);

        response.setData(viewTotal);
        response.setSuccessStatus(HttpStatus.OK);

        return ResponseEntity.status(response.getStatus()).

                body(response.getData());
    }


    @GetMapping(value = "/weeklySalesGraph", name = "store-weekly-sales-amount")
    public ResponseEntity<Object> weeklySalesGraph(HttpServletRequest request, @PathVariable("storeId") String storeId,
                                                   @RequestParam(required = false, defaultValue = "2019-01-06") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
                                                   @RequestParam(required = false, defaultValue = "2021-12-31") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
                                                   @RequestParam(defaultValue = "created", required = false) String sortBy,
                                                   @RequestParam(defaultValue = "ASC", required = false) String sortingOrder,
                                                   @RequestParam(defaultValue = "") String countryCode,
                                                   @RequestParam(defaultValue = "") String serviceType) throws IOException {
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
        to.setDate(to.getDate() + 1);

        Logger.application.info("First Day of The Week  : " + firstDayOfWeek.getDate());
        Logger.application.info("Service TYPE   : " + serviceType.isEmpty());
        List<Object[]> weeklyOrder;
        if (!serviceType.isEmpty())
            weeklyOrder = orderRepository.findAllDateRangeAndSumTotalSales(storeId,
                    simpleDateFormat.format(from), simpleDateFormat.format(to), serviceType);
        else
            weeklyOrder = orderRepository.findAllDateRangeAndSumTotalSalesGroup(storeId,
                    simpleDateFormat.format(from), simpleDateFormat.format(to));
        // List<OrderCount> weeklySales = new HashSet<>();
        List<OrderCount> weeklySales = new ArrayList<>();

        for (Object[] item : weeklyOrder) {

            OrderCount weeklyOrderCount = new OrderCount();
            weeklyOrderCount.setTotalSalesAmount(Double.parseDouble(item[1].toString()));
            weeklyOrderCount.setDate(item[2].toString());
            weeklySales.add(weeklyOrderCount);
        }

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


    //Specification//
    public Specification<Order> getSpecWithDatesBetween(
            Date from, Date to, OrderStatus completionStatus, Example<Order> example, String countryCode) {

        return (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            Join<Order, Store> store = root.join("store");

            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("created"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("created"), to));
            }
            if (completionStatus == OrderStatus.PAID) {
                Predicate predicateForOnlinePayment = builder.equal(root.get("paymentStatus"), "PAID");
                Predicate predicateForCompletionStatus = builder.equal(root.get("paymentStatus"), "PENDING");
                Predicate predicateForCOD = builder.and(predicateForCompletionStatus);
                Predicate finalPredicate = builder.or(predicateForOnlinePayment, predicateForCOD);
                predicates.add(finalPredicate);
            } else if (completionStatus != null) {
                predicates.add(builder.equal(root.get("paymentStatus"), "PAID"));
            }
            if (!countryCode.isEmpty())
                predicates.add(builder.equal(store.get("regionCountryId"), countryCode));

            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public Specification<ProductDailySale> getSpecDailySaleWithDatesBetween(
            Date from, Date to, Example<ProductDailySale> example, String countryCode, String serviceType) {

        return (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            Join<ProductDailySale, Store> store = root.join("store");

            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("date"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("date"), to));
            }
            if (!serviceType.equals("")) {
                predicates.add(builder.equal(root.get("serviceType"), serviceType));
            }
            if (!countryCode.isEmpty())
                predicates.add(builder.equal(store.get("regionCountryId"), countryCode));
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public Specification<OrderGroup> getSpecGroupOrderWithVoucherDailySaleWithDatesBetween(
            Date from, Date to, Example<OrderGroup> example, String countryCode) {

        return (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("created"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("created"), to));
            }
            predicates.add(builder.isNotNull(root.get("voucher")));
            predicates.add(builder.equal(root.get("paymentStatus"), "PAID"));
            if (!countryCode.isEmpty())
                predicates.add(builder.equal(root.get("regionCountryId"), countryCode));
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public Specification<OrderGroup> getSpecGroupOrderListDailySaleWithDatesBetween(
            Date from, Date to, Example<OrderGroup> example, String countryCode, String serviceType, String channel) {

        return (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            Join<OrderGroup, Order> order = root.join("orderList");

            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("created"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("created"), to));
            }

            if (serviceType.equals("DINEIN")) {
                predicates.add(builder.equal(root.get("serviceType"), "DINEIN"));
                predicates.add(builder.equal(root.get("paymentStatus"), "PAID"));
                predicates.add(builder.equal(order.get("completionStatus"), "DELIVERED_TO_CUSTOMER"));
            } else if (serviceType.equals("DELIVERIN")) {
                predicates.add(builder.equal(root.get("serviceType"), "DELIVERIN"));
                predicates.add(builder.equal(root.get("paymentStatus"), "PAID"));
            }

            if (channel.equals("DELIVERIN")) {
                predicates.add(builder.equal(root.get("channel"), "DELIVERIN"));
            } else if (channel.equals("PAYHUB2U")) {
                predicates.add(builder.equal(root.get("channel"), "PAYHUB2U"));
            } else if (channel.equals("EKEDAI")) {
                predicates.add(builder.equal(root.get("channel"), "EKEDAI"));
            }
//            predicates.add(builder.equal(root.get("paymentStatus"), "PAID"));
            if (!countryCode.isEmpty())
                predicates.add(builder.equal(root.get("regionCountryId"), countryCode));
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));
            query.distinct(true);

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public Specification<StoreDailySale> getMerchantDailySaleWithDateBetween(
            Date from, Date to, Example<StoreDailySale> example, String serviceType) {

        return (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
//            Join<StoreDailySale, Store> store = root.join("store");


            if (from != null && to != null) {
                // to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("date"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("date"), to));
            }

            if (serviceType.equals("DINEIN")) {
                predicates.add(builder.equal(root.get("serviceType"), "DINEIN"));
            } else if (serviceType.equals("DELIVERIN")) {
                predicates.add(builder.equal(root.get("serviceType"), "DELIVERIN"));
            }

//            predicates.add(builder.equal(store.get("regionCountryId"),countryCode));
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public static Specification<StoreSettlement> getStoreSettlementsSpec(
            String from, String to, Example<StoreSettlement> example, String countryCode) {
        return (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            Join<StoreSettlement, Store> store = root.join("store");

            if (from != null && to != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("cycleStartDate"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("cycleStartDate"), to));
            }
            if (!countryCode.isEmpty())
                predicates.add(builder.equal(store.get("regionCountryId"), countryCode));
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    public static Specification<StoreDailySale> getStoreDailySaleSpec(
            Date from, Date to, Example<StoreDailySale> example, String countryCode, String serviceType, String channel) {
        return (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            Join<StoreSettlement, Store> store = root.join("store");

            if (from != null && to != null) {
                // to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("date"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("date"), to));
            }

            if (serviceType.equals("DINEIN")) {
                predicates.add(builder.equal(root.get("serviceType"), "DINEIN"));
            } else if (serviceType.equals("DELIVERIN")) {
                predicates.add(builder.equal(root.get("serviceType"), "DELIVERIN"));
            }

            if (channel.equals("DELIVERIN")) {
                predicates.add(builder.equal(root.get("channel"), "DELIVERIN"));
            } else if (channel.equals("PAYHUB2U")) {
                predicates.add(builder.equal(root.get("channel"), "PAYHUB2U"));
            } else if (channel.equals("EKEDAI")) {
                predicates.add(builder.equal(root.get("channel"), "EKEDAI"));
            }

            if (!countryCode.isEmpty())
                predicates.add(builder.equal(store.get("regionCountryId"), countryCode));
            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }


    public Specification<StoreUser> getSpecStaffReportSaleWithDatesBetween(
            Date from, Date to, Example<StoreUser> example) {

        return (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
            Join<StoreUser, StoreShiftSummary> storeShiftSummary = root.join("shiftSummaries");

            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(storeShiftSummary.get("created"), from));
            }

            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));
            query.distinct(true);

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }


    public Specification<StoreUser> getStaffDetails(
            Date from, Date to, Example<StoreUser> example) {

        return (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();
//            Join<StoreUser, StoreShiftSummary> storeShiftSummary = root.join("shiftSummaries");

//            if (from != null && to != null) {
//                to.setDate(to.getDate() + 1);
//                predicates.add(builder.greaterThanOrEqualTo(storeShiftSummary.get("created"), from));
//                predicates.add(builder.lessThanOrEqualTo(storeShiftSummary.get("created"), to));
//            }

            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));
            query.distinct(true);

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }


    public Specification<StoreShiftSummary> getStaffSalesSummary(
            Date from, Date to, Example<StoreShiftSummary> example) {

        return (root, query, builder) -> {
            final List<Predicate> predicates = new ArrayList<>();

            if (from != null && to != null) {
                to.setDate(to.getDate() + 1);
                predicates.add(builder.greaterThanOrEqualTo(root.get("created"), from));
                predicates.add(builder.lessThanOrEqualTo(root.get("created"), to));
            }

            predicates.add(QueryByExamplePredicateBuilder.getPredicate(root, builder, example));
            query.distinct(true);

            return builder.and(predicates.toArray(new Predicate[predicates.size()]));
        };
    }

    @Getter
    @Setter
    @ToString
    public static class Statement {

        private int startWeekNo;
        private int endWeekNo;
        private String startMonth;
        private String endMonth;
        private Integer pageSize;

    }


    @Getter
    @Setter
    @ToString
    @NoArgsConstructor
//    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StaffSalesReport {

        private StaffDetails staff;
        private StaffOrderCount dailyCount;
        private StaffOrderCount weeklyCount;
        private StaffOrderCount previousWeeklyCount;
        private StaffOrderCount monthlyCount;
        private StaffOrderCount previousMonthlyCount;

    }

    @Getter
    @Setter
    public class StaffOrderCount {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String date;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        int weekNo;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        int previousWeekNo;


        @JsonInclude(JsonInclude.Include.NON_NULL)
        String month;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String lastMonth;
        int total;
    }

    @Getter
    @Setter
    public class StaffDetails {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String name;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String id;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String storeId;
    }

}
