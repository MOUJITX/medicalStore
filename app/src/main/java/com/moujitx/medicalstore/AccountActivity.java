package com.moujitx.medicalstore;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener{

    String user,psw,newPSW;
    Button btn_psw,btn_delAccount,btn_return,btn_logout;
    EditText et_newPSW,et_oldPSW;
    SQLiteDatabase db;
    SQLites sqLites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        sqLites = new SQLites(this);
        initView();

        //获取账号信息
        Intent intent0 = getIntent();
        user = intent0.getStringExtra("user");

        db = sqLites.getReadableDatabase();
        Cursor cursor = db.query("user", null, "username=?", new String[]{user}, null,
                null, null);
        cursor.moveToFirst();
        psw = cursor.getString(2);
        cursor.close();
        db.close();
    }

    private void initView() {
        btn_psw = findViewById(R.id.btn_psw);
        btn_psw.setOnClickListener(this);
        btn_delAccount = findViewById(R.id.btn_delAccount);
        btn_delAccount.setOnClickListener(this);
        btn_return = findViewById(R.id.btn_return);
        btn_return.setOnClickListener(this);
        btn_logout = findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(this);
        et_newPSW = findViewById(R.id.et_newPSW);
        et_oldPSW = findViewById(R.id.et_oldPSW);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_logout:
                backToLogin();
                break;
            case R.id.btn_psw:
                String oldPSW = et_oldPSW.getText().toString();
                newPSW = et_newPSW.getText().toString();
                if (TextUtils.isEmpty(oldPSW)){
                    AlertDialog oldPSWEmpty = new AlertDialog.Builder(this)
                            .setTitle("密码修改失败！")//标题
                            .setMessage("旧密码不能为空")//内容
                            .setIcon(R.drawable.hospital)//图标
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create();
                    oldPSWEmpty.show();
                    return;
                } else if (TextUtils.isEmpty(newPSW)) {
                    AlertDialog newPSWEmpty = new AlertDialog.Builder(this)
                            .setTitle("密码修改失败！")//标题
                            .setMessage("新密码不能为空")//内容
                            .setIcon(R.drawable.hospital)//图标
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create();
                    newPSWEmpty.show();
                    return;
                } else if (oldPSW.equals(psw)){
                    db = sqLites.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put("password", newPSW);
                    db.update("user", values, "username=?", new String[]{user});
                    db.close();
                    Toast.makeText(this, "密码修改成功，请重新登录！", Toast.LENGTH_SHORT).show();
                    backToLogin();
                } else {
                    AlertDialog oldPSWWrong = new AlertDialog.Builder(this)
                            .setTitle("密码修改失败！")//标题
                            .setMessage("旧密码错误！")//内容
                            .setIcon(R.drawable.hospital)//图标
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create();
                    oldPSWWrong.show();
                    return;
                }
                break;
            case R.id.btn_delAccount:

                AlertDialog accountDel = new AlertDialog.Builder(this)
                        .setTitle("注销账户")//标题
                        .setMessage("请确认是否注销当前账户？")//内容
                        .setIcon(R.drawable.hospital)//图标
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db = sqLites.getReadableDatabase();
                                db.delete("user", "username = ?", new String[]{user});
                                db.close();
                                backToLogin();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create();
                accountDel.show();
                break;
            case R.id.btn_return:
                Intent intent = new Intent(this,ListActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
                break;
        }
    }

    private void backToLogin() {
        cancelAutoLogin();
        Intent intent0 = new Intent(this,MainActivity.class);
        startActivity(intent0);
    }

    private void cancelAutoLogin() {
        SharedPreferences sp = getSharedPreferences("autoLogin", Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("user",null);
        edit.commit();
    }
}