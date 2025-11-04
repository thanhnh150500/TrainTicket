package vn.ttapp.model;

public enum BookingStatus {
    DRAFT, HOLD, PAID, CANCELED, EXPIRED;

    public static BookingStatus from(String s) {
        if (s == null) {
            return null;
        }
        return switch (s.toUpperCase()) {
            case "DRAFT" ->
                DRAFT;
            case "HOLD" ->
                HOLD;
            case "PAID" ->
                PAID;
            case "CANCELED" ->
                CANCELED;
            case "EXPIRED" ->
                EXPIRED;
            default ->
                DRAFT;
        };
    }
}
