package com.lfcounago.gastoscompartidos.core;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String groupId;
    private String groupName;
    private List<User> users;

    public Group(String groupId, String groupName, List<User> users){
        this.groupId = groupId;
        this.groupName = groupName;
        this.users = (users != null) ? users : new ArrayList<>();;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {

        this.users = (users != null) ? users : new ArrayList<>();
    }

    public void updateUserExpenses(String userId, List<String> expenses){
        if (users != null){
            for (User user : users){
                if(user != null & user.getUserId().equals(userId)){
                    user.setBalanceId(expenses);
                    break;
                }
            }
        } else {
            // Manejar el caso en que users es nulo
            Log.e("Group", "La lista de usuarios es nula");
        }

    }
}