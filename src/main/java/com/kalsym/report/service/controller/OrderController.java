package com.kalsym.report.service.controller;

import com.kalsym.report.service.ReportServiceApplication;
import com.kalsym.report.service.model.DashboardViewTotal;
import com.kalsym.report.service.model.Order;
import com.kalsym.report.service.model.OrderGroup;
import com.kalsym.report.service.model.OrderItem;
import com.kalsym.report.service.model.repository.OrderItemRepository;
import com.kalsym.report.service.model.repository.OrderRepository;
import com.kalsym.report.service.utils.HttpResponse;
import com.kalsym.report.service.utils.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping(path = "store/")
public class OrderController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;


    @GetMapping(value = "getOrderItemList/{orderId}", name = "order-item-list")
    public ResponseEntity<Object> getOrderItemList(HttpServletRequest request, @PathVariable("orderId") String orderId) throws IOException {
        HttpResponse response = new HttpResponse(request.getRequestURI());
        String logprefix = request.getRequestURI();

        Optional<Order> order = orderRepository.findById(orderId);

        if (!order.isPresent()) {
            response.setErrorStatus(HttpStatus.NOT_FOUND);
            Logger.application.info(Logger.pattern, ReportServiceApplication.VERSION, logprefix, "order-items-get-by-order, orderId, not found. orderId: " + orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        List<OrderItem> itemList = orderItemRepository.findAllByOrderId(order.get().getId());

        response.setData(itemList);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
