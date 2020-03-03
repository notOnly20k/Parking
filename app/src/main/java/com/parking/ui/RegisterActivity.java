package com.parking.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.parking.R;
import com.parking.model.User;
import com.parking.util.DBService;

import java.util.Optional;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RegisterActivity extends AppCompatActivity {
    EditText etName,etCar,etAccount,etPassword;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etName=findViewById(R.id.et_name);
        etCar=findViewById(R.id.et_car);
        etAccount=findViewById(R.id.et_account);
        etPassword=findViewById(R.id.et_password);
    }

    @SuppressLint("CheckResult")
    public void register(View view) {
        User user=new User();
        user.setAccount(etAccount.getText().toString());
        user.setPassword(etPassword.getText().toString());
        user.setName(etName.getText().toString());
        user.setCar(etCar.getText().toString());
        DBService.getDbService().insertUserData(user)
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
                            Intent intent2 = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent2);
                        }else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
                            builder.setTitle("确认");
                            builder.setMessage("注册失败");
                            builder.setPositiveButton("是", null);
                            builder.show();
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
