package com.example.jacekmichalik.idomapp;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.jacekmichalik.idomapp.FloorMapPackage.FloorItemsList;
import com.example.jacekmichalik.idomapp.FloorMapPackage.MySecurItemRecyclerViewAdapter;
import com.example.jacekmichalik.idomapp.JMTools.ProfStr;
import com.google.android.gms.maps.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

import static android.view.View.VISIBLE;
import static com.example.jacekmichalik.idomapp.FloorMapPackage.FloorItemsList.TYPE_HEATER;
import static com.example.jacekmichalik.idomapp.FloorMapPackage.FloorItemsList.TYPE_LIGHT;
import static com.example.jacekmichalik.idomapp.iDOmSettingsActivity.CNF_PHONE_NUMBER;

public class IDOMDataManager {

    // zmienne diagnostyczne ######

    public void diag_add_stats() {
        String t = "";
        Calendar dat;
        Date dd;
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        DateFormat timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault());

        if (!diag_AddDummyOnError())
            return;

        allLogs="";

        for (int i = 0; i < 60; i++) {
            if (Math.round(Math.random() * 100) < 10) {
                dat = Calendar.getInstance();
                dat.add(Calendar.DATE, (int) (-Math.round(Math.random() * 10) - 5));
                dd = new Date(dat.getTimeInMillis());
                t = "\n" + dateFormat.format(dd) + "\n";
            } else {
                dat = Calendar.getInstance();
                dat.add(Calendar.SECOND, (int) (-Math.round(Math.random() * 10000) - 5));
                dd = new Date(dat.getTimeInMillis());
                try {
                    t = " " + timeFormat.format(dd);
                } catch (Exception e) {
                }

            }
            allLogs = allLogs + t;
        }
        lastGateOpen = "DIAG 18:30";
        tempIN = 23;
        tempOUT = 17;

