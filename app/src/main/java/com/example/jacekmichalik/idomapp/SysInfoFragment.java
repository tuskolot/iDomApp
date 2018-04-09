package com.example.jacekmichalik.idomapp;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SysInfoFragment extends Fragment implements IDOMTaskNotyfikator {

    @BindView(R.id.allLogsTextView)
    TextView allLogsTV;

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
        MainActivity.IDOM.importSysInfo(rootView.getContext(), this);

        return rootView;
    }

    @Override
    public void handleUpdated(String updateTAG, Object addInfo) {
        allLogsTV.setText(MainActivity.IDOM.allLogs);
    }
}
