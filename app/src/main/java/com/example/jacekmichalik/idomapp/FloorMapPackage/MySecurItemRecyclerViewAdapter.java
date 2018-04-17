package com.example.jacekmichalik.idomapp.FloorMapPackage;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.jacekmichalik.idomapp.IDOMTaskNotyfikator;
import com.example.jacekmichalik.idomapp.JMTools.ProfStr;
import com.example.jacekmichalik.idomapp.MainActivity;
import com.example.jacekmichalik.idomapp.R;

import java.util.Collection;

import static com.example.jacekmichalik.idomapp.FloorMapPackage.FloorItemsList.TYPE_HEATER;
import static com.example.jacekmichalik.idomapp.FloorMapPackage.FloorItemsList.TYPE_LIGHT;
import static com.example.jacekmichalik.idomapp.FloorMapPackage.FloorItemsList.TYPE_ROOM;

public class MySecurItemRecyclerViewAdapter extends RecyclerView.Adapter<MySecurItemRecyclerViewAdapter.ViewHolder>
        implements IDOMTaskNotyfikator{

    private final FloorItemsList mValues;
    private final SecurItemFragment.OnListFragmentInteractionListener mListener;


    public MySecurItemRecyclerViewAdapter(FloorItemsList items, SecurItemFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        if (null == mValues)
            Log.d("j23", this.toString() + " mValues = null");
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_securitem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        if (null == mValues) {
            Log.d("j23", this.toString() + "mValues = null");
            return;
        }

        holder.mItem = mValues.get(position);
        holder.mItemNameView.setText(holder.mItem.name );
        holder.mItemDetailView.setText(holder.mItem.roomName + ", @" + holder.mItem.securID + ", " + holder.mItem.type);
        holder.position = position;

        if (holder.mItem.type.equals(TYPE_HEATER)) {
            holder.mItemTypeImageView.setImageResource(R.mipmap.ic_heater_foreground);
        }

        if (holder.mItem.type.equals(TYPE_LIGHT)) {
            updateLightState(holder.mItemTypeImageView, holder.mItem.state.equals("X"));
            holder.mItemTypeImageView.setVisibility(View.VISIBLE);
            holder.mItemNameView.setTextColor(Color.DKGRAY);
            holder.mItemNameView.setTextSize(16);
        }

        if (holder.mItem.type.equals(TYPE_ROOM)) {
            holder.mItemNameView.setText(holder.mItem.name + "   "+holder.mItem.addInfo);
            holder.mItemTypeImageView.setVisibility(View.GONE);
            holder.mItemNameView.setTextColor(Color.GRAY);
            holder.mItemNameView.setTextSize(13);
        }

        final MySecurItemRecyclerViewAdapter tempNot = this;

        if (holder.mItem.type.equals(TYPE_LIGHT)) {
            View.OnClickListener onClickListener = new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.

                        if (holder.mItem.type.equals(TYPE_LIGHT)) {
                            MainActivity.IDOM.turnLight(v.getContext(), holder, tempNot);
                            mListener.onListFragmentInteraction(holder.mItem);

                            try {
                                updateLightState(holder.mItemTypeImageView, !holder.mItem.state.equals("X"));
                            } catch (Exception e) {
                                Log.d("j23", "updateLightState" + e.toString());
                            }
                        }

                    }
                }
            };

            holder.mItemTypeImageView.setOnClickListener(onClickListener);
            holder.mView.setOnClickListener(onClickListener);
        }

    }

    private void updateLightState(ImageView imageView, boolean light_state) {
        try {
            if (light_state) {
                imageView.setImageResource(R.mipmap.ic_lamp_bulb_on_front);
            } else {
                imageView.setImageResource(R.mipmap.ic_lamp_bulb_off);
            }
        } catch (Exception e) {
            Log.d("j23", "updateLightState" + e.toString());
        }
        if (imageView.getVisibility() != View.VISIBLE)
            imageView.setVisibility(View.VISIBLE);

    }

    @Override
    public int getItemCount() {
        if (null == mValues) {
            return 0;
        }
        return mValues.getSize();
    }

    @Override
    public void handleUpdated(String updateTAG, Object addInfo) {
        if (null != addInfo)
            if (addInfo instanceof ViewHolder) {
                ViewHolder item = (ViewHolder) addInfo;

                notifyItemChanged(item.position);
            }

    }

    @Override
    public void forceUpdate() {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        public int position = 0;
        public final TextView mItemNameView;
        public final TextView mItemDetailView;
        public final ImageView mItemTypeImageView;

        public FloorItemsList.SecurItemData mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mItemNameView = (TextView) view.findViewById(R.id.item_name);
            mItemDetailView = (TextView) view.findViewById(R.id.item_details);
            mItemTypeImageView = (ImageView) view.findViewById(R.id.securItemTypeImage);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mItemNameView.getText() + "'/[" + position + "] " + mItem.type;
        }


    }
}
