/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.model;

/**
 *
 * @author New User
 */
public class BookingCreateResult {

    public Integer bookingId;
    public String bookingCode;
    public int totalAmount;

    public BookingCreateResult() {
    }

    public BookingCreateResult(Integer bookingId, String bookingCode, int totalAmount) {
        this.bookingId = bookingId;
        this.bookingCode = bookingCode;
        this.totalAmount = totalAmount;
    }
}
