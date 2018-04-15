package com.example.jacekmichalik.idomapp.JMTools;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Switch;

import com.example.jacekmichalik.idomapp.MainActivity;
import com.example.jacekmichalik.idomapp.R;
import com.example.jacekmichalik.idomapp.SmartLocationManager;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.OnClick;

public class IdomDiagsActivity extends AppCompatActivity implements OnMapReadyCallback,
        SmartLocationManager.LocationManagerInterface {

    private GoogleMap mMap=null;
    private MapView mapView=null;
    SmartLocationManager mLocationManager = null;
    private Switch useRestSwitch = null ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_idom_diags);

        mapView = findViewById(R.id.mapView);
        mapView.getMapAsync(this);


        useRestSwitch= findViewById(R.id.useRESTSwitch);
        useRestSwitch.setChecked( MainActivity.IDOM.diag_UseRest());

        mapView.onCreate(savedInstanceState);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this);
        mapView.onResume();


        mLocationManager = new SmartLocationManager(getBaseContext(), this,
                this, SmartLocationManager.ALL_PROVIDERS,
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                10 * 1000, 1 * 1000,
                SmartLocationManager.LOCATION_PROVIDER_RESTRICTION_NONE);

        mLocationManager.startLocationFetching();

        useRestSwitch.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.IDOM.diag_setRestUse( useRestSwitch.isChecked() );
                MainActivity.mb("Zmieniono useRest");
            }
        });
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
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
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17.0f));
    }

    @Override
    public void locationFetched(Location mLocation, Location oldLocation, String time, String locationProvider) {
        LatLng pos = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
        if (null != mMap) {

            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(pos).title("Tu jesteś"));
//            mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(pos));
        }

    }

}
