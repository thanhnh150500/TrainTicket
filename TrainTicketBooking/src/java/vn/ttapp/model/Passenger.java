package vn.ttapp.model;

import java.time.LocalDate;

public class Passenger {

    private Long passengerId;
    private Long bookingId;   
    private String fullName;     
    private LocalDate birthDate; 
    private String idNumber;      
    private String phone;        
    private String email;       

    // ====== Constructors ======
    public Passenger() {
    }

    public Passenger(Long bookingId, String fullName, LocalDate birthDate, String idNumber, String phone, String email) {
        this.bookingId = bookingId;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.idNumber = idNumber;
        this.phone = phone;
        this.email = email;
    }

    // ====== Getters & Setters ======
    public Long getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(Long passengerId) {
        this.passengerId = passengerId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // ====== Convenience ======
    @Override
    public String toString() {
        return "Passenger{"
                + "passengerId=" + passengerId
                + ", bookingId=" + bookingId
                + ", fullName='" + fullName + '\''
                + ", birthDate=" + birthDate
                + ", idNumber='" + idNumber + '\''
                + ", phone='" + phone + '\''
                + ", email='" + email + '\''
                + '}';
    }
}