        if (diag_AddDummyOnError()) {

            if ( floorArray.size() > 0 )
                return;

            for (int i = 0; i < 3; i++) {
                String fn = "pietro_" + i;
                floorArray.add(fn);
            }
        }


    }

    public void diag_add_macros() {

        if (!diag_AddDummyOnError())
            return;

        if (macrosList.size() > 2)
            return;

        for (int i = 0; i < 11; i++) {
            macrosList.add(new MacrosFragment.RowMacroItem(i, "Przykładowe makro: " + i));
        }
    }


    public void diag_add_floors(String floor_name,boolean forceRefresh) {

        if (!diag_AddDummyOnError())
            return;

        FloorItemsList fli = floorMap.get(floor_name);

        if (fli.getSize() > 0 && !forceRefresh)
            return;

        int items = (int) Math.round(Math.random() * 10 + 3);
        for (int j = 0; j < items; j++) {
            fli.addItem(
                    new FloorItemsList.SecurItemData(
                            "" +  j,
                            j % 3 == 0 ? TYPE_HEATER : TYPE_LIGHT,
                            "Element:" + j,
                            "pokój:" + ( j ) % 4,
                            ProfStr.random_my(3) == 0 ? "X" : " ",
                            ""
                    ));
        }
        fli.orderMe();
        floorMap.put(floor_name, fli);
    }


    public LinkedList<MacrosFragment.RowMacroItem> macrosList = new LinkedList<>();   // lista pobranych makr
    public String allLogs = ""; // historia pobrana z serwera
    public boolean partyActive = false; // status Party pobrany z serwera
    public int tempIN = 0;      //  temperatura wew
    public int tempOUT = 0;      //  temperatura zewnętrzna
    public String lastGateOpen = "";      //  ostatnie otwarcie bramy


    public static Map<String, Integer> jsonStatsistics = new HashMap<String, Integer>(); // statystyki wywołań jsonApi

    public LinkedList<String> floorArray = new LinkedList<>();
    public Map<String, FloorItemsList> floorMap = new HashMap<>();


    public static String IDOM_WWW = "http://192.168.1.100/";
    final static int MACROID_SEND_STATUS_REQUEST = 1001;   //  "makro" - wyślij SMS z zapytaniem o status

    LinkedList<RequestQueue> runningProcesses = new LinkedList<>(); // lista wątków aktualizacji

    private ProgressBar dataLoaderPB;
    private IDOMTaskNotyfikator sysChangeNotify = null;

    public IDOMDataManager(ProgressBar dataLoaderPB) {
        this.dataLoaderPB = dataLoaderPB;
        IDOM_WWW = MainActivity.prefs.getString("json_serwver", IDOM_WWW);
        if (dataLoaderPB != null)
            dataLoaderPB.setVisibility(View.INVISIBLE);
    }


    public void setSysNotification(IDOMTaskNotyfikator idomTaskNotyfikator) {
        this.sysChangeNotify = idomTaskNotyfikator;
    }

    private void sysChange() {
        if (sysChangeNotify != null)
            callNotyficator(sysChangeNotify, IDOMTaskNotyfikator.SYS_INFO, null);
    }

    private void callNotyficator(IDOMTaskNotyfikator idomTaskNotyfikator, String tag, Object addInfo) {
        if (idomTaskNotyfikator != null) {
            try {
                idomTaskNotyfikator.handleUpdated(tag, addInfo);
            } catch (Exception e) {
                Log.d("j23", e.toString());
            }
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

        String url = IDOM_WWW + "JSON/@GETMACROS";

        if (macrosList.size() >= 1) {
            // już raz pobraliśmy listę - nie pobieraj jej ponownie
            callNotyficator(idomTaskNotyfikator, IDOMTaskNotyfikator.GET_MACROS, null);
            return;
        }

        if ( !diag_UseRest() ) {
            diag_add_macros();
            callNotyficator(idomTaskNotyfikator, IDOMTaskNotyfikator.GET_MACROS, null);
            return;
        }

        final RequestQueue queue = Volley.newRequestQueue(context);

        macrosList.add(new MacrosFragment.RowMacroItem(MACROID_SEND_STATUS_REQUEST, "SMS: pobierz status"));

        incDiags(IDOMTaskNotyfikator.GET_MACROS);

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
                                        macrosList.add(new MacrosFragment.RowMacroItem(
                                                jo.getInt("id"),
                                                jo.getString("name")));
                                    }
                                } catch (JSONException e) {
                                    Log.d("j23", " importMacrosList - getMacros: badJSON ");
                                }
                                updateProgressCounter(queue);
                                callNotyficator(idomTaskNotyfikator, IDOMTaskNotyfikator.GET_MACROS, null);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        updateProgressCounter(queue);
                        diag_add_macros();
                        callNotyficator(idomTaskNotyfikator, IDOMTaskNotyfikator.GET_MACROS, null);

                    }
                });
        updateProgressCounter(queue);
        queue.add(stringRequest);
    }

    public void importFloorList(Context context, final IDOMTaskNotyfikator idomTaskNotyfikator, final String floorName, final boolean forceRefresh) {

        String url = IDOM_WWW + "JSON/@GETFLOOR$" + floorName;
        final FloorItemsList floorItemsList = floorMap.get(floorName);
        if (null == floorItemsList)
            return;

        if (floorItemsList.getSize() > 0 && !forceRefresh) //  struktura była już wcześniej odczytana
        {
            callNotyficator(idomTaskNotyfikator, IDOMTaskNotyfikator.GET_FLOOR, null);
            return;
        }

        if ( !diag_UseRest()  ) {
            diag_add_floors(floorName, forceRefresh);
            callNotyficator(idomTaskNotyfikator, IDOMTaskNotyfikator.GET_FLOOR, null);
            return;
        }

        final RequestQueue queue = Volley.newRequestQueue(context);

        incDiags(IDOMTaskNotyfikator.GET_FLOOR);

        final StringRequest stringRequest =
                new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                JSONArray ja;
                                JSONObject jo;
                                FloorItemsList.SecurItemData si;

                                if ( forceRefresh )
                                    floorItemsList.clearAll();

                                try {
                                    ja = new JSONArray(response);
                                    for (int i = 0; i < ja.length(); i++) {
                                        jo = ja.getJSONObject(i);
                                        si = new FloorItemsList.SecurItemData(
                                                jo.getString("id"),
                                                jo.getString("type"),
                                                jo.getString("name"),
                                                jo.getString("room"),
                                                jo.getString("state"),
                                                jo.getString("add_info")
                                                );
                                        floorItemsList.addItem(si);

                                    }
                                } catch (JSONException e) {
                                    Log.d("j23", " getFloors: badJSON \n" + e.toString());
                                }
                                updateProgressCounter(queue);
                                floorItemsList.orderMe();
                                callNotyficator(idomTaskNotyfikator, IDOMTaskNotyfikator.GET_FLOOR, null);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        updateProgressCounter(queue);
                        diag_add_floors(floorName,forceRefresh);
                        callNotyficator(idomTaskNotyfikator, IDOMTaskNotyfikator.GET_FLOOR, null);

                    }
                });
        updateProgressCounter(queue);
        queue.add(stringRequest);
    }

    private void updateFloors(String floors) {
        int col;
        String t, v;

        if ( floorArray.size() > 0 )
            return; // już raz pobraliśmy listę pięter. Ona się nie zmienia

        v = floors.trim();
        while (!v.isEmpty()) {
            col = v.indexOf(";");
            t = "";
            if (col < 0) {
                t = v;
                v = "";
            } else {
                t = v.substring(0, col);
                v = v.substring(col + 1, v.length());
            }
            t = t.trim();
            v = v.trim();
            if (!t.isEmpty()) {
                floorArray.add(t);
//                floorMap.put(t, new FloorItemsList(t));
            }
        }
    }

    public void importSysInfo(Context context, final IDOMTaskNotyfikator idomTaskNotyfikator, final boolean forceRefresh) {

        String url = IDOM_WWW + "JSON/@GETINFO";

        if ( allLogs.length() > 0 && !forceRefresh){
            // już raz pobrano dane
            callNotyficator(idomTaskNotyfikator, IDOMTaskNotyfikator.SYS_INFO, null);
            sysChange();
            return;
        }

        if ( !diag_UseRest()  ) {
            diag_add_stats();
            callNotyficator(idomTaskNotyfikator, IDOMTaskNotyfikator.SYS_INFO, null);
            sysChange();
            return;
        }

        final RequestQueue queue = Volley.newRequestQueue(context);
        incDiags(IDOMTaskNotyfikator.SYS_INFO);

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
                            updateFloors(jo.getString("floors"));
                            callNotyficator(idomTaskNotyfikator, IDOMTaskNotyfikator.SYS_INFO, null);
                            sysChange();

                        } catch (Exception e) {
                            Log.d("j23", "sysInfo:bad JSON ! ");
                        }
                        updateProgressCounter(queue);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                diag_add_stats();
                callNotyficator(idomTaskNotyfikator, IDOMTaskNotyfikator.SYS_INFO, null);
                sysChange();
                updateProgressCounter(queue);
            }
        });
        updateProgressCounter(queue);
        queue.add(stringRequest);
    }

    public void runMacro(final Context context, int macroID, final String macroName, final IDOMTaskNotyfikator idomTaskNotyfikator) {

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = IDOM_WWW + "JSON/@RUNMACRO$" + macroID;

        incDiags(IDOMTaskNotyfikator.RUN_MACRO);
        if (macroID > 999) {
            switch (macroID) {
                case MACROID_SEND_STATUS_REQUEST: {

                    final String tn = MainActivity.prefs.getString(CNF_PHONE_NUMBER, "");
                    ProfStr.ask(context, "Pytanie?", "Wysłać SMS do centralki?", new DialogInterface.OnClickListener() {
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
                            importSysInfo(context, idomTaskNotyfikator,true);
                            callNotyficator(idomTaskNotyfikator, IDOMTaskNotyfikator.RUN_MACRO, null);
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

    public void turnLight(final Context context, final MySecurItemRecyclerViewAdapter.ViewHolder si, final IDOMTaskNotyfikator idomTaskNotyfikator) {

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = IDOM_WWW + "JSON/@LIGHT$" + si.mItem.securID;

        incDiags(IDOMTaskNotyfikator.LIGHT);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jo;
                        String new_state = "";
                        try {
                            jo = new JSONObject(response);
                            new_state = jo.getString("result");
                            switch (new_state) {
                                case "X":
                                    si.mItem.state = "X";
                                    break;
                                default:
                                    si.mItem.state = " ";
                                    break;

                            }
                        } catch (Exception e) {
                            Log.d("j23", "turnLight:bad JSON ! ");
                        }
                        callNotyficator(idomTaskNotyfikator, IDOMTaskNotyfikator.LIGHT, si);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Light:" + si.mItem.securID + " BŁĄD WYKONANIA", Toast.LENGTH_LONG).show();
                    }
                });
        queue.add(stringRequest);
    }

    private static void incDiags(String tag) {

        Integer i = null;
        i = jsonStatsistics.get(tag);
        if (i == null) {
            jsonStatsistics.put(tag, 1);
        } else
            jsonStatsistics.put(tag, new Integer(i + 1));
    }

    public static String getDiags() {
        return
                "GM: " + jsonStatsistics.get(IDOMTaskNotyfikator.GET_MACROS) + "/" +
                        "SI: " + jsonStatsistics.get(IDOMTaskNotyfikator.SYS_INFO) + "/" +
                        "RM: " + jsonStatsistics.get(IDOMTaskNotyfikator.RUN_MACRO) + "/" +
                        "GF: " + jsonStatsistics.get(IDOMTaskNotyfikator.GET_FLOOR);
    }

    public String getFloorName(int floor_idx) {
        if (floor_idx < 0 || floor_idx >= floorArray.size())
            return "?fllor:" + floor_idx;
        else
            return floorArray.get(floor_idx).toString();
    }


    public boolean diag_IsON() {
        try {
            if (null == MainActivity.prefs) return false;
            return MainActivity.prefs.getBoolean("show_diaga", false);
        }catch (Exception e){ return false;}
    }

    public boolean diag_UseRest() {
        try {
            if (null == MainActivity.prefs) return false;
            return MainActivity.prefs.getBoolean("useREST", true);
        }catch (Exception e){ return false;}
    }

    public void diag_setRestUse(boolean use_rest) {
        try {
            if (null == MainActivity.prefs) return ;
            MainActivity.prefs.edit().putBoolean("useREST", use_rest).commit();
        }catch (Exception e){ Log.d("j23","diag_setRestUse");}
    }

    public boolean diag_AddDummyOnError() {
        try {
            if (null == MainActivity.prefs) return false;
            return MainActivity.prefs.getBoolean("dummyOnError", true);
        }catch (Exception e){ return false;}
    }


}
