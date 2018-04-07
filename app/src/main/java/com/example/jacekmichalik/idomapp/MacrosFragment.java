package com.example.jacekmichalik.idomapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;


class RowMacroAdapter extends ArrayAdapter<IDOMDataManager.RowMacroItem> {

    Context context;
    int rowLayoutResourceId;
    LinkedList<IDOMDataManager.RowMacroItem> macros = null;

    public RowMacroAdapter(Context context, int rowLayoutResourceId, LinkedList<IDOMDataManager.RowMacroItem> data) {
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

        IDOMDataManager.RowMacroItem object = macros.get(position);
        holder.txtTitle.setText(object.macro_name);
        holder.imgIcon.setImageResource(android.R.drawable.ic_media_ff);
        return row;
    }

    static class RowMacroHolder {
        ImageView imgIcon;
        TextView txtTitle;
    }
}

public class MacrosFragment extends Fragment implements IDOMTaskNotyfikator{

    private ListView macroListView ;
    private TextView tempOutInfo ;
    private TextView tempINInfo ;
    private TextView lastEntryInfo;
    private TextView iDomInfo ;
    private ImageView partyModeImage;


    public MacrosFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.macros_fragment, container, false);

        macroListView = rootView.findViewById(R.id.macroListView);
        tempOutInfo =rootView.findViewById(R.id.tOutTV);
        tempINInfo = rootView.findViewById(R.id.tempIN_TV);
        lastEntryInfo = rootView.findViewById(R.id.lastEntryTV);
        iDomInfo = rootView.findViewById(R.id.iDomTV);
        partyModeImage = rootView.findViewById(R.id.partyImage);

        final IDOMTaskNotyfikator getNot = this;

        macroListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        IDOMDataManager.RowMacroItem macro = MainActivity.IDOM.macrosList.get(position);
                        MainActivity.IDOM.runMacro(view.getContext(), macro.macro_id, macro.macro_name,
                                getNot);
                    }
                }
        );

        lastEntryInfo.setText("pobieram dane:" + MainActivity.IDOM.IDOM_WWW);
        tempOutInfo.setText("");
        tempINInfo.setText("");

        MainActivity.IDOM.importMacrosList(getContext(),this);
        MainActivity.IDOM.importSysInfo(getContext(),this);
        return rootView;
    }

    @Override
    public void handleUpdated(String updateTAG) {
//        Toast.makeText(getContext(),"finised: "+updateTAG,Toast.LENGTH_LONG).show();

        if ( updateTAG.equals(IDOMTaskNotyfikator.GET_MACROS) ) {
            RowMacroAdapter adapter = new RowMacroAdapter(macroListView.getContext(),
                    R.layout.macro_row_item, MainActivity.IDOM.macrosList);
            macroListView.setAdapter(adapter);
        }

        if ( updateTAG.equals(IDOMTaskNotyfikator.SYS_INFO) ) {
            lastEntryInfo.setText(MainActivity.IDOM.lastGateOpen);
            tempINInfo.setText(""+MainActivity.IDOM.tempIN);
            tempOutInfo.setText(""+MainActivity.IDOM.tempOUT);
            if (MainActivity.IDOM.partyActive)
                DrawableCompat.setTint(
                        partyModeImage.getDrawable(),
                        ContextCompat.getColor(getContext(), R.color.colorAccent));
            else
                DrawableCompat.setTint(
                        partyModeImage.getDrawable(),
                        Color.LTGRAY);

        }

    }
}
