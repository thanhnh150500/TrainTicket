package vn.ttapp.model;

import java.io.Serializable;

/**
 * Role model đại diện cho vai trò người dùng trong hệ thống.
 *
 * Ví dụ: - ADMIN → quyền cao nhất - MANAGER → quản lý - STAFF_FNB → nhân viên
 * đồ ăn thức uống - STAFF_TICKET→ nhân viên bán vé - CUSTOMER → khách hàng
 */
public class Role implements Serializable {

    private int roleId;
    private String code; // 'MANAGER', 'STAFF_FNB', ...
    private String name; // Tên hiển thị: 'Quản lý', 'Nhân viên F&B', ...

    public Role() {
    }

    public Role(int roleId, String code, String name) {
        this.roleId = roleId;
        this.code = code;
        this.name = name;
    }

    // ====== Getter / Setter ====== //
    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // ====== Helper methods ====== //
    public boolean isAdmin() {
        return code != null && code.equalsIgnoreCase("ADMIN");
    }

    public boolean isManager() {
        return code != null && code.equalsIgnoreCase("MANAGER");
    }

    public boolean isStaff() {
        return code != null && code.toUpperCase().startsWith("STAFF_");
    }

    @Override
    public String toString() {
        return "Role{"
                + "id=" + roleId
                + ", code='" + code + '\''
                + ", name='" + name + '\''
                + '}';
    }
}
