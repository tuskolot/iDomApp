package com.example.jacekmichalik.idomapp;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SysInfoFragment extends Fragment implements IDOMTaskNotyfikator, LocationManagerInterface {

    @BindView(R.id.allLogsTextView)
    TextView allLogsTV;
    private TextView diagInfo;
    private TextView gpsInfo;
    private Button showLocation;
    Location    myLastLocation;

    SmartLocationManager mLocationManager;

    //public static final String TAG = LocationActivity.class.getSimpleName();


    public SysInfoFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.sys_info_fragment, container, false);

        allLogsTV = rootView.findViewById(R.id.allLogsTextView);
        diagInfo = rootView.findViewById(R.id.diagInfo);
        gpsInfo = rootView.findViewById(R.id.locationTextView);
        showLocation = rootView.findViewById(R.id.showLocationButton);

        MainActivity.IDOM.importSysInfo(rootView.getContext(), this, false);

//        mLocationManager = new SmartLocationManager(rootView.getContext(), getActivity(),
//                this, SmartLocationManager.ALL_PROVIDERS,
//                LocationRequest.PRIORITY_HIGH_ACCURACY,
//                10 * 1000, 1
//                * 1000, SmartLocationManager.LOCATION_PROVIDER_RESTRICTION_NONE); // init location manager


        showLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do something here
//                mLocationManager.startLocationFetching();
                Intent mI= new Intent(getContext(), IdomMapsActivity.class);
                mI.putExtra("lat",myLastLocation.getLatitude());
                mI.putExtra("lon",myLastLocation.getLongitude());
                startActivity(mI);
            }
        });
        return rootView;
    }

    @Override
    public void handleUpdated(String updateTAG, Object addInfo) {
        allLogsTV.setText(MainActivity.IDOM.allLogs);
        diagInfo.setText(MainActivity.IDOM.getDiags());
    }

    @Override
    public void forceUpdate() {
        MainActivity.IDOM.importSysInfo(getView().getContext(), this, true);
    }

    @Override
    public void locationFetched(Location mLocation, Location oldLocation, String time, String locationProvider) {
        String s = "",t="";
        s = gpsInfo.getText().toString();
//        s = mLocation.toString() + "\n" + s;

        if ( s.length() > 300 )
            s = s.substring(0,300);

        myLastLocation = mLocation;
        t =  "Lat : " + mLocation.getLatitude() + " Lng : " + mLocation.getLongitude();
        t = t + "\nProvider: " + locationProvider;
        t = t + " @ " + time;
        gpsInfo.setText(t + "\n" + s);

    }

}
