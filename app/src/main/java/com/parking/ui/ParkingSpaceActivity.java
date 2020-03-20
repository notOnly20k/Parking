package com.parking.ui;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parking.R;
import com.parking.adapter.ParkingSpaceAdapter;
import com.parking.model.Message;
import com.parking.model.ParkingSpace;
import com.parking.model.User;
import com.parking.util.DBService;
import com.parking.util.PreferencesUtil;
import com.parking.util.ScreenUtils;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class ParkingSpaceActivity extends AppCompatActivity {

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    User user;
    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_space);
        user= (User) PreferencesUtil.getInstance().getObject("user");

    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }
    private ParkingSpaceAdapter parkingSpaceAdapter;
    private List<ParkingSpace>parkingSpaceList;
    private Calendar c;

    private void initData() {
        parkingSpaceList = (List<ParkingSpace>) getIntent().getExtras().getSerializable("parkingSpace");
        RecyclerView recyclerView=findViewById(R.id.rec);
        parkingSpaceAdapter=new ParkingSpaceAdapter(this,parkingSpaceList,R.layout.item_parking_space);
        parkingSpaceAdapter.setCallback(new ParkingSpaceAdapter.Callback() {
            @SuppressLint("CheckResult")
            @Override
            public void onClickOrder(final int position) {
                assert parkingSpaceList != null;
                parkingSpaceList.get(position).setIs_empty(0);
                parkingSpaceList.get(position).setParking_car(user.getCar());
                parkingSpaceList.get(position).setParking_user_id(user.getId());
                DBService.getDbService().getParkingSpaceByUserId(user.getId())
                        .subscribeOn(Schedulers.io())
                        .doOnSubscribe(new Consumer<Disposable>() {
                            @Override
                            public void accept(Disposable disposable) {
                                compositeDisposable.add(disposable);
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(Integer integer) throws Exception {
                                if (integer>0){
                                    Toast.makeText(ParkingSpaceActivity.this,"你已预约了车位",Toast.LENGTH_SHORT).show();
                                } else {
                                    c = Calendar.getInstance();
                                    c.setTimeInMillis(System.currentTimeMillis());
                                    final int currentHour = c.get(Calendar.HOUR_OF_DAY);
                                    final int currentMinute = c.get(Calendar.MINUTE);

                                    new TimePickerDialog(ParkingSpaceActivity.this,new TimePickerDialog.OnTimeSetListener(){

                                        @Override
                                        public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                                            if (hour<currentHour||(hour==currentHour&&minute<currentMinute)){
                                                Toast.makeText(ParkingSpaceActivity.this,"选择时间不能早于当前时间",Toast.LENGTH_SHORT).show();
                                            }else {
                                                parkingSpaceList.get(position).setParking_user_id(user.getId());
                                                parkingSpaceList.get(position).setIs_empty(0);
                                                String time="";
                                                java.util.Date date =new java.util.Date();
                                                date.setHours(hour);
                                                date.setMinutes(minute);
                                                Log.e("---",""+date.getTime());
                                                parkingSpaceList.get(position).setStartTime(new Timestamp(date.getTime()));
                                                orderSpace(position);
                                            }
                                        }
                                    },currentHour,currentMinute,true).show();
                                }
                            }
                        });
            }

            @Override
            public void onClickMessage(int position) {
                showMessageDialog( parkingSpaceList.get(position).getParking_user_id());
            }
        });
        recyclerView.setAdapter(parkingSpaceAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(ParkingSpaceActivity.this,LinearLayoutManager.VERTICAL,false));
    }

    @SuppressLint("CheckResult")
    public void orderSpace(int position){
        DBService.getDbService().updateParkingSpace(parkingSpaceList.get(position))
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) {
                        compositeDisposable.add(disposable);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (integer==1){
                            Toast.makeText(ParkingSpaceActivity.this,"预约成功",Toast.LENGTH_SHORT).show();
                            parkingSpaceAdapter.refresh(parkingSpaceList);
                        }
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void replayMessage(Message message, final AlertDialog dialog){
        DBService.getDbService().insertMessage(message)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) {
                        compositeDisposable.add(disposable);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        dialog.dismiss();
                        if (integer==1){
                            Toast.makeText(ParkingSpaceActivity.this,"回复成功",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showMessageDialog(final int receiver){
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_message,null,false);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        final EditText etContent=view.findViewById(R.id.et_content);
        TextView tvCancel=view.findViewById(R.id.tv_cancel);
        TextView tvReplay=view.findViewById(R.id.tv_replay);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        tvReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message message1=new Message();
                message1.setSender(user);
                message1.setContent(etContent.getText().toString());
                message1.setDate(new Date(System.currentTimeMillis()));
                User user1=new User();
                user1.setId(receiver);
                message1.setReceiver(user1);
                replayMessage(message1,dialog);
            }
        });

        dialog.show();
        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的3/4  注意一定要在show方法调用后再写设置窗口大小的代码，否则不起效果会
        dialog.getWindow().setLayout((ScreenUtils.getScreenWidth(this)/4*3), LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!compositeDisposable.isDisposed())
            compositeDisposable.dispose();
    }
}
