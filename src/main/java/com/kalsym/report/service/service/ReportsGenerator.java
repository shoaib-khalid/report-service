package com.kalsym.report.service.service;

import com.kalsym.report.service.model.SettlementStatus;
import com.kalsym.report.service.model.Store;
import com.kalsym.report.service.model.report.StoreDailySale;
import com.kalsym.report.service.model.report.StoreSettlement;
import com.kalsym.report.service.model.repository.OrderRepository;
import com.kalsym.report.service.model.repository.StoreDailySalesRepository;
import com.kalsym.report.service.model.repository.StoreRepository;
import com.kalsym.report.service.model.repository.StoreSettlementsRepository;
import com.kalsym.report.service.utils.Logger;
import com.kalsym.report.service.utils.TxIdUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author saros
 */
@Service
public class ReportsGenerator {

    private final Integer[] cycle1 = {3, 4, 5};
    private final Integer[] cycle2 = {6, 7, 1, 2};
    @Autowired
    StoreDailySalesRepository storeDailySalesRepository;
    @Autowired
    StoreSettlementsRepository storeSettlementsRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    StoreRepository storeRepository;
    //ALL is for beginning to end, CURRENT is for current date and cycle
    @Value("${report.settlement.transactions.amount:ALL}")
    private String reportSettlementTransactionsAmount;

    @Scheduled(cron = "${schudler.dailysales.cron:0 58 23 * * ?}")
    public void dailySalesScheduler() {
        storeDailySalesRepository.insertDailySales();
        Logger.application.info("inserted daily store sales");

        storeDailySalesRepository.insertProductDailySales();
        Logger.application.info("inserted daily product sales");

        storeDailySalesRepository.insertStoreDailyTopProduct();
        Logger.application.info("inserted store daily top product");

        Date date = new Date();
        int cycle = getCycle(date);
        int dayOfWeek = getDayOfWeek(date);

        List<StoreDailySale> dailySales = null;
        Logger.application.info("reportSettlementTransactionsAmount: " + reportSettlementTransactionsAmount);

        if (reportSettlementTransactionsAmount.equalsIgnoreCase("ALL")) {
            dailySales = storeDailySalesRepository.findAll();
        } else {
            Date startDate = getStartDate(cycle, dayOfWeek, null);
            Date endDate = getEndDate(cycle, dayOfWeek, null);
            dailySales = storeDailySalesRepository.findByDateBetween(startDate, endDate);
        }

        Logger.application.info("dailySales: " + dailySales.size());

        for (StoreDailySale dailySale : dailySales) {
            Logger.application.info("dailySale date: " + dailySale.getDate() + " storeId: " + dailySale.getStoreId());
            Logger.application.info("dailySale settlementReferenceId: " + dailySale.getSettlementReferenceId());

            if (null == dailySale.getSettlementReferenceId()) {

                int dailySaleCycle = getCycle(dailySale.getDate());

                int dailySaleDayOfWeek = getDayOfWeek(dailySale.getDate());
                Logger.application.info("dailySaleDate: " + dailySale.getDate());

                Logger.application.info("dailySaleCycle: " + dailySaleCycle);
                Logger.application.info("dailySaleDayOfWeek: " + dailySaleDayOfWeek);

                Date dailySaleStartDate = getStartDate(dailySaleCycle, dailySaleDayOfWeek, dailySale.getDate());
                Date dailySaleEndDate = getEndDate(dailySaleCycle, dailySaleDayOfWeek, dailySale.getDate());

                Date currentDate = new Date();

                SettlementStatus status = SettlementStatus.RUNNING;
                if (currentDate.after(dailySaleEndDate)) {
                    status = SettlementStatus.CLOSED;
                }

                String storeId = dailySale.getStoreId();

                Date settlementDate = getSettlementDate(dailySaleEndDate, dailySaleCycle);

                Optional<StoreSettlement> dailySalesStoreSettlementOpt = storeSettlementsRepository.findByStoreIdAndCycleStartDateAndCycleEndDate(storeId, dailySaleStartDate, dailySaleEndDate);

                if (dailySalesStoreSettlementOpt.isPresent()) {

                    StoreSettlement dailySalesStoreSettlement = dailySalesStoreSettlementOpt.get();
                    Logger.application.info("found settlement: " + dailySalesStoreSettlement.getId());
                    Logger.application.info("total comission: " + dailySalesStoreSettlement.getTotalCommisionFee());
                    Logger.application.info("commision: " + dailySale.getCommision());
                    Logger.application.info("totalServiceSettlement: " + dailySalesStoreSettlement.getTotalServiceFee());
                    Logger.application.info("totalServiceSettlement: " + dailySalesStoreSettlement.getTotalServiceFee());

                    //dailySalesStoreSettlement.setTotalCommisionFee(dailySalesStoreSettlement.getTotalCommisionFee() + dailySale.getCommision());
                    dailySalesStoreSettlement.setTotalStoreShare(dailySale.getAmountEarned());
                    dailySalesStoreSettlement.setSettlementStatus(status);
                    dailySalesStoreSettlement.setTotalServiceFee(dailySale.getTotalServiceCharge());
                    dailySalesStoreSettlement.setTotalDeliveryFee(dailySale.getTotalDeliveryFee());
                    storeDailySalesRepository.save(dailySale);
                    storeSettlementsRepository.save(dailySalesStoreSettlement);
                } else {
                    Logger.application.info("settlement not present creating new");
                    StoreSettlement dailySalesStoreSettlement = new StoreSettlement();

                    dailySalesStoreSettlement.setTotalCommisionFee(dailySale.getCommision());
                    dailySalesStoreSettlement.setTotalStoreShare(dailySale.getAmountEarned());
                    dailySalesStoreSettlement.setSettlementStatus(status);
                    dailySalesStoreSettlement.setSettlementDate(settlementDate);
                    dailySalesStoreSettlement.setTotalServiceFee(dailySale.getTotalServiceCharge());
                    dailySalesStoreSettlement.setTotalDeliveryFee(dailySale.getTotalDeliveryFee());

                    Optional<Store> storeOpt = storeRepository.findById(storeId);

                    String settlementStoreNameAbbreviation = "";
                    String settlementStoreCountryId = "";
                    if (storeOpt.isPresent()) {
                        Store store = storeRepository.getOne(storeId);
                        Store settlementStore = storeOpt.get();
                        dailySalesStoreSettlement.setStore(store);
                        settlementStoreNameAbbreviation = settlementStore.getNameAbreviation();
                        settlementStoreCountryId = settlementStore.getRegionCountryId();
                        dailySalesStoreSettlement.setStoreName(settlementStore.getName());
                        dailySalesStoreSettlement.setClientId(settlementStore.getClientId());
                        dailySalesStoreSettlement.setCycle(dailySaleCycle + "");
                    }

                    String settlementReferenceId = TxIdUtil.generateReferenceId(settlementStoreCountryId + settlementStoreNameAbbreviation);

                    dailySalesStoreSettlement.setReferenceId(settlementReferenceId);
                    dailySalesStoreSettlement.setCycleStartDate(dailySaleStartDate);
                    dailySalesStoreSettlement.setCycleEndDate(dailySaleEndDate);

                    dailySale.setSettlementReferenceId(settlementReferenceId);

                    storeDailySalesRepository.save(dailySale);
                    storeSettlementsRepository.save(dailySalesStoreSettlement);

                }
            } else {
                Logger.application.info("settlement id already set, no need to recalculate");

            }
        }
    }

