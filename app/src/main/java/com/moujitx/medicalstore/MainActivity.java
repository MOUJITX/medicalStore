package com.moujitx.medicalstore;
import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText et_username, et_password;
    private TextView tv_mainTitle,tv_loginType,tv_city,tv_locationName;
    private Spinner sp_city1,sp_city2,sp_city3;
    private Button btn_login,btn_location;
    private CheckBox cb_autoLogin;
    private LinearLayout LL_location;
    Integer loginType = 0;  //登录类型，0注册1登录
    String cityCode = "000000";  //初始化城市代码
    SQLites sqLites;


    //地区三级选择全局定义
    String[]p = new String[36];
    String[][] c = new String[36][200];
    String[][][] a = new String[36][200][300];
    List list;
    int index2 = 0;
    //地区三级选择全局定义 - 结束


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sqLites = new SQLites(this);
        initView();
        ifFirst();
        ifAutoLogin();
        readPlace();
        setPlace();
        setNowPlace();
        getAddress();
    }

    public void setPlace(){
        // 设置适配器
        sp_city1.setAdapter(new ArrayAdapter<String>(this,R.layout.item_select,p));
        sp_city1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // 下拉框选中事件
                index2 = i;
                List<String> box = new ArrayList<String>(Arrays.asList(c[i]));
                box.removeIf(Objects::isNull);
                sp_city2.setAdapter(new ArrayAdapter<String>(MainActivity.this,R.layout.item_select,box));

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        sp_city2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                List<String> box = new ArrayList<String>(Arrays.asList(a[index2][i]));
                box.removeIf(Objects::isNull);
                sp_city3.setAdapter(new ArrayAdapter<String>(MainActivity.this,R.layout.item_select,box));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void readPlace(){
        // 设置缓冲区
        byte []array = new byte[1024*100];
        int count =0;
        String word;
        StringBuffer sb = new StringBuffer();
        try {
            // 打开文件以流的方式打开
            InputStream stream = this.getApplicationContext().getAssets().open("place.json");
            count = stream.read(array);
            while(count>0){
                word = new String(array, 0, count);
                sb.append(word);
                count = stream.read(array);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        // 将json文件解析成List
        list = new Gson().fromJson(sb.toString(), List.class);
        int pIndex = 0; // 省索引
        int cIndex = 0; // 城市索引
        int aIndex = 0; // 区索引
        // 遍历
        for (Object o : list) {
            Map pMap = (Map) o;
            // 取出省的名字
            String pName = (String) pMap.get("provinceName");
            p[pIndex] = pName;
            // 取出城市list
            List pList = (List) pMap.get("mallCityList");
            for (Object o1 : pList) {
                // 二级遍历
                Map cMap = (Map) o1;
                String cName = (String) cMap.get("cityName");
                List cList = (List) cMap.get("mallAreaList");
                c[pIndex][cIndex] = cName;
                for (Object o2 : cList) {
                    // 三级遍历
                    Map aMap = (Map) o2;
                    String aName = (String) aMap.get("areaName");
                    a[pIndex][cIndex][aIndex] = aName;
                    aIndex++;
                }
                cIndex++;
                // 将aIndex变成0进行下一次循环
                aIndex=0;
            }
            pIndex++;
            // 将cIndex变成0进行下一次循环
            cIndex=0;
        }
    }

    private void ifAutoLogin() {
        String loginUser = getAutoLogin();
        if (TextUtils.isEmpty(loginUser)) return;
        else {
            Intent intent = new Intent(this,ListActivity.class);
            intent.putExtra("user",loginUser);
            startActivity(intent);
        }
    }

    private void ifFirst() {
        SQLiteDatabase db;
        db = sqLites.getReadableDatabase();
        Cursor cursor = db.query("user", null, null,null, null,
                null, null);
        if (cursor.getCount() == 0)  loginType = 0;
        else loginType = 1;
        setPage(loginType);
    }

    private void setPage(Integer type){
        if (type == 0) { //数据空
            loginType = 0;
            tv_mainTitle.setText("欢迎注册网上药店管理系统");
            btn_login.setText("注册");
            sp_city1.setVisibility(View.VISIBLE);
            sp_city2.setVisibility(View.VISIBLE);
            sp_city3.setVisibility(View.VISIBLE);
            tv_city.setVisibility(View.VISIBLE);
            tv_loginType.setText("已有账号？去登录");
            LL_location.setVisibility(View.VISIBLE);
        }
        else {
            loginType = 1;
            tv_mainTitle.setText("欢迎登录网上药店管理系统");
            btn_login.setText("登录");
            sp_city1.setVisibility(View.GONE);
            sp_city2.setVisibility(View.GONE);
            sp_city3.setVisibility(View.GONE);
            tv_loginType.setText("还没有账号？去注册");
            tv_city.setVisibility(View.GONE);
            LL_location.setVisibility(View.GONE);
        }
    }

    private void initView() {
        LL_location = findViewById(R.id.LL_location);
        tv_city = findViewById(R.id.tv_city);
        tv_loginType = findViewById(R.id.tv_loginType);
        tv_mainTitle = findViewById(R.id.tv_mainTitle);
        tv_locationName = findViewById(R.id.tv_locationName);
        et_username =  findViewById(R.id.et_username);
        et_password =  findViewById(R.id.et_password);
        sp_city1 = findViewById(R.id.sp_city1);
        sp_city2 = findViewById(R.id.sp_city2);
        sp_city3 = findViewById(R.id.sp_city3);
        btn_login = findViewById(R.id.btn_login);
        btn_location = findViewById(R.id.btn_location);
        cb_autoLogin = findViewById(R.id.cb_autoLogin);
        btn_login.setOnClickListener(this);
        btn_location.setOnClickListener(this);
        tv_loginType.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {

        SQLiteDatabase db;
        SQLiteDatabase dbInsert;

        switch (v.getId()) {
            case R.id.btn_location:
                setNowPlace();
                break;
            case R.id.tv_loginType:
                setPage(1-loginType);
                break;
            case R.id.btn_login:
                String username = et_username.getText().toString().trim();
                String password = et_password.getText().toString();
                String city = "";
                String cityCode = "";
                if (loginType == 0) {
                    city = sp_city3.getSelectedItem().toString();
                    cityCode = getCityCode();
                }
                //检验输入的账号和密码是否为空
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                    AlertDialog usernameEmpty = new AlertDialog.Builder(this)
                            .setTitle("注册失败！")//标题
                            .setMessage("用户名或密码不能为空")//内容
                            .setIcon(R.drawable.hospital)//图标
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create();
                    usernameEmpty.show();
                    return;
                } else if (cityCode.equals("000101") && loginType == 0) {
                    AlertDialog cityEmpty = new AlertDialog.Builder(this)
                            .setTitle("未选择城市")//标题
                            .setMessage("请选择您所在的城市")//内容
                            .setIcon(R.drawable.hospital)//图标
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .create();
                    cityEmpty.show();
                    return;
                }
                //查询用户信息
                db = sqLites.getReadableDatabase();
                Cursor cursor = db.query("user", null, "username=?", new String[]{username}, null,
                        null, null);
                if (cursor.getCount() == 0) {  //查询为空则新增用户
                    if (loginType == 0){
                        dbInsert = sqLites.getWritableDatabase();
                        ContentValues values = new ContentValues();
                        values.put("username", username);
                        values.put("password", password);
                        values.put("city", city);
                        values.put("cityCode", cityCode);
                        dbInsert.insert("user", null, values);
                        dbInsert.close();
                        cursor.close();
                        db.close();
                        setAutoLogin(username);
                        Toast.makeText(this, "注册成功，欢迎新用户！", Toast.LENGTH_SHORT).show();
                        //跳转
                        Intent intent = new Intent(this,ListActivity.class);
                        intent.putExtra("user",username);
                        startActivity(intent);
                        break;
                    } else {
                        Toast.makeText(this, "用户名不存在，请先注册", Toast.LENGTH_SHORT).show();
                    }

                } else {  //查询到用户数据则校验密码
                    if (loginType == 1){
                        cursor.moveToFirst();
                        if (cursor.getString(2).equals(password)){
                            cursor.close();
                            db.close();
                            setAutoLogin(username);
                            Toast.makeText(this, "登录成功，欢迎回来！", Toast.LENGTH_SHORT).show();
                            //密码校验成功，实现页面跳转到information页
                            Intent intent = new Intent(this,ListActivity.class);
                            intent.putExtra("user",username);
                            startActivity(intent);
                        } else {
                            cursor.close();
                            db.close();
                            Toast.makeText(this, "密码错误！", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "用户名已存在，请直接登录或更改注册用户名", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private String getCityCode() {
        Integer cityNameOne = sp_city1.getSelectedItemPosition();
        Integer cityNameTwo = sp_city2.getSelectedItemPosition();
        Integer cityNameThree = sp_city3.getSelectedItemPosition();
        Map map1 = (Map) list.get(cityNameOne);
        List list1 = (List) map1.get("mallCityList");
        Map map2 = (Map) list1.get(cityNameTwo);
        List list2 = (List) map2.get("mallAreaList");
        Map map3 = (Map) list2.get(cityNameThree);
        String cityCode = (String) map3.get("areaCode");
        return cityCode;
    }

    private void setAutoLogin(String username) {
        SharedPreferences sp = getSharedPreferences("autoLogin",Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("START", String.valueOf(true));
        if (cb_autoLogin.isChecked()){
            edit.putString("user",username);
        }
        else {
            edit.putString("user",null);
        }
        edit.commit();
    }

    private String getAutoLogin(){
        SharedPreferences sp = getSharedPreferences("autoLogin",Context.MODE_PRIVATE);
        String user = sp.getString("user",null);
        return user;
    }


    private void getAddress() {
        //首先获取LocationManager实例
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        //检查是否有定位权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //如果没有定位权限，则请求用户授权
            int MY_PERMISSIONS_REQUEST_LOCATION = 0;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }
        //获取最近的位置信息
        try {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                //获取经度和纬度
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                Log.d("GET LOCATION", "latitude: " + latitude);
                Log.d("GET LOCATION", "longitude: " + longitude);

                //  Toast.makeText(this, "latitude: " + latitude, LENGTH_SHORT).show();
                //  Toast.makeText(this, "longitude: " + longitude, LENGTH_SHORT).show();
                getLocationCode(latitude,longitude);
            }
        } catch (Exception e){
            tv_locationName.setText("获取失败，请手动设置");
        }
    }

    private void getLocationCode(double latitude, double longitude){


        String path = "https://restapi.amap.com/v3/geocode/regeo?key=ab2622dbde98a8954a643600479c216e&location=" + longitude + "," + latitude;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(path);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.connect();
                    int responseCode = connection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        InputStream inputStream = connection.getInputStream();
                        String result = is2String(inputStream);//将流转换为字符串。
                        setLocationCode(result);

                        System.out.println("setLocationCode ok");

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void setLocationCode(String result) {
        System.out.println(result);
        Map locationList = new Gson().fromJson(result, Map.class);
        Map regeocode = (Map) locationList.get("regeocode");
        Map AddressComponent = (Map) regeocode.get("addressComponent");
        String adcode = (String) AddressComponent.get("adcode");
        String district = (String) AddressComponent.get("district");
        System.out.println(adcode);
        System.out.println(district);
        cityCode = adcode;

        MainActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                tv_locationName.setText(district);
            //    setNowPlace();
            }
        });
    }

    public String is2String(InputStream is) throws IOException {
        //连接后，创建一个输入流来读取response
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is,"utf-8"));
        String line = "";
        StringBuilder stringBuilder = new StringBuilder();
        // String response = "";
        //每次读取一行，若非空则添加至 stringBuilder
        while((line = bufferedReader.readLine()) != null){
            stringBuilder.append(line);
        }
        //读取所有的数据后，赋值给 response
        String response = stringBuilder.toString().trim();
        return response;
    }
    private void setNowPlace() {

        int i=0;
        int j=0;
        int k=0;

        String cityPro = cityCode.substring(0,2) + "0000";
        String cityArea = cityCode.substring(0,4) + "00";

        for (Object o : list) {
            Map pMap = (Map) o;
            if (pMap.get("provinceCode").equals(cityPro)){
                System.out.println("i true");
                List pList = (List) pMap.get("mallCityList");
                for (Object o1 : pList) {
                    Map cMap = (Map) o1;
                    if (cMap.get("cityCode").equals(cityArea)){
                        System.out.println("j true");
                        List cList = (List) cMap.get("mallAreaList");
                        for (Object o2 : cList) {
                            Map aMap = (Map) o2;
                            if (aMap.get("areaCode").equals(cityCode)){
                                break;
                            }
                            k++;
                            System.out.println("k="+k);
                        }
                        break;
                    }
                    j++;
                    System.out.println("j="+j);
                }
                break;
            }
            i++;
            System.out.println("i="+i);
        }

        sp_city1.setSelection(i);

        sp_city2.setSelection(j);

        sp_city3.setSelection(k);

        System.out.println(sp_city1.getSelectedItemPosition());
        System.out.println(sp_city2.getSelectedItemPosition());
        System.out.println(sp_city3.getSelectedItemPosition());

    }
}
