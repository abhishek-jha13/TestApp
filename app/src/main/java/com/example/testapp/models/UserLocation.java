package com.example.testapp.models;

import java.util.HashMap;
import java.util.Map;

public class UserLocation {
    private final String uid;
    private double latitude;
    private double longitude;
    private double altitude;
    private float speed;
    private float accuracy;
    private long time;
    public UserLocation(String uid, long time, double latitude, double longitude, double altitude, float speed, float accuracy){
        this.uid = uid;
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = speed;
        this.accuracy = accuracy;
    }



    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<String, Object>();
        //result.put("uid", uid);
        //result.put("time", time);
        result.put("latitude", latitude);
        result.put("longitude", longitude);
        result.put("altitude", altitude);
        result.put("speed", speed);
        result.put("accuracy", accuracy);

        return result;
    }

    public String getUid() {
        return uid;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public float getSpeed() {
        return speed;
    }

    public long getTime() {
        return time;
    }

    public float getAccuracy() {
        return accuracy;
    }
}
