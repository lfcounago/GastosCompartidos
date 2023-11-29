package com.lfcounago.gastoscompartidos.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseItem {
    private String spendId;
    private String groupId;
    private String payerId;
    private double amount;
    private List<String> sharedWith;

    public ExpenseItem(String spendId,String groupId, String payerId, double amount, List<String> sharedWith){
        this.spendId = spendId;
        this.groupId = groupId;
        this.payerId = payerId;
        this.amount = amount;
        this.sharedWith = sharedWith;
    }

    public String getSpendId() {
        return spendId;
    }

    public void setSpendId(String spendId) {
        this.spendId = spendId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public List<String> getSharedWith() {
        return sharedWith;
    }

    public void setSharedWith(List<String> sharedWith) {
        this.sharedWith = sharedWith;
    }

}