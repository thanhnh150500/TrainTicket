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
    public Integer seatHoldId;
    public Integer tripId;
    public Integer carriageId;
    public Integer seatId;
    public String  userSession; 
    public OffsetDateTime expiresAt;
    public OffsetDateTime createdAt;
    
    public Integer bookingId;
    public Boolean isConfirmed;
}
