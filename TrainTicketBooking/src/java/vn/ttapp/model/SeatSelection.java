/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.model;

/**
 *
 * @author New User
 */
public class SeatSelection {

    public Integer seatId;
    public Integer carriageId;
    public String seatCode;
    public String seatClassName;
    public int price;

    public SeatSelection() {
    }

    public SeatSelection(Integer seatId, Integer carriageId, String seatCode, String seatClassName, int price) {
        this.seatId = seatId;
        this.carriageId = carriageId;
        this.seatCode = seatCode;
        this.seatClassName = seatClassName;
        this.price = price;
    }
}
