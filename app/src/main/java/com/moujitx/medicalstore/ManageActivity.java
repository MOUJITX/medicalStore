package com.moujitx.medicalstore;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ManageActivity extends AppCompatActivity implements
        View.OnClickListener {
    SQLites sqLites;
    private EditText et_name,et_price,et_quantity;
    private Spinner sp_type;
    private Button btn_add,btn_clean,btn_update,btn_delete,btn_return, btn_plus, btn_cut;
    private TextView tv_manageTitle;
    private ImageView iv_userInfo;
    Integer id = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);
        sqLites = new SQLites(this);
        init();

    }
    private void init() {
        iv_userInfo = findViewById(R.id.iv_userInfo);
        tv_manageTitle = findViewById(R.id.tv_manageTitle);
        et_name = findViewById(R.id.et_name);
        et_price = findViewById(R.id.et_price);
        et_quantity = findViewById(R.id.et_quantity);
        sp_type = findViewById(R.id.sp_type);
        btn_add = findViewById(R.id.btn_add);
        btn_clean = findViewById(R.id.btn_clean);
        btn_update = findViewById(R.id.btn_update);
        btn_delete = findViewById(R.id.btn_delete);
        btn_return = findViewById(R.id.btn_return);
        btn_plus = findViewById(R.id.btn_plus);
        btn_cut = findViewById(R.id.btn_cut);
        btn_add.setOnClickListener(this);
        btn_clean.setOnClickListener(this);
        btn_update.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
        btn_return.setOnClickListener(this);
        btn_plus.setOnClickListener(this);
        btn_cut.setOnClickListener(this);
        iv_userInfo.setOnClickListener(this);
        Intent intent = getIntent();
        String type = intent.getStringExtra("type");
        if (type.equals("add")){
            btn_update.setVisibility(View.GONE);
            btn_delete.setVisibility(View.GONE);
            tv_manageTitle.setText("新增药品");
        }
        else if (type.equals("change")){
            btn_add.setVisibility(View.GONE);
            btn_clean.setVisibility(View.GONE);
            id = Integer.valueOf(intent.getStringExtra("id"));
            tv_manageTitle.setText("管理药品");

            SQLiteDatabase db;
            db = sqLites.getReadableDatabase();
            String sql = "select * from shop where _id=" + id;
            Cursor cursor=db.rawQuery(sql,null);
            cursor.moveToFirst();
            et_name.setText(cursor.getString(1));
            String[] drugTypes = {"处方药","非处方药","中药","其他"};
            for (int i=0;i<drugTypes.length;i++){
                if (drugTypes[i].equals(cursor.getString(2))) {sp_type.setSelection(i+1);break;}
                else sp_type.setSelection(0);
            }
            et_price.setText(cursor.getString(3));
            et_quantity.setText(cursor.getString(4));
            cursor.close();
            db.close();
        }
    }
    @Override
    public void onClick(View v) {
        String name, type;
        Integer quantity;
        Float price;
        SQLiteDatabase db;
        ContentValues values;
        switch (v.getId()) {
            case R.id.iv_userInfo:
                Intent intent = getIntent();
                String username = intent.getStringExtra("user");
                Intent intentUserInfo = new Intent(this,AccountActivity.class);
                intentUserInfo.putExtra("user",username);
                startActivity(intentUserInfo);
                break;
            case R.id.btn_add: //添加
                name = et_name.getText().toString();
                String price0 = et_price.getText().toString();
                String quantity0 = et_quantity.getText().toString();
                type = sp_type.getSelectedItem().toString();
                if (TextUtils.isEmpty(name)) {InfoEmpty("药品名称");return;}
                else if (TextUtils.isEmpty(quantity0)) {InfoEmpty("库存余量");return;}
                else if (TextUtils.isEmpty(price0)) {InfoEmpty("价格");return;}
                else if (type.equals("选择药品分类")) {InfoEmpty("药品分类");return;}

                price = Float.valueOf(price0);
                quantity = Integer.valueOf(quantity0);

                db = sqLites.getWritableDatabase();
                values = new ContentValues();
                values.put("name", name);
                values.put("type", type);
                values.put("price", price);
                values.put("quantity", quantity);
                db.insert("shop", null, values);
                db.close();
                Toast.makeText(this, "信息添加成功！", Toast.LENGTH_SHORT).show();
                AlertDialog addConfirm = new AlertDialog.Builder(this)
                        .setTitle("添加成功")
                        .setMessage("信息添加成功，是否继续添加")
                        .setIcon(R.drawable.hospital)
                        .setPositiveButton("继续添加", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                et_name.setText("");
                                et_price.setText("0.00");
                                et_quantity.setText("0");
                                sp_type.setSelection(0,true);
                            }
                        })
                        .setNegativeButton("添加完毕", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                goback();
                            }
                        })
                        .create();
                addConfirm.show();
                break;
            case R.id.btn_clean: //重置
                et_name.setText("");
                et_quantity.setText("0");
                et_price.setText("0.00");
                sp_type.setSelection(0,true);
                break;
            case R.id.btn_update: //修改
                name = et_name.getText().toString();
                String price1 = et_price.getText().toString();
                String quantity1 = et_quantity.getText().toString();
                type = sp_type.getSelectedItem().toString();
                if (TextUtils.isEmpty(name)) {InfoEmpty("药品名称");return;}
                else if (TextUtils.isEmpty(quantity1)) {InfoEmpty("库存余量");return;}
                else if (TextUtils.isEmpty(price1)) {InfoEmpty("价格");return;}
                else if (type.equals("选择药品分类")) {InfoEmpty("药品分类");return;}

                price = Float.valueOf(price1);
                quantity = Integer.valueOf(quantity1);

                db = sqLites.getWritableDatabase();
                values = new ContentValues();
                values.put("name", name);
                values.put("type", type);
                values.put("price", price);
                values.put("quantity", quantity);
                db.update("shop", values, "_id=?",
                        new String[]{String.valueOf(id)});
                Toast.makeText(this, "信息修改成功", Toast.LENGTH_SHORT).show();
                db.close();
                break;
            case R.id.btn_delete:
                AlertDialog delConfirm = new AlertDialog.Builder(this)
                        .setTitle("确认删除")
                        .setMessage("确认要删除这条信息吗？")
                        .setIcon(R.drawable.hospital)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                delInfo();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .create();
                delConfirm.show();
                break;
            case R.id.btn_return:
                goback();
                break;
            case R.id.btn_plus:
                String quantity2 = et_quantity.getText().toString();
                if (TextUtils.isEmpty(quantity2)) {
                    quantity2 = "0";
                    quantity = Integer.valueOf(quantity2);
                }
                else {
                    quantity = Integer.valueOf(quantity2);
                    quantity ++;
                }
                et_quantity.setText(String.valueOf(quantity));
                break;
            case R.id.btn_cut:
                String quantity3 = et_quantity.getText().toString();
                if (TextUtils.isEmpty(quantity3)) {
                    quantity3 = "0";
                    quantity = Integer.valueOf(quantity3);
                } else {
                    quantity = Integer.valueOf(quantity3);
                    if (quantity == 0) quantity=0;
                    else quantity--;
                }
                et_quantity.setText(String.valueOf(quantity));
                break;
        }
    }

    private void InfoEmpty(String name) {
        AlertDialog infoEmpty = new AlertDialog.Builder(this)
                .setTitle("保存失败！")//标题
                .setMessage(name+"不能为空")//内容
                .setIcon(R.drawable.hospital)//图标
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
        infoEmpty.show();
        return;
    }

    private void goback() {

        Intent intentGetUser = getIntent();
        String username = intentGetUser.getStringExtra("user");
        Intent intent = new Intent(this,ListActivity.class);
        intent.putExtra("user",username);
        startActivity(intent);
    }

    private void delInfo() {
        SQLiteDatabase db;
        db = sqLites.getWritableDatabase();
        db.delete("shop", "_id = ?", new String[]{String.valueOf(id)});
        Toast.makeText(this, "信息删除成功", Toast.LENGTH_SHORT).show();
        db.close();

        goback();
    }
}