package vn.ttapp.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * User entity cho hệ thống đặt vé tàu.
 *
 * - Dùng UUID làm khóa chính (user_id) - Chứa đầy đủ thông tin cơ bản (email,
 * họ tên, sđt, địa chỉ, ...). - Có danh sách vai trò (roles) để xác định quyền
 * hạn: ADMIN / MANAGER / STAFF / CUSTOMER.
 */
public class User {

    private UUID userId;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phone;
    private String address;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Danh sách vai trò của user
     */
    private List<Role> roles;

    // ======== Constructors ======== //
    public User() {
    }

    public User(UUID userId, String email, String fullName) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
    }

    // ======== Getter / Setter ======== //
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    // ======== Helper methods ======== //
    /**
     * Trả về true nếu user có role ADMIN
     */
    public boolean isAdmin() {
        if (roles == null) {
            return false;
        }
        return roles.stream().anyMatch(r -> r != null && "ADMIN".equalsIgnoreCase(r.getCode()));
    }

    /**
     * Trả về true nếu user có role MANAGER
     */
    public boolean isManager() {
        if (roles == null) {
            return false;
        }
        return roles.stream().anyMatch(r -> r != null && "MANAGER".equalsIgnoreCase(r.getCode()));
    }

    /**
     * Trả về true nếu user có role STAFF_*
     */
    public boolean isStaff() {
        if (roles == null) {
            return false;
        }
        return roles.stream().anyMatch(r -> {
            String c = (r != null && r.getCode() != null) ? r.getCode().toUpperCase() : "";
            return c.startsWith("STAFF_");
        });
    }

    @Override
    public String toString() {
        return "User{"
                + "id=" + userId
                + ", email='" + email + '\''
                + ", fullName='" + fullName + '\''
                + ", roles=" + (roles != null ? roles.size() : 0)
                + '}';
    }
}
