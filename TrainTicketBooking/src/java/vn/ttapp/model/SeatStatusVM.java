/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.model;

/**
 *
 * @author New User
 */
public class SeatStatusVM {

    public Integer seatId, carriageId, seatClassId;
    public String seatCode, seatClassName, carriageCode, trainCode, trainName;
    public String status; // AVAILABLE | HELD | BOOKED
    public int price;  // nếu bạn hiển thị giá trên seatmap
}
