package com.example.jacekmichalik.idomapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.jacekmichalik.idomapp.FloorMapPackage.FloorItemsList;
import com.example.jacekmichalik.idomapp.FloorMapPackage.SecurItemFragment;
import com.example.jacekmichalik.idomapp.JMTools.MessageBox;
import com.example.jacekmichalik.idomapp.JMTools.SMS_Czytacz;
import com.example.jacekmichalik.idomapp.JMTools.SmsHandler;

import static android.app.PendingIntent.getActivity;
import static com.example.jacekmichalik.idomapp.PagesAdapter.PAGE_MACROS;
import static com.example.jacekmichalik.idomapp.iDOmSettingsActivity.CNF_PHONE_NUMBER;
import static java.lang.Thread.sleep;




public class MainActivity extends AppCompatActivity
        implements SmsHandler,IDOMTaskNotyfikator,SecurItemFragment.OnListFragmentInteractionListener {

    final static int MY_PERMISSIONS_REQUEST_SEND_SMS = 991;
    private ViewPager   viewPager;
    private PagesAdapter pagesAdapter;
    private static Context     tmpContext;


    public static SharedPreferences prefs = null;
    public static IDOMDataManager IDOM = null;


    private TextView tempOutInfo ;
    private TextView tempINInfo ;
    private TextView lastEntryInfo;
    private ImageView partyModeImage;


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        tmpContext = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        ProgressBar sysProgressBar =(ProgressBar)findViewById(R.id.progressBar);

        tempOutInfo =findViewById(R.id.tOutTV);
        tempINInfo = findViewById(R.id.tempIN_TV);
        lastEntryInfo = findViewById(R.id.lastEntryTV);
        partyModeImage = findViewById(R.id.partyImage);

        lastEntryInfo.setText("pobieram dane:" + MainActivity.IDOM.IDOM_WWW);
        tempOutInfo.setText("");
        tempINInfo.setText("");

        IDOM = new IDOMDataManager(sysProgressBar);
        IDOM.setSysNotification(this);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        pagesAdapter= new PagesAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagesAdapter);// Set the adapter onto the view pager

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);// Give the TabLayout the ViewPager
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(PAGE_MACROS);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        registerSmsListener();// Register the broadcast receiver
        requestSmsPermission(); /* Make sure, we have the permissions */
        checkMyPermission(Manifest.permission.SEND_SMS);
        checkMyPermission(Manifest.permission.READ_PHONE_STATE);

    }

    private boolean checkMyPermission(String perm) {
        if (Build.VERSION.SDK_INT < 23)
            return true;
        if (ActivityCompat.checkSelfPermission(this, perm)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(getBaseContext(), "no way to send SMS", Toast.LENGTH_LONG).show();
                return false;
            } else {

                Toast.makeText(getBaseContext(), "wywyłam prośbę o zgodę:" + perm, Toast.LENGTH_LONG).show();

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{perm},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
                return false;
            }
        } else {
            return true;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getBaseContext(), "właśnie dostałeś zgodę", Toast.LENGTH_LONG).show();


                } else {

                    Toast.makeText(getBaseContext(), "ZAKAZANO WYSYŁKI", Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent configActivity;
            configActivity = new Intent(this, iDOmSettingsActivity.class);
            startActivity(configActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleSms(String sender, String message) {

        try {
            SharedPreferences sms_prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String tn = sms_prefs.getString(CNF_PHONE_NUMBER, "");

            if (sender.equals(tn)) {
                MessageBox.show(this, "Info:" + sender, message);
            } else {
            }
        } catch (Exception e) {
            Log.d("j23", e.toString());
        }
    }

    private void registerSmsListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.provider.Telephony.SMS_RECEIVED");
        SMS_Czytacz receiver = new SMS_Czytacz(this);
        registerReceiver(receiver, filter);
    }

    private void requestSmsPermission() {
        String permission = Manifest.permission.RECEIVE_SMS;
        int grant = ContextCompat.checkSelfPermission(this, permission);
        if (grant != PackageManager.PERMISSION_GRANTED) {
            String[] permission_list = new String[1];
            permission_list[0] = permission;
            ActivityCompat.requestPermissions(this, permission_list, 1);
        }
    }

    public static void mb(String msg) {
//        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        try {
            Toast.makeText(tmpContext, msg, Toast.LENGTH_LONG).show();
        }
        catch (Exception e){
            Log.d("j23","exception MainWindow.mb make Toast");
        }
    }

    @Override
    public void handleUpdated(String updateTAG, Object addInfo) {

        if ( updateTAG.equals(IDOMTaskNotyfikator.SYS_INFO) ) {

            lastEntryInfo.setText(MainActivity.IDOM.lastGateOpen);
            tempINInfo.setText(""+MainActivity.IDOM.tempIN + "°");
            tempOutInfo.setText(""+MainActivity.IDOM.tempOUT+ "°");
            if (MainActivity.IDOM.partyActive)
                DrawableCompat.setTint(
                        partyModeImage.getDrawable(),
                        ContextCompat.getColor(this, R.color.colorAccent));
            else
                DrawableCompat.setTint(
                        partyModeImage.getDrawable(),
                        Color.LTGRAY);

            pagesAdapter.notifyDataSetChanged();
        }

//        lastEntryInfo.setText(IDOM.getDiags());
    }

    @Override
    public void onListFragmentInteraction(FloorItemsList.SecurItemData item) {
//        Toast.makeText(getBaseContext(),item.toString(),Toast.LENGTH_LONG).show();
    }
}
