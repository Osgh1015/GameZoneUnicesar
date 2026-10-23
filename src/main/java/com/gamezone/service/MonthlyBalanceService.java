package com.gamezone.service;

import com.gamezone.model.Return;
import com.gamezone.model.Sale;

import java.time.LocalDate;

/**
 * Monthly balance of the store: total sold, total refunded and net balance
 * of a month. It is an accounting report, a responsibility different from
 * registering and consulting returns, so it lives in its own class and
 * reads the sales and the returns through the services that own them.
 */
public class MonthlyBalanceService {

    private final SaleLookup sales;
    private final ReturnService returnService;

    /**
     * Creates the balance service.
     *
     * @param sales         read-only view of the registered sales
     * @param returnService service that owns the registered returns
     */
    public MonthlyBalanceService(SaleLookup sales, ReturnService returnService) {
        this.sales = sales;
        this.returnService = returnService;
    }

    /**
     * Calculates the total sold in the given month and year. Each sale
     * contributes its final total (subtotal - discount + extended warranty
     * cost), which is the amount the client actually paid.
     *
     * @param month the month (1-12)
     * @param year  the year
     * @return the sum of the final totals of the sales of that period
     */
    public double calculateMonthlySales(int month, int year) {
        double totalSales = 0.0;
        for (Sale sale : sales.getSalesHistory()) {
            if (isInPeriod(sale.getDate(), month, year)) {
                totalSales += sale.calculateFinalTotal();
            }
        }
        return round(totalSales);
    }

    /**
     * Calculates the total refunded in the given month and year, adding
     * the refund amount of every return registered in that period.
     *
     * @param month the month (1-12)
     * @param year  the year
     * @return the sum of the refunds of the returns of that period
     */
    public double calculateMonthlyReturns(int month, int year) {
        double totalReturns = 0.0;
        for (Return r : returnService.viewAllReturns()) {
            if (isInPeriod(r.getDate(), month, year)) {
                totalReturns += r.getRefundAmount();
            }
        }
        return round(totalReturns);
    }

    /**
     * Calculates the net balance for the given month and year: total
     * sales minus total returns in that period.
     *
     * @param month the month (1-12)
     * @param year  the year
     * @return the net balance (monthly sales - monthly returns)
     */
    public double generateMonthlyBalance(int month, int year) {
        return round(calculateMonthlySales(month, year) - calculateMonthlyReturns(month, year));
    }

    /**
     * Checks whether a date belongs to the given month and year.
     *
     * @param date  date to check
     * @param month the month (1-12)
     * @param year  the year
     * @return true if the date is in that month of that year
     */
    private boolean isInPeriod(LocalDate date, int month, int year) {
        return date.getMonthValue() == month && date.getYear() == year;
    }

    /**
     * Rounds a monetary value to two decimals.
     *
     * @param value value to round
     * @return the rounded value
     */
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
