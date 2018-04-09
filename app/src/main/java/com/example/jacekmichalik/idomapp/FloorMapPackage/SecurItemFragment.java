package com.example.jacekmichalik.idomapp.FloorMapPackage;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.jacekmichalik.idomapp.FloorMapPackage.FloorItemsList;
import com.example.jacekmichalik.idomapp.FloorMapPackage.MySecurItemRecyclerViewAdapter;
import com.example.jacekmichalik.idomapp.IDOMTaskNotyfikator;
import com.example.jacekmichalik.idomapp.MainActivity;
import com.example.jacekmichalik.idomapp.R;


// Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
public class SecurItemFragment extends Fragment implements IDOMTaskNotyfikator {

    private FloorItemsList floorMap = null;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView=null;

    public SecurItemFragment() {

        this.floorMap = null;
    }

    public SecurItemFragment(FloorItemsList floorMap) {

        this.floorMap = floorMap;
        Bundle b = new Bundle();
        b.putString("floor_name",floorMap.floorName);
        this.setArguments(b);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ( floorMap == null){
            Bundle b = getArguments();
            String floorName;
            floorName = b.getString("floor_name");
            floorMap = MainActivity.IDOM.floorMap.get(floorName);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_securitem_list, container, false);

        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            recyclerView = (RecyclerView) view;
//            column = 1
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
//            column = 2
//                recyclerView.setLayoutManager(new GridLayoutManager(context, XXX));
            recyclerView.setAdapter(new MySecurItemRecyclerViewAdapter(floorMap, mListener));

            recyclerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        try {
            MainActivity.IDOM.importFloorList(view.getContext(), this, floorMap.floorName);
        } catch (Exception e) {
            Log.d("j23", this.toString() + " MainActivity.IDOM.importFloorList");
        }

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void handleUpdated(String updateTAG, Object addInfo) {
        if (updateTAG.equals(IDOMTaskNotyfikator.GET_FLOOR)) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }

    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(FloorItemsList.SecurItemData item);
    }
}
