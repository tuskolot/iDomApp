package com.example.jacekmichalik.idomapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class MainActivity extends AppCompatActivity {


    private LinkedList<String> macroNamesList = new LinkedList<>();
    private LinkedList<Integer> macroIDList = new LinkedList<>();

    @BindView(R.id.macroListView)
    ListView macroListView;

    @BindView(R.id.tempTV)
    TextView tempInfo;

    @BindView(R.id.lastEntryTV)
    TextView lastEntryInfo;

    @BindView(R.id.iDomTV)
    TextView iDomInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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

        lastEntryInfo.setText("pobieram dane");
        tempInfo.setText("");
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void importMacrosList() {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.1.100/JSON/@GETMACROS";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
//                        mTextView.setText("Response is: "+ response.substring(0,500));
                        JSONArray ja = null;
                        JSONObject jo = null;
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

                        macroListView.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_activated_1, macroNamesList));

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                macroNamesList.add("error");
                macroNamesList.add(error.toString());
//                mTextView.setText("That didn't work!");
            }
        });
        queue.add(stringRequest);
    }

    private void importSysInfo() {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.1.100/JSON/@GETINFO";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jo = null;
                        try {
                            jo = new JSONObject(response);
                            tempInfo.setText(
                                    "IN:" + jo.getString("tempin") + " / " +
                                            "OUT:" + jo.getString("tempout")
                            );

                            lastEntryInfo.setText( jo.getString("lastgate") );
                            iDomInfo.setText( jo.getString("lastlogs") );

                        } catch (
                                Exception e)

                        {
                            tempInfo.setText("bad getInfo JSON !");
                        }
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
// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }


    private void runMacro(int macroID, final String macroName) {

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "http://192.168.1.100/JSON/@RUNMACRO$" + macroID;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getBaseContext(), macroName + " uruchomione", Toast.LENGTH_SHORT).show();
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
