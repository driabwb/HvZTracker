package com.example.david.hvztracker;

import android.location.Location;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manage updates coming from the firebase system
 */
public class FirebaseManager {
    private TrackingActivity trackingActivity = null;
    private Firebase firebase = null;
    private long numDataFields = 4;

    public FirebaseManager(TrackingActivity ta){
        trackingActivity = ta;
        firebase = new Firebase("https://<firebase name>.firebaseio.com/");

        // When any user updates there status be informed
        firebase.child("users").addChildEventListener(new ChildEventListener() {
            // On any status update do the same thing
            private void onUpdate(DataSnapshot dataSnapshot){
                Log.i("Firebase", dataSnapshot.getValue().getClass().toString() + dataSnapshot.getValue().toString());
                List<PlayerDataObject> positions = new ArrayList<PlayerDataObject>();
                // Get the updated information
                Map<String, Object> playerData = (Map<String, Object>)dataSnapshot.getValue();
                positions.add(new PlayerDataObject(
                        (Double)playerData.get("Latitude"),
                        (Double)playerData.get("Longitude"),
                        (String)playerData.get("userId"),
                        (Boolean)playerData.get("isZombie")
                ));
                // Update the rest of the app
                trackingActivity.onOtherPlayerUpdate(positions);
            }

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                onUpdate(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                onUpdate(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                onUpdate(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                onUpdate(dataSnapshot);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void onLocationChanged(Location newLocation){
        PlayerDataObject player = new PlayerDataObject(newLocation.getLatitude(), newLocation.getLongitude(),
                trackingActivity.getUserId(), trackingActivity.getIsZombie());
        firebase.child("users").child(trackingActivity.getUserId()).setValue(player.getMap());
    }



}
