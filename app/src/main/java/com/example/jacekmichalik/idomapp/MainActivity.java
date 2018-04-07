package com.example.jacekmichalik.idomapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import android.content.DialogInterface;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.PendingIntent.getActivity;
import static android.view.View.VISIBLE;
import static com.example.jacekmichalik.idomapp.PagesAdapter.PAGE_MACROS;
import static com.example.jacekmichalik.idomapp.iDOmSettingsActivity.CNF_PHONE_NUMBER;
import static java.lang.Thread.sleep;




public class MainActivity extends AppCompatActivity implements SmsHandler {

    final static int MY_PERMISSIONS_REQUEST_SEND_SMS = 991;
    private ViewPager   viewPager;
    private static Context     tmpContext;


    public static SharedPreferences prefs = null;
    public static IDOMDataManager IDOM = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        tmpContext = this;

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        PagesAdapter adapter = new PagesAdapter(getSupportFragmentManager());
        // Set the adapter onto the view pager
        viewPager.setAdapter(adapter);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(PAGE_MACROS);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ProgressBar sysProgressBar =(ProgressBar)findViewById(R.id.progressBar);
        IDOM = new IDOMDataManager(sysProgressBar);



        /* Register the broadcast receiver */
        registerSmsListener();

        /* Make sure, we have the permissions */
        requestSmsPermission();

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
            Log.d("catch", e.toString());
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
        Toast.makeText(tmpContext, msg, Toast.LENGTH_LONG).show();
    }
}
