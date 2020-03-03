package com.parking.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parking.R;
import com.parking.model.ParkingSpace;
import com.parking.model.User;
import com.parking.util.PreferencesUtil;

import java.util.List;

public class ParkingSpaceAdapter extends BaseAdapter<ParkingSpace> {

    Callback callback;
    User user;
    Boolean canOrder=true;
    public ParkingSpaceAdapter(Context context, List<ParkingSpace> datas, int layoutId) {
        super(context, datas, layoutId);
        user= (User) PreferencesUtil.getInstance().getObject("user");
        for (int i = 0; i < datas.size(); i++) {
            if (datas.get(i).getParking_user_id()==user.getId()){
                canOrder=false;
            }
        }
    }

    public ParkingSpaceAdapter(Context context, List<ParkingSpace> datas, int[] layoutIds) {
        super(context, datas, layoutIds);
    }

    public void setCallback(Callback callback){
        this.callback=callback;
    }

    public void refresh(List<ParkingSpace> datas){
        this.setData(datas);
        this.notifyDataSetChanged();
        for (int i = 0; i < datas.size(); i++) {
            if (datas.get(i).getParking_user_id()==user.getId()){
                canOrder=false;
            }
        }
    }

    @Override
    protected void onBindData(BaseHolder baseHolder, final ParkingSpace parkingSpace, final int postion) {
        TextView tvNum= baseHolder.getView(R.id.tv_num);
        TextView tvStatus= baseHolder.getView(R.id.tv_status);
        Button btnOrder= baseHolder.getView(R.id.btn_order);
        tvNum.setText("编号:"+parkingSpace.getNum());
        if (parkingSpace.getIs_empty()==0){
            tvStatus.setText("状态:已停");
            btnOrder.setText("留言");
            btnOrder.setBackgroundColor(Color.GRAY);
            btnOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onClickMessage(postion);
                }
            });
        }else {
            tvStatus.setText("状态:空闲");
            if (canOrder) {
                btnOrder.setBackgroundColor(Color.GREEN);
                btnOrder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callback.onClickOrder(postion);
                    }
                });
            }
        }

    }

    public interface Callback{
        void onClickOrder(int position);
        void onClickMessage(int position);
    }
}
