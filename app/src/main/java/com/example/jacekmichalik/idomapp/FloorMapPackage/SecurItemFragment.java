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

import com.example.jacekmichalik.idomapp.IDOMTaskNotyfikator;
import com.example.jacekmichalik.idomapp.MainActivity;
import com.example.jacekmichalik.idomapp.R;


// Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
public class SecurItemFragment extends Fragment implements IDOMTaskNotyfikator {

    private final static String ARG_FLOOR_NAME = "floor_name";
    private FloorItemsList floorMap = null;
    private OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView = null;

    public SecurItemFragment() {
    }

    public static SecurItemFragment instanceMe(String floor_name) {
        SecurItemFragment fragment = new SecurItemFragment();
        Bundle b = new Bundle();
        b.putString(ARG_FLOOR_NAME, floor_name);
        fragment.setArguments(b);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_securitem_list, container, false);

        Bundle bundle = getArguments();
        String floorName = bundle.getString(ARG_FLOOR_NAME);

        floorMap = MainActivity.IDOM.floorMap.get(floorName);
        if (null == floorMap) {
            // pierwsze uruchomienie - tworzymy obiekt i inicujemy jego załadowanie danymi
            floorMap = new FloorItemsList(floorName);
            MainActivity.IDOM.floorMap.put(floorName, floorMap);
            try {
                MainActivity.IDOM.importFloorList(view.getContext(), this, floorName, false);
            } catch (Exception e) {
                Log.d("j23", this.toString() + " MainActivity.IDOM.importFloorList");
            }
        }

        // Set the adapter of recyclerview
        recyclerView = view.findViewById(R.id.secur_item_list);
        if (recyclerView != null) {
            Context context = view.getContext();
//            column = 1
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
//            column = 2 recyclerView.setLayoutManager(new GridLayoutManager(context, XXX));
            recyclerView.setAdapter(new MySecurItemRecyclerViewAdapter(floorMap, mListener));
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
        // wołane po zakończeniu odczytu danych z serwera
        if (updateTAG.equals(IDOMTaskNotyfikator.GET_FLOOR)) {
            recyclerView.getAdapter().notifyDataSetChanged();
        }

    }

    @Override
    public void forceUpdate() {
        try {
            MainActivity.IDOM.importFloorList(getContext(), this, floorMap.floorName, true);
        } catch (Exception e) {
            Log.d("j23", e.toString());
        }
    }

    public interface OnListFragmentInteractionListener {
        void onListFragmentInteraction(FloorItemsList.SecurItemData item);
    }
}
