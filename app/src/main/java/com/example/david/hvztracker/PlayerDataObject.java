package com.example.david.hvztracker;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Map;

/**
 * Player Data Aggregation Object
 */
public class PlayerDataObject {
    private String userId = null;
    private Double latitude = 0.0;
    private Double longitude = 0.0;
    private Boolean isZombie = false;

    public PlayerDataObject(Double lat, Double lon, String user, Boolean isZombie){
        latitude = lat;
        longitude = lon;
        userId = user;
        this.isZombie = isZombie;
    }

    // For sending player data to firebase
    public Map<String, Object> getMap(){
        Map<String, Object> retMap = new HashMap<String, Object>();
        retMap.put("Latitude", latitude);
        retMap.put("Longitude", longitude);
        retMap.put("userId", userId);
        retMap.put("isZombie", isZombie);
        return retMap;
    }

    public String getUserId(){
        return userId;
    }

    public Double getLatitude(){
        return latitude;
    }

    public Double getLongitude(){
        return longitude;
    }

    // More convenient ingestion by the Map API
    public LatLng getLatLng(){
        return new LatLng(latitude, longitude);
    }

    public Boolean getIsZombie(){
        return isZombie;
    }

    public String toString(){
        return "{Latitude: " + Double.toString(latitude) +
                ", Longitude: " + Double.toString(longitude) +
                ", userId: " + userId +
                ", isZombie: " + Boolean.toString(isZombie) + "}";
    }
}
