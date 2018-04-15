package com.example.jacekmichalik.idomapp;

import android.content.Intent;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.jacekmichalik.idomapp.JMTools.IdomDiagsActivity;

import butterknife.BindView;


public class SysInfoFragment extends Fragment implements IDOMTaskNotyfikator {

    @BindView(R.id.allLogsTextView)
    TextView allLogsTV;
    private TextView diagInfo;

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

        MainActivity.IDOM.importSysInfo(rootView.getContext(), this, false);


        diagInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do something here
                Intent intent;

                if (MainActivity.IDOM.diag_IsON()) {
                    intent = new Intent(getContext(), IdomDiagsActivity.class);
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        MainActivity.mb("Fail...");
                        Log.d("j23",e.toString());

                    }
                } else {
                    MainActivity.mb("włącz diagnostykę !");
                }
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


}
