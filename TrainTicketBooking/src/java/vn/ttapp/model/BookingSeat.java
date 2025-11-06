/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.model;

/**
 *
 * @author New User
 */
public class BookingSeat {

    public Integer bookingSeatId;  // identity
    public Integer bookingId;
    public Integer tripId;
    public Integer carriageId;
    public Integer seatId;

    // Lưu “snapshot” để audit kể cả sau này đổi tên/giá
    public String seatCode;       // ví dụ A05
    public String seatClass;      // ví dụ Ngồi mềm
    public int price;          // giá tại thời điểm đặt
}
