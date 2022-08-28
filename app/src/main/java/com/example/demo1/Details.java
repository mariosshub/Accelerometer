package com.example.demo1;

public class Details {
    private final double lat;
    private final double lon;
    private final float speed;
    private final long time;
    private final double acceleration;

    public Details(double lat, double lon, float speed, long time, double acceleration) {
        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.time = time;
        this.acceleration = acceleration;
    }

    public double getLat() {
        return lat;
    }
    public double getLon() {
        return lon;
    }
    public float getSpeed() {
        return speed;
    }
    public long getTime() {
        return time;
    }
    public double getAcceleration() {
        return acceleration;
    }
}
