package vn.ttapp.model;

import java.util.List;

public class HoldDtos {

    // ==============================
    // 1️⃣ Yêu cầu giữ ghế (POST /api/hold)
    // ==============================
    public static class HoldRequest {
        private Integer tripId;
        private List<Integer> seatIds;
        private Integer ttlMinutes;

        public HoldRequest() {}

        public HoldRequest(Integer tripId, List<Integer> seatIds, Integer ttlMinutes) {
            this.tripId = tripId;
            this.seatIds = seatIds;
            this.ttlMinutes = ttlMinutes;
        }

        public Integer getTripId() { return tripId; }
        public void setTripId(Integer tripId) { this.tripId = tripId; }

        public List<Integer> getSeatIds() { return seatIds; }
        public void setSeatIds(List<Integer> seatIds) { this.seatIds = seatIds; }

        public Integer getTtlMinutes() { return ttlMinutes; }
        public void setTtlMinutes(Integer ttlMinutes) { this.ttlMinutes = ttlMinutes; }
    }

    // ==============================
    // 2️⃣ Thông tin ghế đã giữ (server trả về)
    // ==============================
    public static class HeldItem {
        private int seatId;
        private int lockId;
        private String expiresAt;

        public HeldItem() {}

        public HeldItem(int seatId, int lockId, String expiresAt) {
            this.seatId = seatId;
            this.lockId = lockId;
            this.expiresAt = expiresAt;
        }

        public int getSeatId() { return seatId; }
        public void setSeatId(int seatId) { this.seatId = seatId; }

        public int getLockId() { return lockId; }
        public void setLockId(int lockId) { this.lockId = lockId; }

        public String getExpiresAt() { return expiresAt; }
        public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    }

    // ==============================
    // 3️⃣ Phản hồi từ server sau khi giữ ghế
    // ==============================
    public static class HoldResponse {
        private boolean ok;
        private List<HeldItem> held;
        private List<Integer> conflicts;
        private String error;
        private String now;
        private Integer ttlMinutes;

        public HoldResponse() {}

        public boolean isOk() { return ok; }
        public void setOk(boolean ok) { this.ok = ok; }

        public List<HeldItem> getHeld() { return held; }
        public void setHeld(List<HeldItem> held) { this.held = held; }

        public List<Integer> getConflicts() { return conflicts; }
        public void setConflicts(List<Integer> conflicts) { this.conflicts = conflicts; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public String getNow() { return now; }
        public void setNow(String now) { this.now = now; }

        public Integer getTtlMinutes() { return ttlMinutes; }
        public void setTtlMinutes(Integer ttlMinutes) { this.ttlMinutes = ttlMinutes; }
    }

    // ==============================
    // 4️⃣ Yêu cầu nhả ghế (DELETE /api/hold)
    // ==============================
    public static class ReleaseRequest {
        private List<Integer> lockIds;

        public ReleaseRequest() {}
        public ReleaseRequest(List<Integer> lockIds) {
            this.lockIds = lockIds;
        }

        public List<Integer> getLockIds() { return lockIds; }
        public void setLockIds(List<Integer> lockIds) { this.lockIds = lockIds; }
    }

    // ==============================
    // 5️⃣ Phản hồi nhả ghế
    // ==============================
    public static class ReleaseResponse {
        private boolean ok;
        private int released;
        private String error;

        public ReleaseResponse() {}

        public boolean isOk() { return ok; }
        public void setOk(boolean ok) { this.ok = ok; }

        public int getReleased() { return released; }
        public void setReleased(int released) { this.released = released; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
