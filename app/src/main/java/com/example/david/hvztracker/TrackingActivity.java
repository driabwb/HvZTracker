package com.example.david.hvztracker;

import android.graphics.Color;
import android.location.Location;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackingActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap = null;

    private String userId = "driabwb";
    private Boolean isZombie = false;

    private Marker currentLocation = null;
    private Circle[] circles = {null, null, null};
    private Map<String, PlayerDataObject> otherPlayers = null;
    private List<Marker> otherPlayerMarkers = null;

    private CurrentLocationManager currLocManger = null;
    private FirebaseManager firebaseManager = null;

    public String getUserId(){
        return userId;
    }

    public Boolean getIsZombie(){
        return isZombie;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        otherPlayers = new HashMap<String, PlayerDataObject>();

        currLocManger = new CurrentLocationManager(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Firebase.setAndroidContext(this);
        firebaseManager = new FirebaseManager(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Get the now ready Google Map
        mMap = googleMap;
        // Draw the map
        updateMap();
    }

    /*
     * Does all the drawing on the map
     */
    private void updateMap(){
        // If not created make a marker for the user on the map with the distance circles
        if(null == currentLocation) {
            // Use a default location
            LatLng defaultLatLng = new LatLng(0.0,0.0);
            // Create the users marker
            currentLocation = mMap.addMarker(new MarkerOptions().
                            position(defaultLatLng).
                            title("Me").
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
            );
            // Create circles for 100, 150, and 200m in red, magenta, and green respectively
            //    surrounding the users position
            circles[0] = mMap.addCircle(new CircleOptions().
                            center(defaultLatLng).radius(100).
                            fillColor(Color.TRANSPARENT).strokeColor(Color.RED)
            );
            circles[1] = mMap.addCircle(new CircleOptions().
                            center(defaultLatLng).radius(150).
                            fillColor(Color.TRANSPARENT).strokeColor(Color.MAGENTA)
            );
            circles[2] = mMap.addCircle(new CircleOptions().
                            center(defaultLatLng).radius(200).
                            fillColor(Color.TRANSPARENT).strokeColor(Color.GREEN)
            );
        }

        // if the other player's maker list in not create make it
        //    otherwise clear the markers and the list of them
        if(null == otherPlayerMarkers){
            otherPlayerMarkers = new ArrayList<Marker>();
        }else{
            for(Marker marker :otherPlayerMarkers){
                marker.remove();
            }
            otherPlayerMarkers.clear();
            Log.i("LocationUpdate", "Cleared old stuff");
        }
        // If there are known locations for other players draw add the markers
        if(null != otherPlayers) {
            for (String user : otherPlayers.keySet()) {
                Log.i("LocationUpdate", "Adding new Marker: " + user);
                // If the player is a zombie the marker is red otherwise blue
                if(otherPlayers.get(user).getIsZombie()) {
                    Log.i("LocationUpdate", "Updating " + user + ": and is a zombie");
                    otherPlayerMarkers.add(mMap.addMarker(new MarkerOptions().
                                    position(otherPlayers.get(user).getLatLng()).
                                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)).
                                    title(user)
                    ));
                }else{
                    Log.i("LocationUpdate", "Updating " + user + ": and is not a zombie");
                    otherPlayerMarkers.add(mMap.addMarker(new MarkerOptions().
                                    position(otherPlayers.get(user).getLatLng()).
                                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).
                                    title(user)
                    ));
                }
            }
        }
        // Make the map focus on the users position at a reasonable viewing height
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation.getPosition()));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
    }

    // When the GPS registers a new location for the user
    public void onLocationChanged(Location newLocation){
        // If it can update the app to draw the users position
        if(null != currentLocation) {
            LatLng newLatLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
            currentLocation.setPosition(newLatLng);
            for(Circle c : circles){
                c.setCenter(newLatLng);
            }
        }
        // Inform everyone else of the user's new position
        firebaseManager.onLocationChanged(newLocation);
        // If available update the map
        if(null != mMap) {
            updateMap();
        }
    }

    // When another players location updates
    public void onOtherPlayerUpdate(List<PlayerDataObject> newLocations){
        // update the other players map with new data
        for(PlayerDataObject dataObject : newLocations) {
            Log.i("PlayerId", dataObject.getUserId());
            Log.i("PlayerId", userId);
            // Don't add the user to the new players list
            if(!dataObject.getUserId().equals(userId)) {
                otherPlayers.put(dataObject.getUserId(), dataObject);
            }
        }
        Log.i("LocationUpdate", "Got new Locations: " + Integer.toString(newLocations.size()));
        Log.i("OtherPlayerLocation", otherPlayers.getClass().toString() + otherPlayers.toString());
        // If available update the map
        if(null != mMap) {
            updateMap();
        }
    }
}
