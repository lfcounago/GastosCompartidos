package com.lfcounago.gastoscompartidos.core;

import java.util.List;

public class User {
    private String userId;
    private String userName;
    private List<String> balanceId;
    private double totalBalance;

    public User (String userId, String userName, List<String> balanceId){
        this.userId = userId;
        this.userName = userName;
        this.balanceId = balanceId;
        this.totalBalance = 0.0;
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

    public double calcularSaldo(List<ExpenseItem> expenseItems){
        double saldo = 0;

        for (ExpenseItem expenseItem: expenseItems){
            if (expenseItem.getPayerId().equals(userId)){
                //Si el usuario pagó el gasto, restar el total del gasto
                saldo -= expenseItem.getAmount();
            } else if (expenseItem.getSharedWith().contains(userId)) {
                //Si el usuario compartió el gasto, sumar su parte al saldo
                saldo += expenseItem.getAmount()/expenseItem.getSharedWith().size();
            }
        }
        return saldo;
    }
    public void actualizarSaldo(List<ExpenseItem> expenseItems) {
        // Llamar a calcularSaldo para obtener el nuevo saldo
        double nuevoSaldo = calcularSaldo(expenseItems);
    }

    public void setTotalBalance(Double totalBalance){
        this.totalBalance = totalBalance;
    }
}