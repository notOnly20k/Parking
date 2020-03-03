package com.parking.adapter;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.parking.R;
import com.parking.model.Message;
import com.parking.model.ParkingLot;
import com.parking.model.ParkingSpace;
import com.parking.model.User;
import com.parking.util.PreferencesUtil;

import java.util.List;

public class MessageAdapter extends BaseAdapter<Message> {
    private Callback callback;

    public MessageAdapter(Context context, List<Message> datas, int layoutId) {
        super(context, datas, layoutId);
    }

    public MessageAdapter(Context context, List<Message> datas, int[] layoutIds) {
        super(context, datas, layoutIds);
    }

    public void setCallback(Callback callback){
        this.callback=callback;
    }

    @Override
    protected void onBindData(BaseHolder baseHolder, final Message message, int postion) {
        TextView tvSenderName=baseHolder.getView(R.id.tv_sender_name);
        TextView tvCar=baseHolder.getView(R.id.tv_car);
        TextView tvContent=baseHolder.getView(R.id.tv_content);
        TextView tvDate=baseHolder.getView(R.id.tv_date);
        tvSenderName.setText("留言人："+message.getSender().getName());
        tvCar.setText("车辆："+message.getSender().getCar());
        tvContent.setText(message.getContent());
        tvDate.setText("日期："+message.getDate().toString());
        Button btnReplay=baseHolder.getView(R.id.btn_replay);
        btnReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callback.OnReplayClick(message);
            }
        });
    }


    public interface Callback{
        void OnReplayClick(Message message);
    }
}
