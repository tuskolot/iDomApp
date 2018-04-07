package com.example.jacekmichalik.idomapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import static android.view.View.VISIBLE;
import static com.example.jacekmichalik.idomapp.iDOmSettingsActivity.CNF_PHONE_NUMBER;

public class IDOMDataManager {

    public LinkedList<RowMacroItem> macrosList = new LinkedList<>();   // lista pobranych makr
    public String allLogs = ""; // historia pobrana z serwera
    public boolean partyActive = false; // status Party pobrany z serwera
    public int tempIN = 0;      //  temperatura wew
    public int tempOUT = 0;      //  temperatura zewnętrzna
    public String lastGateOpen = "";      //  ostatnie otwarcie bramy
    public String connectStr = "...";  //  wynik odpytania serwera

    public static String IDOM_WWW = "http://192.168.1.100/";
    final static int MACROID_SEND_STATUS_REQUEST = 1001;   //  "makro" - wyślij SMS z zapytaniem o status

    LinkedList<RequestQueue> runningProcesses = new LinkedList<>(); // lista wątków aktualizacji

    private ProgressBar dataLoaderPB;

    public IDOMDataManager(ProgressBar dataLoaderPB) {
        this.dataLoaderPB = dataLoaderPB;
        IDOM_WWW = MainActivity.prefs.getString("json_serwver", IDOM_WWW);
        if (dataLoaderPB != null)
            dataLoaderPB.setVisibility(View.INVISIBLE);
    }

    public class RowMacroItem {

        public int macro_id;
        public String macro_name;

        public RowMacroItem() {
        }

        public RowMacroItem(int macro_id, String macro_name) {

            this.macro_id = macro_id;
            this.macro_name = macro_name;
        }
    }

    public void updateProgressCounter(RequestQueue job) {

        // pierwsze wywołanie - dodaje do listy
        // kolejne - usuwa z listy

        if (runningProcesses.contains(job)) {
            // usuń z listy
            runningProcesses.removeLastOccurrence(job);
            if (runningProcesses.size() <= 0) {
                if (dataLoaderPB != null)
                    dataLoaderPB.setVisibility(View.INVISIBLE);
            }
        } else {
            runningProcesses.add(job);
            // coś się mieli w tle - pokaż licznik
            if (dataLoaderPB != null)
                if (dataLoaderPB.getVisibility() != View.VISIBLE)
                    dataLoaderPB.setVisibility(VISIBLE);
        }
    }

    public void importMacrosList(Context context, final IDOMTaskNotyfikator idomTaskNotyfikator) {

        final RequestQueue queue = Volley.newRequestQueue(context);
        String url = IDOM_WWW + "JSON/@GETMACROS";
        macrosList.add(new RowMacroItem(MACROID_SEND_STATUS_REQUEST, "pobierz status"));

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
                                    connectStr = connectStr + " getMacros: badJSON ";
                                }

                                updateProgressCounter(queue);
                                idomTaskNotyfikator.handleUpdated(IDOMTaskNotyfikator.GET_MACROS);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        updateProgressCounter(queue);
                        connectStr = connectStr + " getMacros: volError";
                        idomTaskNotyfikator.handleUpdated(IDOMTaskNotyfikator.GET_MACROS);

                    }
                });
        updateProgressCounter(queue);
        queue.add(stringRequest);
    }

    public void importSysInfo(Context context, final IDOMTaskNotyfikator idomTaskNotyfikator) {

        final RequestQueue queue = Volley.newRequestQueue(context);
        String url = IDOM_WWW + "JSON/@GETINFO";
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jo;
                        try {
                            jo = new JSONObject(response);
                            tempOUT = jo.getInt("tempout");
                            tempIN = jo.getInt("tempin");

                            lastGateOpen = jo.getString("lastgate");
                            partyActive = jo.getString("isparty").equals("X");
                            allLogs = jo.getString("lastlogs");
                            idomTaskNotyfikator.handleUpdated(IDOMTaskNotyfikator.SYS_INFO);

                        } catch (Exception e) {
                            connectStr = connectStr + "sysInfo:bad JSON ! ";
                        }
                        updateProgressCounter(queue);


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                updateProgressCounter(queue);
                connectStr = connectStr + " sysInf: errListener ";
            }
        });
        updateProgressCounter(queue);
        queue.add(stringRequest);
    }


    public void runMacro(final Context context, int macroID, final String macroName, final IDOMTaskNotyfikator idomTaskNotyfikator) {

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = IDOM_WWW + "JSON/@RUNMACRO$" + macroID;

        if (macroID > 999) {
            switch (macroID) {
                case MACROID_SEND_STATUS_REQUEST: {

                    final String tn = MainActivity.prefs.getString(CNF_PHONE_NUMBER, "");
                    MessageBox.ask(context, "Pytanie?", "Wysłać SMS do centralki?", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            SmsManager sms = SmsManager.getDefault();
                            try {
                                sms.sendTextMessage(tn, null, "status", null, null);
                                MainActivity.mb("Wysłano zapytanie @" + tn);
                            } catch (Exception e) {
                                MainActivity.mb("Exc:" + e.toString());
                                return;
                            }

                        }
                    });
                }
                break;
            }
        } else {

            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(context, macroName + " uruchomione", Toast.LENGTH_SHORT).show();
                            importSysInfo(context,idomTaskNotyfikator);
                            idomTaskNotyfikator.handleUpdated(IDOMTaskNotyfikator.RUN_MACRO);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(context, macroName + " BŁĄD WYKONANIA", Toast.LENGTH_LONG).show();
                        }
                    });
            queue.add(stringRequest);
        }
    }

}
