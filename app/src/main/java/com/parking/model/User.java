package com.parking.model;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String name;
    private String car;
    private String password;
    private String account;

    public User() {
    }

    public User(int id, String name, String car, String password, String account) {
        this.id = id;
        this.name = name;
        this.car = car;
        this.password = password;
        this.account = account;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCar() {
        return car;
    }

    public void setCar(String car) {
        this.car = car;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
