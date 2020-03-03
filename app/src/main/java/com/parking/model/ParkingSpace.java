package com.parking.model;

import java.io.Serializable;

public class ParkingSpace implements Serializable {
    private int id;
    private String num;
    private int is_empty;
    private int parking_user_id;
    private String parking_car;

    public ParkingSpace() {
    }

    public ParkingSpace(int id, String num, int is_empty) {
        this.id = id;
        this.num = num;
        this.is_empty = is_empty;
    }

    public int getParking_user_id() {
        return parking_user_id;
    }

    public void setParking_user_id(int parking_user_id) {
        this.parking_user_id = parking_user_id;
    }

    public String getParking_car() {
        return parking_car;
    }

    public void setParking_car(String parking_car) {
        this.parking_car = parking_car;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public int getIs_empty() {
        return is_empty;
    }

    public void setIs_empty(int is_empty) {
        this.is_empty = is_empty;
    }

    @Override
    public String toString() {
        return "ParkingSpace{" +
                "id=" + id +
                ", num='" + num + '\'' +
                ", is_empty=" + is_empty +
                '}';
    }
}
