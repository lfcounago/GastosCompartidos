package com.lfcounago.gastoscompartidos.core;

import java.util.List;

public class User {
    private String userId;
    private String userName;
    private List<String> balanceId;
    private double totalBalance;
    private String currency;
    private  Double debtUser;

    public User (String userId, String userName, List<String> balanceId, String currency){
        this.userId = userId;
        this.userName = userName;
        this.balanceId = balanceId;
        this.totalBalance = 0.0;
        this.currency = currency;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getBalanceId() {
        return balanceId;
    }

    public void setBalanceId(List<String> balanceId) {
        this.balanceId = balanceId;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(Double totalBalance){
        this.totalBalance = totalBalance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getDebtUser() {
        return debtUser;
    }

    public void setDebtUser(Double debtUser) {
        this.debtUser = debtUser;
    }

    public String toString(){
        return this.userName;
    }
}