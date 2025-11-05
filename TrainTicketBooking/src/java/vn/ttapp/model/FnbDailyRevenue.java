package vn.ttapp.model;

import java.time.LocalDate;

public class FnbDailyRevenue {
    private LocalDate day;
    private double totalAmount;

    public FnbDailyRevenue() {}

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
