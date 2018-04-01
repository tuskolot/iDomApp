package com.example.jacekmichalik.idomapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {


    private LinkedList<String> macroNamesList = new LinkedList<>();
    private LinkedList<Integer> macroIDList = new LinkedList<>();

    private String allLogs = ""; // pobrana historia z serwera
    private boolean isPartyActive = false; // pobrane z serwera

    public static String IDOM_WWW = "http://192.168.1.100/";

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

    private int pendingProcessCount = 0;

    private void updateProgressCounter(boolean incCounter) {
        if (incCounter)
            pendingProcessCount++;
        else if (pendingProcessCount > 0)
            pendingProcessCount--;

        if (pendingProcessCount > 0) {
            // coś się mieli w tle - pokaż licznik
            if (progressBar.getVisibility() != View.VISIBLE)
                progressBar.setVisibility(VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
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
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        fab.setVisibility(View.GONE);

        macroListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        int macroID = macroIDList.get(position);
                        String macroName = macroNamesList.get(position);
                        runMacro(macroID, macroName);
                    }
                }
        );

        allLogsImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent moreLogs = new Intent(getBaseContext(), LogosInfoActivity.class);
                moreLogs.putExtra("logs", allLogs);
                startActivity(moreLogs);
            }
        });

        lastEntryInfo.setText("pobieram dane:"+IDOM_WWW);
        tempOutInfo.setText("");
        tempINInfo.setText("");
        importMacrosList();
        importSysInfo();

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
            configActivity = new Intent(this , iDOmSettingsActivity.class );
            startActivity(configActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void importMacrosList() {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = IDOM_WWW+"JSON/@GETMACROS";

        updateProgressCounter(true);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
//                        mTextView.setText("Response is: "+ response.substring(0,500));
                        JSONArray ja;
                        JSONObject jo;
                        try {
                            ja = new JSONArray(response);
                            for (int i = 0; i < ja.length(); i++) {
                                jo = ja.getJSONObject(i);
                                macroNamesList.add(jo.getString("name"));
                                macroIDList.add(jo.getInt("id"));
                            }
                        } catch (JSONException e) {
                            macroNamesList.add("bad JSON!");
                        }

                        macroListView.setAdapter(new ArrayAdapter<>(getBaseContext(), android.R.layout.simple_list_item_activated_1, macroNamesList));
                        updateProgressCounter(false);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                macroNamesList.add("error");
                macroNamesList.add(error.toString());
//                mTextView.setText("That didn't work!");
                updateProgressCounter(false);
            }
        });
        queue.add(stringRequest);
    }

    private void importSysInfo() {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = IDOM_WWW+"JSON/@GETINFO";

        updateProgressCounter(true);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
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

                        } catch (
                                Exception e)

                        {
                            tempOutInfo.setText("bad getInfo JSON !");
                        }
                        updateProgressCounter(false);
                        if (isPartyActive)
                            DrawableCompat.setTint(
                                    partyModeImage.getDrawable(),
                                    ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                        else
                            DrawableCompat.setTint(
                                    partyModeImage.getDrawable(),
                                    Color.LTGRAY);


                    }
                }, new Response.ErrorListener()

        {
            @Override
            public void onErrorResponse(VolleyError error) {
                macroNamesList.add("error");
                macroNamesList.add(error.toString());
//                mTextView.setText("That didn't work!");
            }
        });
        queue.add(stringRequest);
    }


    private void runMacro(int macroID, final String macroName) {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = IDOM_WWW + "JSON/@RUNMACRO$" + macroID;

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
