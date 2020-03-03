package com.parking.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

    @SuppressLint("CheckResult")
    @Override
    public void OnCancelClick(ParkingSpace parkingSpace) {
        parkingSpace.setParking_car(null);
        parkingSpace.setParking_user_id(0);
        parkingSpace.setIs_empty(1);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!compositeDisposable.isDisposed())
            compositeDisposable.dispose();
    }
}
