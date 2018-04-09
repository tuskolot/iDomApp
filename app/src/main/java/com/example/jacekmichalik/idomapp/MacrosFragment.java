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


class RowMacroAdapter extends ArrayAdapter<MacrosFragment.RowMacroItem> {

    Context context;
    int rowLayoutResourceId;
    LinkedList<MacrosFragment.RowMacroItem> macros = null;

    public RowMacroAdapter(Context context, int rowLayoutResourceId, LinkedList<MacrosFragment.RowMacroItem> data) {
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

        MacrosFragment.RowMacroItem object = macros.get(position);
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

        final IDOMTaskNotyfikator tmpThis = this;

        macroListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener(){
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        MacrosFragment.RowMacroItem macro = MainActivity.IDOM.macrosList.get(position);
                        MainActivity.IDOM.runMacro(view.getContext(), macro.macro_id, macro.macro_name,
                                tmpThis);
                    }
                }
        );

        MainActivity.IDOM.importMacrosList(getContext(),this);
        return rootView;
    }

    @Override
    public void handleUpdated(String updateTAG, Object addInfo) {
//        Toast.makeText(getContext(),"finised: "+updateTAG,Toast.LENGTH_LONG).show();

        if ( updateTAG.equals(IDOMTaskNotyfikator.GET_MACROS) ) {
            RowMacroAdapter adapter = new RowMacroAdapter(macroListView.getContext(),
                    R.layout.macro_row_item, MainActivity.IDOM.macrosList);
            macroListView.setAdapter(adapter);
        }
    }

    @Override
    public void forceUpdate() {}

    public static class RowMacroItem {

        public int macro_id;
        public String macro_name;

        public RowMacroItem() {
        }

        public RowMacroItem(int macro_id, String macro_name) {

            this.macro_id = macro_id;
            this.macro_name = macro_name;
        }
    }

}
