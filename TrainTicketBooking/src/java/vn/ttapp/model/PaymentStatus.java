package vn.ttapp.model;

public enum PaymentStatus {

    INITIATED,
    PENDING,
    SUCCESS,
    FAILED, 
    CANCELED; 

    public static PaymentStatus from(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return switch (s.trim().toUpperCase()) {
            case "INIT", "INITIATED" ->
                INITIATED;
            case "PENDING" ->
                PENDING;
            case "SUCCESS", "SUCCEEDED", "COMPLETED", "PAID" ->
                SUCCESS;
            case "FAILED", "ERROR" ->
                FAILED;
            case "CANCELED", "CANCELLED" ->
                CANCELED;
            default ->
                INITIATED;
        };
    }

    public String toDbValue() {
        return name();
    }
}
