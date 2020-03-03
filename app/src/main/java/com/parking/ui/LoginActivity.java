package com.parking.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.parking.R;
import com.parking.model.User;
import com.parking.util.DBService;
import com.parking.util.PreferencesUtil;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.Optional;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity {
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    EditText mEtAccount;
    EditText mEtPassword;
    RxPermissions rxPermissions;
    boolean premission=false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rxPermissions= new RxPermissions(this);
        getPremission();
        if (PreferencesUtil.getInstance().getParam("user",null)!=null){
            Intent intent2 = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent2);
        }
        setContentView(R.layout.activity_login);
        mEtAccount=findViewById(R.id.et_account);
        mEtPassword=findViewById(R.id.et_password);
    }

    @SuppressLint("CheckResult")
    private void getPremission() {
        rxPermissions.requestEach(Manifest.permission.INTERNET,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE)
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        compositeDisposable.add(disposable);
                    }
                })
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        if (permission.granted) {
                            premission=true;
                            return;
                        }
                        if (permission.shouldShowRequestPermissionRationale) {
                            premission=false;
                            Toast.makeText(LoginActivity.this,"请同意权限",Toast.LENGTH_SHORT).show();
                            getPremission();
                            return;
                        }
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void login(View view) {
        String account = mEtAccount.getText().toString();
        String password = mEtPassword.getText().toString();
        DBService.getDbService().login(new User(0, "", "", password, account))
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) {
                        compositeDisposable.add(disposable);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Optional<User>>() {
                    @Override
                    public void accept(Optional<User> user) throws Exception {
                        if (user.isPresent()) {
                            PreferencesUtil.getInstance().saveParam("user",user.get());
                            Intent intent2 = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent2);
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                            builder.setTitle("确认");
                            builder.setMessage("登录失败");
                            builder.setPositiveButton("是", null);
                            builder.show();
                        }
                    }
                });
    }

    public void register(View view) {
        Intent intent2 = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!compositeDisposable.isDisposed())
            compositeDisposable.dispose();
    }
}
