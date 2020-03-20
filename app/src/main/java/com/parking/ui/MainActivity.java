package com.parking.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.parking.R;
import com.parking.adapter.ParkAdapter;
import com.parking.model.ParkingLot;
import com.parking.model.ParkingSpace;
import com.parking.model.User;
import com.parking.util.DBService;
import com.parking.util.PreferencesUtil;
import com.parking.util.ScreenUtils;

import java.io.Serializable;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements ParkAdapter.Callback {
    RecyclerView recyclerView;
    TextView tvLocal;
    FloatingActionButton floatingActionButton;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    User user;

    public LocationClient mLocationClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLocal=findViewById(R.id.tv_local);
        recyclerView=findViewById(R.id.rec);
        floatingActionButton=findViewById(R.id.fbtn_message);
        user= (User) PreferencesUtil.getInstance().getObject("user");
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, MessageActivity.class));
            }
        });
        mLocationClient = new LocationClient(getApplicationContext());

        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                tvLocal.setText(bdLocation.getProvince()+bdLocation.getCity()+bdLocation.getStreet());
            }
        });
        LocationClientOption option = new LocationClientOption();

        option.setIsNeedAddress(true);

        mLocationClient.setLocOption(option);

    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        //当点击不同的menu item 是执行不同的操作
        switch (id) {
            case R.id.action_mine:
                goMine();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    public void goMine(){
        Intent intent2 = new Intent(MainActivity.this, MineActivity.class);
        startActivity(intent2);
    }

    @SuppressLint("CheckResult")
    private void initData() {
        DBService.getDbService().getParkingLots()
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) {
                        compositeDisposable.add(disposable);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<ParkingLot>>() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void accept(List<ParkingLot> parkingLots) throws Exception {
                        ParkAdapter parkAdapter=new ParkAdapter(MainActivity.this,parkingLots,R.layout.item_park);
                        parkAdapter.setCallback(MainActivity.this);
                        recyclerView.setAdapter(parkAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this,LinearLayoutManager.VERTICAL,false));
                    }
                });
    }

    @Override
    public void OnItemClick(ParkingLot parkingLot) {
        Intent intent=new Intent(MainActivity.this,ParkingSpaceActivity.class);
        Bundle bundle=new Bundle();
        bundle.putSerializable("parkingSpace",(Serializable)parkingLot.getParkingSpaces());
        intent.putExtras(bundle);
        startActivity(intent);
    }


    @Override
    public void OnCancelClick(ParkingSpace parkingSpace) {
      showCancelDialog(parkingSpace);
    }

    @SuppressLint("CheckResult")
    private void cancelOrder(ParkingSpace parkingSpace){
        parkingSpace.setParking_car(null);
        parkingSpace.setParking_user_id(0);
        parkingSpace.setIs_empty(1);
        parkingSpace.setStartTime(null);
        DBService.getDbService().updateParkingSpace(parkingSpace)
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
                            initData();
                            Toast.makeText(MainActivity.this,"退出车位成功",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void showCancelDialog(final ParkingSpace parkingSpace){
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_cancel,null,false);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        TextView tvCoast=view.findViewById(R.id.tv_coast);
        long time=getTime(parkingSpace.getStartTime().getTime());
        if (time<-14){
            tvCoast.setText("本次停车扣费：0元");
        }
        tvCoast.setText("本次停车扣费："+time*0.5+"元");
        TextView tvCancel=view.findViewById(R.id.tv_cancel);
        TextView tvSure=view.findViewById(R.id.tv_sure);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                cancelOrder(parkingSpace);
            }
        });
        dialog.show();
        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的3/4  注意一定要在show方法调用后再写设置窗口大小的代码，否则不起效果会
        dialog.getWindow().setLayout((ScreenUtils.getScreenWidth(this)/4*3), LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private long getTime(long parkingTime){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long currentTime =System.currentTimeMillis();
         long diff=(currentTime-parkingTime)/1000/60;
        return diff;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!compositeDisposable.isDisposed())
            compositeDisposable.dispose();
    }
}
