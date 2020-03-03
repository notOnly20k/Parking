package com.parking.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parking.R;
import com.parking.model.ParkingLot;
import com.parking.model.ParkingSpace;
import com.parking.model.User;
import com.parking.util.PreferencesUtil;

import java.util.List;

public class ParkAdapter extends BaseAdapter<ParkingLot> {
    private Callback callback;
    private User user;
    public ParkAdapter(Context context, List<ParkingLot> datas, int layoutId) {
        super(context, datas, layoutId);
        user= (User) PreferencesUtil.getInstance().getObject("user");
    }

    public ParkAdapter(Context context, List<ParkingLot> datas, int[] layoutIds) {
        super(context, datas, layoutIds);
    }

    public void setCallback(Callback callback){
        this.callback=callback;
    }

    public void refresh(){

    }

    @Override
    protected void onBindData(BaseHolder baseHolder, final ParkingLot parkingLot, int postion) {
        TextView tvEmpty=baseHolder.getView(R.id.tv_empty);
        TextView tvTotal=baseHolder.getView(R.id.tv_total);
        TextView tvName=baseHolder.getView(R.id.tv_name);
        TextView tvLocate=baseHolder.getView(R.id.tv_locate);
        Button btnCancel=baseHolder.getView(R.id.btn_cancel);
        tvLocate.setText("位置:"+parkingLot.getLocation());
        tvName.setText(parkingLot.getName());
        baseHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.OnItemClick(parkingLot);
            }
        });
        int empty=0;
        for (int i = 0; i < parkingLot.getParkingSpaces().size(); i++) {
            final ParkingSpace p=parkingLot.getParkingSpaces().get(i);
            Log.e("-----------"+i,p.getParking_user_id()+"-"+user.getId());
            if (p.getParking_user_id()==user.getId()){
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.OnCancelClick(p);
                    }
                });
            }
            if (p.getIs_empty()==1){
                empty+=1;
            }
        }
        tvTotal.setText("全部车位:"+parkingLot.getParkingSpaces().size());
        tvEmpty.setText("空闲车位:"+empty);

    }

    public interface Callback{
        void OnItemClick(ParkingLot parkingLot);
        void OnCancelClick(ParkingSpace parkingSpace);
    }
}
