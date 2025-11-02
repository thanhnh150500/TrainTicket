/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package vn.ttapp.model;

import java.time.OffsetDateTime;

/**
 *
 * @author New User
 */
public class SeatHold {
    public Integer seatHoldId;     // identity
    public Integer tripId;
    public Integer carriageId;
    public Integer seatId;
    public String  userSession;    // sid theo HttpSession
    public OffsetDateTime expiresAt;
    public OffsetDateTime createdAt;
    
    // thêm 2 dòng này để đồng bộ với Booking flow
    public Integer bookingId;   // null nếu chưa gắn booking
    public Boolean isConfirmed; // true nếu đã chốt (PAID)
}
