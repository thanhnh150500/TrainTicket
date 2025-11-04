package vn.ttapp.model;

public enum BookingStatus {
    DRAFT,
    HOLD, 
    PAID, 
    CANCELED, 
    EXPIRED; 

    public static BookingStatus from(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return switch (s.trim().toUpperCase()) {
            case "DRAFT" ->
                DRAFT;
            case "HOLD" ->
                HOLD;
            case "PAID", "SUCCESS", "SUCCEEDED", "COMPLETED" ->
                PAID;
            case "CANCELED", "CANCELLED" ->
                CANCELED;
            case "EXPIRED", "TIMEOUT" ->
                EXPIRED;
            default ->
                DRAFT;
        };
    }

    public String toDbValue() {
        return name();
    }
}
