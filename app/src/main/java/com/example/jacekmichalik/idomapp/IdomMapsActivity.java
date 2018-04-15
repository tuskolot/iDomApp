package com.example.jacekmichalik.idomapp;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class IdomMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idom_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
//                .findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
        mapView = findViewById(R.id.mapView);
                mapView.getMapAsync(this);

        mapView.onCreate(savedInstanceState);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this);
        mapView.onResume();
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
        mMap = googleMap;
        String lat,lon;

        Intent mi;
        mi =  getIntent();
        // Add a marker in Sydney and move the camera
        Toast.makeText(this,
                "Lat:"+mi.getDoubleExtra("lat",0)+
                " Lon:" + mi.getDoubleExtra("lon",0),Toast.LENGTH_LONG).show();
        LatLng sydney = new LatLng( mi.getDoubleExtra("lat",0), mi.getDoubleExtra("lon",0));
        mMap.addMarker(new MarkerOptions().position(sydney).title("Tu jesteś"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        mMap.animateCamera(CameraUpdateFactory.newLatLng(sydney),);
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 17.0f ) );

    }
}
