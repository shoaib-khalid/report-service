package com.kalsym.report.service.service;

import com.kalsym.report.service.model.repository.OrderRepository;
import com.kalsym.report.service.model.repository.StoreDailySalesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author saros
 */
@Service
public class ReportsGenerator {

    private static Logger logger = LoggerFactory.getLogger("application");

    @Autowired
    StoreDailySalesRepository storeDailySalesRepository;

    @Autowired
    OrderRepository orderRepository;

    @Scheduled(cron = "${schudler.dailysales.cron:0 55 23 * * ?}")
    public void dailySalesScheduler() {
        storeDailySalesRepository.insertDailySales();
        logger.info("inserted daily store sales");

        storeDailySalesRepository.insertProductDailySales();
        logger.info("inserted daily product sales");

        storeDailySalesRepository.insertStoreDailyTopProduct();
        logger.info("inserted store daily top product");

    }
}
