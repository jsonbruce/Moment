package com.bukeu.moment.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Max on 2015/4/14.
 */
public class UserList extends BaseModelList {

    private List<User> users = new ArrayList<>();

    public UserList() {}

    public UserList(List<User> users) {
        this.users = users;
    }

    public UserList(int page) {
        this.page = page;
    }

    public UserList(int page, List<User> users) {
        this.page = page;
        this.users = users;
    }

    public UserList(int page, int size, int count, List<User> users) {
        this.page = page;
        this.size = size;
        this.count = count;
        this.users = users;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
