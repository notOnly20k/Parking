package com.parking.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.parking.R;
import com.parking.model.User;
import com.parking.util.DBService;
import com.parking.util.PreferencesUtil;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MineActivity extends AppCompatActivity {
    EditText etAccount,etPwd,etCar,etName;
    Button btnSave,btnLogout;
    User user;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine);
        etAccount=findViewById(R.id.et_account);
        etCar=findViewById(R.id.et_car);
        etPwd=findViewById(R.id.et_pwd);
        etName=findViewById(R.id.et_name);
        btnSave=findViewById(R.id.btn_save);
        btnLogout=findViewById(R.id.btn_logout);
        user= (User) PreferencesUtil.getInstance().getObject("user");
        etAccount.setText(user.getAccount());
        etPwd.setText(user.getPassword());
        etCar.setText(user.getCar());
        etName.setText(user.getName());
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferencesUtil.getInstance().remove("user");
                Intent intent2 = new Intent(MineActivity.this, LoginActivity.class);
                startActivity(intent2);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CheckResult")
            @Override
            public void onClick(View view) {
                if (etAccount.getText().toString().isEmpty()||etCar.getText().toString().isEmpty()||etPwd.getText().toString().isEmpty()){
                    Toast.makeText(MineActivity.this,"请完善信息",Toast.LENGTH_SHORT).show();
                }else {
                    user.setName(etName.getText().toString());
                    user.setCar(etCar.getText().toString());
                    user.setAccount(etAccount.getText().toString());
                    user.setPassword(etPwd.getText().toString());
                    DBService.getDbService().updateUserData(user)
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
                                        PreferencesUtil.getInstance().saveParam("user",user);
                                        Toast.makeText(MineActivity.this,"保存成功",Toast.LENGTH_SHORT).show();
                                    }else {
                                        Toast.makeText(MineActivity.this,"保存失败",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
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
