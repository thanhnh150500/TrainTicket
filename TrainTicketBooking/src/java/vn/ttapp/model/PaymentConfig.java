package vn.ttapp.model;

public class PaymentConfig {

    private int paymentCfgId;
    private String bankCode;
    private String bankName;
    private String accountNo;
    private String accountName;
    private String binCode;

    // --- GETTERS & SETTERS ---
    public int getPaymentCfgId() {
        return paymentCfgId;
    }

    public void setPaymentCfgId(int paymentCfgId) {
        this.paymentCfgId = paymentCfgId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getBinCode() {
        return binCode;
    }

    public void setBinCode(String binCode) {
        this.binCode = binCode;
    }
}
