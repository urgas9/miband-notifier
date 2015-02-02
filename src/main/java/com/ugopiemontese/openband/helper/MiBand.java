package com.ugopiemontese.openband.helper;

/**
 * Created by UgoRaffaele on 30/01/2015.
 */
public class MiBand {

    private String name;
    private String address;
    private Battery battery;
    private int steps;
    private String firmware;

    public MiBand(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public Battery getBattery() {
        return battery;
    }

    public void setBattery(Battery battery) {
        this.battery = battery;
    }

    public String getFirmware() {
        return firmware;
    }

    public void setFirmware(byte[] firmware) {
        if (firmware.length == 4)
            this.firmware = firmware[3] + "." + firmware[2] + "." + firmware[1] + "." + firmware[0];
    }

}
