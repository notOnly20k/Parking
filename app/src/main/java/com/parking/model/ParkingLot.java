package com.parking.model;

import java.util.List;

public class ParkingLot {
    private int id;
    private String name;
    private String location;
    private String lat;
    private String lan;
    private List<ParkingSpace> parkingSpaces;

    public ParkingLot() {
    }

    public ParkingLot(int id, String name, String location, String lat, String lan) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.lat = lat;
        this.lan = lan;
    }

    public List<ParkingSpace> getParkingSpaces() {
        return parkingSpaces;
    }

    public void setParkingSpaces(List<ParkingSpace> parkingSpaces) {
        this.parkingSpaces = parkingSpaces;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLan() {
        return lan;
    }

    public void setLan(String lan) {
        this.lan = lan;
    }

    @Override
    public String toString() {
        return "ParkingLot{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", lat='" + lat + '\'' +
                ", lan='" + lan + '\'' +
                ", parkingSpaces=" + parkingSpaces +
                '}';
    }
}
