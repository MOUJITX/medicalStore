package com.moujitx.medicalstore;
import static android.widget.Toast.LENGTH_SHORT;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class CityActivity extends AppCompatActivity implements View.OnClickListener{

    String user,cityCode;
    private Spinner sp_city1,sp_city2,sp_city3;
    private Button btn_cityChange,btn_return,btn_location;
    private TextView tv_locationName;
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
        setContentView(R.layout.activity_city);
        sqLites = new SQLites(this);


        Intent intent0 = getIntent();
        user = intent0.getStringExtra("user");
        cityCode = intent0.getStringExtra("cityCode");

        initView();
        readPlace();
        setPlace();
        setNowPlace();
        getAddress();
    }

    private void setNowPlace() {


        int i=-1;

        String cityPro = cityCode.substring(0,2) + "0000";
        String cityArea = cityCode.substring(0,4) + "00";

        for (Object o : list) {
            i++;
            Map pMap = (Map) o;
            if (pMap.get("provinceCode").equals(cityPro)){
                sp_city1.setSelection(i);
                i=-1;
                List pList = (List) pMap.get("mallCityList");
                for (Object o1 : pList) {
                    i++;
                    Map cMap = (Map) o1;
                    if (cMap.get("cityCode").equals(cityArea)){
                        sp_city2.setSelection(i);
                        i=-1;
                        List cList = (List) cMap.get("mallAreaList");
                        for (Object o2 : cList) {
                            i++;
                            Map aMap = (Map) o2;
                            if (aMap.get("areaCode").equals(cityCode)){
                          //          Toast.makeText(this, String.valueOf(i), LENGTH_SHORT).show();
                                sp_city3.setSelection(i);
                                //    Toast.makeText(this, String.valueOf(sp_city3.getSelectedItemPosition()), LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }
                }
            }
        }
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
                sp_city2.setAdapter(new ArrayAdapter<String>(CityActivity.this,R.layout.item_select,box));

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
                sp_city3.setAdapter(new ArrayAdapter<String>(CityActivity.this,R.layout.item_select,box));
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

    private void initView() {
        tv_locationName = findViewById(R.id.tv_locationName);
        sp_city1 = findViewById(R.id.sp_city1);
        sp_city2 = findViewById(R.id.sp_city2);
        sp_city3 = findViewById(R.id.sp_city3);
        btn_cityChange = findViewById(R.id.btn_cityChange);
        btn_cityChange.setOnClickListener(this);
        btn_return = findViewById(R.id.btn_return);
        btn_return.setOnClickListener(this);
        btn_location = findViewById(R.id.btn_location);
        btn_location.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {

        SQLiteDatabase db;
        SQLiteDatabase dbInsert;

        switch (v.getId()) {
            case R.id.btn_location:
                getAddress();
                setNowPlace();
                break;
            case R.id.btn_cityChange:
                String city = "";
                String cityCode = "";
                city = sp_city3.getSelectedItem().toString();
                cityCode = getCityCode();
                if (cityCode.equals("000101")) {
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

                db = sqLites.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("city", city);
                values.put("cityCode", cityCode);
                db.update("user", values, "username=?", new String[]{user});
                db.close();
                Toast.makeText(this, "所在城市修改成功！", LENGTH_SHORT).show();
                break;
            case R.id.btn_return:
                Intent intent = new Intent(this,ListActivity.class);
                intent.putExtra("user",user);
                startActivity(intent);
                break;
        }
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
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location != null) {
            //获取经度和纬度
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            Log.d("GET LOCATION", "latitude: " + latitude);
            Log.d("GET LOCATION", "longitude: " + longitude);

            getLocationCode(latitude,longitude);
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

        CityActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                tv_locationName.setText(district);
            }
        });
        return;
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
}
