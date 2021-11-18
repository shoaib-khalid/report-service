package com.kalsym.report.service.controller;

import com.kalsym.report.service.model.repository.OrderItemRepository;
import com.kalsym.report.service.model.repository.OrderRepository;
import com.kalsym.report.service.model.repository.ProductInventoryRepository;
import com.kalsym.report.service.model.repository.ProductRepository;
import com.kalsym.report.service.model.repository.ProductDailySalesRepository;
import com.kalsym.report.service.utils.HttpResponse;
import com.kalsym.report.service.utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping(path = "store/{storeId}/products")
public class StoreProductReportsController {

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
    ProductDailySalesRepository productDailySalesRepository;

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
        Logger.application.info("storeId: " + storeId, "");

        Pageable pageable = null;
        if (sortingOrder.equalsIgnoreCase("desc")) {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).descending());
        } else {
            pageable = PageRequest.of(page, pageSize, Sort.by(sortBy).ascending());
        }
        Logger.application.info("pageable: " + pageable, "");

        response.setSuccessStatus(HttpStatus.OK);
        response.setData(productDailySalesRepository.findByStoreIdAndDateBetween(storeId, from, to, pageable));
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
}
