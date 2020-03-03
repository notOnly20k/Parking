package com.parking.ui;

import android.annotation.SuppressLint;
import android.icu.util.LocaleData;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parking.R;
import com.parking.adapter.MessageAdapter;
import com.parking.model.Message;
import com.parking.model.User;
import com.parking.util.DBService;
import com.parking.util.PreferencesUtil;
import com.parking.util.ScreenUtils;

import java.sql.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MessageActivity extends AppCompatActivity implements MessageAdapter.Callback {
    RecyclerView recyclerView;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        recyclerView=findViewById(R.id.rec);
        user= (User) PreferencesUtil.getInstance().getObject("user");
    }

    @Override
    protected void onStart() {
        super.onStart();
        initData();
    }

    @SuppressLint("CheckResult")
    private void initData() {
        DBService.getDbService().getMessage(user.getId())
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) {
                        compositeDisposable.add(disposable);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Message>>() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void accept(List<Message> messageList) throws Exception {
                        MessageAdapter messageAdapter=new MessageAdapter(MessageActivity.this,messageList,R.layout.item_message);
                        messageAdapter.setCallback(MessageActivity.this);
                        recyclerView.setAdapter(messageAdapter);
                        recyclerView.setLayoutManager(new LinearLayoutManager(MessageActivity.this,LinearLayoutManager.VERTICAL,false));
                    }
                });
    }

    @SuppressLint("CheckResult")
    @Override
    public void OnReplayClick(Message message) {
       showDialog(message);
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
                            Toast.makeText(MessageActivity.this,"回复成功",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showDialog(final Message message){
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
                message1.setReceiver(message.getSender());
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
