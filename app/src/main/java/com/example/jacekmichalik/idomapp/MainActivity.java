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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
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
import static com.example.jacekmichalik.idomapp.iDOmSettingsActivity.CNF_PHONE_NUMBER;
import static java.lang.Thread.sleep;


class RowMacroItem {

    public int macro_id;
    public String macro_name;

    public RowMacroItem() {
    }

    public RowMacroItem(int macro_id, String macro_name) {

        this.macro_id = macro_id;
        this.macro_name = macro_name;
    }
}

class RowMacroAdapter extends ArrayAdapter<RowMacroItem> {

    Context context;
    int rowLayoutResourceId;
    LinkedList<RowMacroItem> macros = null;

    public RowMacroAdapter(Context context, int rowLayoutResourceId, LinkedList<RowMacroItem> data) {
        super(context, rowLayoutResourceId, data);
        this.rowLayoutResourceId = rowLayoutResourceId;
        this.context = context;
        this.macros = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RowMacroHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            row = inflater.inflate(rowLayoutResourceId, parent, false);

            holder = new RowMacroHolder();
            holder.imgIcon = (ImageView) row.findViewById(R.id.run_macro_image);
            holder.txtTitle = (TextView) row.findViewById(R.id.row_macro_name);
            row.setTag(holder);
        } else {
            holder = (RowMacroHolder) row.getTag();
        }

        RowMacroItem object = macros.get(position);
        holder.txtTitle.setText(object.macro_name);
        holder.imgIcon.setImageResource(android.R.drawable.ic_media_ff);
        return row;
    }

    static class RowMacroHolder {
        ImageView imgIcon;
        TextView txtTitle;
    }
}

public class MainActivity extends AppCompatActivity implements SmsHandler {

    final static int MY_PERMISSIONS_REQUEST_SEND_SMS = 991;
    final static int MACROID_SEND_STATUS_REQUEST = 1001;   //  "makro" - wyślij SMS z zapytaniem o status

    private LinkedList<RowMacroItem> macrosList = new LinkedList<>();   // lista pobranych makr
    private String allLogs = ""; // historia pobrana z serwera
    private boolean isPartyActive = false; // status Party pobrany z serwera

    public static String IDOM_WWW = "http://192.168.1.100/";

    // zmienne robocze klasy
    LinkedList<RequestQueue> runningProcesses = new LinkedList<>(); // lista wątków aktualizacji

    // kompoenty
    @BindView(R.id.macroListView)
    ListView macroListView;
    @BindView(R.id.tOutTV)
    TextView tempOutInfo;
    @BindView(R.id.tempIN_TV)
    TextView tempINInfo;

    @BindView(R.id.lastEntryTV)
    TextView lastEntryInfo;
    @BindView(R.id.iDomTV)
    TextView iDomInfo;
    @BindView(R.id.showLoadingProgress)
    ProgressBar progressBar;
    @BindView(R.id.partyImage)
    ImageView partyModeImage;

    @BindView(R.id.moreLogsImage)
    ImageView allLogsImage;


    private void updateProgressCounter(RequestQueue job) {

        // pierwsze wywołanie - dodaje do listy
        // kolejne - usuwa z listy

        if (runningProcesses.contains(job)) {
            // usuń z listy
            runningProcesses.removeLastOccurrence(job);
            if (runningProcesses.size() <= 0) {
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            runningProcesses.add(job);
            // coś się mieli w tle - pokaż licznik
            if (progressBar.getVisibility() != View.VISIBLE)
                progressBar.setVisibility(VISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        IDOM_WWW = prefs.getString("json_serwver", IDOM_WWW);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });


        fab.setVisibility(View.GONE);

        macroListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener()

                {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        RowMacroItem macro = macrosList.get(position);
                        runMacro(macro.macro_id, macro.macro_name);
                    }
                }
        );

