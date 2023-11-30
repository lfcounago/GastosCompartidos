package com.lfcounago.gastoscompartidos.core;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String groupId;
    private String groupName;
    private List<User> users;

    public Group(String groupId, String groupName, List<User> users){
        this.groupId = groupId;
        this.groupName = groupName;
        this.users = (users != null) ? users : new ArrayList<>();
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
}