    private int getCycle(Date date) {
        int cycle = 0;

        //Calendar.DAY_OF_WEEK will retun value like this
        //sunday=1, monday=2, tuesday=3, wednesday=4, thursday=5, friday=6, saturday=7 
        //we need to convert it so
        //monday=1, tuesday=2, wednesday=3, thursday=4, friday=5, saturday=6, sunday=7 
        int dayOfWeek = getDayOfWeek(date);
        Logger.application.info("symplified dayOfWeek: " + dayOfWeek, "");

        for (int day : cycle1) {
            if (dayOfWeek == day) {
                cycle = 1;
            }
        }

        for (int day : cycle2) {
            if (dayOfWeek == day) {
                cycle = 2;
            }
        }

        Logger.application.info("cycle: " + cycle, "");

        return cycle;
    }

    private Date getStartDate(int cycle, int dayOfWeek, Date date) {

        Calendar calendar = null;
        if (date != null) {
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        } else {
            calendar = Calendar.getInstance();

        }
        int daysToMinus = 0;

        if (cycle == 1) {

            switch (dayOfWeek) {
                case 4:
                    daysToMinus = -1;
                    break;
                case 5:
                    daysToMinus = -2;
                    break;
            }
        }

        if (cycle == 2) {

            switch (dayOfWeek) {
                case 1:
                    daysToMinus = -2;
                    break;
                case 2:
                    daysToMinus = -3;
                    break;
                case 7:
                    daysToMinus = -1;
                    break;
            }
        }

        Logger.application.info("for startDate daysToMinus: " + dayOfWeek, "");

        calendar.add(Calendar.DAY_OF_YEAR, daysToMinus);

        Date startDate = calendar.getTime();

        Logger.application.info("for startDate date: " + startDate, "");

        return startDate;
    }

    private Date getEndDate(int cycle, int dayOfWeek, Date date) {

        Calendar calendar = null;
        if (date != null) {
            calendar = Calendar.getInstance();
            calendar.setTime(date);
        } else {
            calendar = Calendar.getInstance();

        }
        int daysToAdd = 0;

        if (cycle == 1) {

            switch (dayOfWeek) {
                case 3:
                    daysToAdd = 2;
                    break;
                case 4:
                    daysToAdd = 1;
                    break;
            }
        }

        if (cycle == 2) {

            switch (dayOfWeek) {
                case 1:
                    daysToAdd = 1;
                    break;
                case 6:
                    daysToAdd = 3;
                    break;
                case 7:
                    daysToAdd = 2;
                    break;
            }
        }

        Logger.application.info("for endDate daysToAdd: " + dayOfWeek, "");

        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);

        Date endDate = calendar.getTime();

        Logger.application.info("for endDate date: " + endDate, "");

        return endDate;
    }

    private int getDayOfWeek(Date date) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        Logger.application.info("calendar dayOfWeek: " + dayOfWeek, "");

        int day = 1;
        switch (dayOfWeek) {
            case 1:
                day = 7;
                break;
            default:
                day = dayOfWeek - 1;
                break;

        }

        return day;
    }

    private Date getSettlementDate(Date date, int cycle) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int daysToAdd = 0;
        if (cycle == 1) {
            daysToAdd = 6;
        } else if (cycle == 2) {
            daysToAdd = 4;
        }

        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd);

        return calendar.getTime();
    }

    private Date getCycleStartDateForCurrentWeek(int cycle) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        if (cycle == 1) {

        }

        return date;
    }
}