        allLogsImage.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v) {
                Intent moreLogs = new Intent(getBaseContext(), LogosInfoActivity.class);
                moreLogs.putExtra("logs", allLogs);
                startActivity(moreLogs);
            }
        });

        /* Register the broadcast receiver */
        registerSmsListener();

        /* Make sure, we have the permissions */
        requestSmsPermission();

        checkMyPermission(Manifest.permission.SEND_SMS);

        checkMyPermission(Manifest.permission.READ_PHONE_STATE);

        lastEntryInfo.setText("pobieram dane:" + IDOM_WWW);
        tempOutInfo.setText("");
        tempINInfo.setText("");

        importMacrosList();

        importSysInfo();

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


    private void importMacrosList() {

        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = IDOM_WWW + "JSON/@GETMACROS";
        final StringRequest stringRequest =
                new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                JSONArray ja;
                                JSONObject jo;
                                try {
                                    ja = new JSONArray(response);
                                    for (int i = 0; i < ja.length(); i++) {
                                        jo = ja.getJSONObject(i);
                                        macrosList.add(new RowMacroItem(
                                                jo.getInt("id"),
                                                jo.getString("name")));
                                    }
                                } catch (JSONException e) {
                                    macrosList.add(new RowMacroItem(0, "bad JSON"));
                                }

                                RowMacroAdapter adapter = new RowMacroAdapter(getBaseContext(),
                                        R.layout.macro_row_item, macrosList);
                                macroListView.setAdapter(adapter);
                                updateProgressCounter(queue);

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        updateProgressCounter(queue);
                    }
                });
        updateProgressCounter(queue);
        RowMacroItem m = new RowMacroItem(MACROID_SEND_STATUS_REQUEST, "pobierz status");
        macrosList.add(m);
        queue.add(stringRequest);
    }

    private void importSysInfo() {

        final RequestQueue queue = Volley.newRequestQueue(this);
        String url = IDOM_WWW + "JSON/@GETINFO";
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jo;
                        try {
                            jo = new JSONObject(response);
                            tempOutInfo.setText(jo.getString("tempout"));
                            tempINInfo.setText(jo.getString("tempin"));

                            lastEntryInfo.setText(jo.getString("lastgate"));
                            isPartyActive = jo.getString("isparty").equals("X");
                            allLogs = jo.getString("lastlogs");
                            iDomInfo.setText(allLogs);

                        } catch (Exception e) {
                            tempOutInfo.setText("bad getInfo JSON !");
                        }
                        updateProgressCounter(queue);
                        if (isPartyActive)
                            DrawableCompat.setTint(
                                    partyModeImage.getDrawable(),
                                    ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                        else
                            DrawableCompat.setTint(
                                    partyModeImage.getDrawable(),
                                    Color.LTGRAY);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                updateProgressCounter(queue);
            }
        });
        updateProgressCounter(queue);
        queue.add(stringRequest);
    }


    private void runMacro(int macroID, final String macroName) {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = IDOM_WWW + "JSON/@RUNMACRO$" + macroID;

        if (macroID > 999) {
            switch (macroID) {
                case MACROID_SEND_STATUS_REQUEST: {

                    SharedPreferences sms_prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    final String tn = sms_prefs.getString(CNF_PHONE_NUMBER, "");
                    MessageBox.ask(this, "Pytanie?", "Wysłać SMS do centralki?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            SmsManager sms = SmsManager.getDefault();
                            try {
                                sms.sendTextMessage(tn, null, "status", null, null);
                                mb("Wysłano zapytanie @" + tn);
                            } catch (Exception e) {
                                mb("Exc:" + e.toString());
                                return;
                            }

                        }
                    });


                }
                break;

            }

            //
        } else {

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(getBaseContext(), macroName + " uruchomione", Toast.LENGTH_SHORT).show();
                            importSysInfo();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getBaseContext(), macroName + " BŁĄD WYKONANIA", Toast.LENGTH_LONG).show();
                        }
                    });
            queue.add(stringRequest);
        }
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
        /* filter.setPriority(999); This is optional. */
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

    private void mb(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
