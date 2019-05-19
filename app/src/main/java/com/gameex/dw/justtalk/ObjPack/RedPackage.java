package com.gameex.dw.justtalk.ObjPack;

public class RedPackage {
    /**
     * 红包唯一标识
     */
    private String pubKey;
    /**
     * 总金额
     */
    private double total;
    /**
     * 余额
     */
    private double balance;
    /**
     * 红包的用户标识
     */
    private String userId;

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "RedPackage{" +
                "pubKey='" + pubKey + '\'' +
                ", total=" + total +
                ", balance=" + balance +
                ", userId='" + userId + '\'' +
                '}';
    }
}
