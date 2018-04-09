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

        MainActivity.IDOM.importSysInfo(rootView.getContext(), this,false);

        return rootView;
    }

    @Override
    public void handleUpdated(String updateTAG, Object addInfo) {
        allLogsTV.setText(MainActivity.IDOM.allLogs);
        diagInfo.setText(MainActivity.IDOM.getDiags());
    }

    @Override
    public void forceUpdate() {
        MainActivity.IDOM.importSysInfo( getView().getContext(), this,true);
    }
}
