package com.moujitx.medicalstore;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ListActivity extends AppCompatActivity implements
        View.OnClickListener {
    SQLites sqLites;
    private EditText et_search;
    private Button btn_search,btn_manage;
    private TextView tv_dataNull,tv_helloWord,tv_cityName,tv_weatherOther,tv_temperature,tv_weatherName;
    private ImageView iv_userInfo,iv_weatherImg;
    String username,cityCode;

    public class DrugInfo {
        public Integer _id;
        public String name;
        public String type;
        public String price;
        public String quantity;
    }
    RecyclerView mRecyclerView;
    MyAdapter mMyAdapter ;
    List<DrugInfo> drugInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        sqLites = new SQLites(this);
        init();
        setHelloWord();
        getWeather();
        setListData("");
    }

    private void getWeather(){
        String path = "https://restapi.amap.com/v3/weather/weatherInfo?key=43bfe379cfd9825d47aa145a7c20a8bf&city=" + cityCode;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(path);
                    //得到connection对象。
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    //设置请求方式
                    connection.setRequestMethod("GET");
                    //连接
                    connection.connect();
                    //得到响应码
                    int responseCode = connection.getResponseCode();
                    if(responseCode == HttpURLConnection.HTTP_OK){
                        //得到响应流
                        InputStream inputStream = connection.getInputStream();
                        //将响应流转换成字符串
                        String result = is2String(inputStream);//将流转换为字符串。
                        setWeather(result);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    private void setWeather(String result) {
        System.out.println(result);
        Map weatherList = new Gson().fromJson(result, Map.class);
        List weatherLives = (List) weatherList.get("lives");
        Map weather = (Map) weatherLives.get(0);
        String weatherName = weather.get("weather").toString();
        String temperature = weather.get("temperature").toString() + " ℃";
        String winddirection = weather.get("winddirection").toString();
        String windpower = weather.get("windpower").toString();

        String wind = winddirection + "风" + windpower + "级 PM2.5指数";


        String[] weatherType = {
                "晴","少云",
                "晴间多云","多云",
                "阴",
                "有风","平静","微风","和风","清风",
                "强风/劲风","疾风","大风","烈风","风暴","狂爆风","飓风","热带风暴",
                "霾","中度霾","重度霾","严重霾",
                "阵雨","雷阵雨","雷阵雨并伴有冰雹",
                "小雨","中雨","大雨","暴雨","大暴雨","特大暴雨","强阵雨","强雷阵雨","极端降雨",
                "毛毛雨/细雨","雨","小雨-中雨","中雨-大雨","大雨-暴雨","暴雨-大暴雨","大暴雨-特大暴雨",
                "雨雪天气","雨夹雪","阵雨夹雪","冻雨","雪","阵雪",
                "小雪", "中雪","大雪","暴雪","小雪-中雪","中雪-大雪","大雪-暴雪",
                "浮尘","扬沙","沙尘暴","强沙尘暴","龙卷风",
                "雾","浓雾","强浓雾","轻雾","大雾","特强浓雾",
                "热","冷","未知"
        };

        Integer[] weatherTypeImg = {
                20,1,
                1,1,
                26,
                9,9,9,9,9,
                29,29,29,29,27,27,27,27,
                7,7,7,7,
                2,15,15,
                24,24,24,14,14,14,14,15,14,
                24,24,24,24,14,14,14,
                6,6,6,6,6,6,
                6,6,6,19,6,6,19,
                25,25,29,27,27,
                25,25,25,25,25,25,
                19,20,18
        };

        for (int type = 0 ;type<weatherType.length;type++) {
            if (weatherName.equals(weatherType[type])) iv_weatherImg.getDrawable().setLevel(weatherTypeImg[type]);
        }

        ListActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                tv_weatherName.setText(weatherName);
                tv_temperature.setText(temperature);
                tv_weatherOther.setText(wind);
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
    private void setHelloWord() {
        Intent intent = getIntent();
        username = intent.getStringExtra("user");

        SimpleDateFormat formatter = new SimpleDateFormat ("HH");
        Date curDate = new Date(System.currentTimeMillis());
        Integer timeH = Integer.valueOf(formatter.format(curDate));
        String timeWord = "";
        if(timeH<4) timeWord = "晚上";
        else if (timeH<6) timeWord = "凌晨";
        else if (timeH<9) timeWord = "早上";
        else if (timeH<11) timeWord = "上午";
        else if (timeH<13) timeWord = "中午";
        else if (timeH<17) timeWord = "下午";
        else if (timeH<19) timeWord = "傍晚";
        else timeWord = "晚上";

        tv_helloWord.setText(username+"，"+timeWord+"好！");

        SQLiteDatabase db;
        db = sqLites.getReadableDatabase();
        String sql = "select * from user where username =='" + username + "'";
        Cursor cursor=db.rawQuery(sql,null);
        cursor.moveToFirst();

        tv_cityName.setText(cursor.getString(3));
        cityCode = cursor.getString(4);
    }

    private void setListData(String info) {

        drugInfos.clear();

        SQLiteDatabase db;
        db = sqLites.getReadableDatabase();

        String sql = "select * from shop where name LIKE '%" + info + "%' or type LIKE '%" + info + "%' or price LIKE '%" + info + "%' or quantity LIKE '%" + info + "%'";
        Cursor cursor=db.rawQuery(sql,null);

        if (cursor.getCount() == 0) {
            mRecyclerView.setVisibility(View.GONE);
            tv_dataNull.setVisibility(View.VISIBLE);
            return;
        } else {
            mRecyclerView.setVisibility(View.VISIBLE);
            tv_dataNull.setVisibility(View.GONE);

            cursor.moveToFirst();

            DrugInfo drugInfo = new DrugInfo();
            drugInfo._id = Integer.valueOf(cursor.getString(0));
            drugInfo.name = cursor.getString(1);
            drugInfo.type = cursor.getString(2);
            drugInfo.price = cursor.getString(3);
            drugInfo.quantity = cursor.getString(4);
            drugInfos.add(drugInfo);
        }
        while (cursor.moveToNext()) {
            DrugInfo drugInfo = new DrugInfo();
            drugInfo._id = Integer.valueOf(cursor.getString(0));
            drugInfo.name = cursor.getString(1);
            drugInfo.type = cursor.getString(2);
            drugInfo.price = cursor.getString(3);
            drugInfo.quantity = cursor.getString(4);
            drugInfos.add(drugInfo);
        }
        cursor.close();
        db.close();

        mMyAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mMyAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(ListActivity.this);
        mRecyclerView.setLayoutManager(layoutManager);
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHoder> {

        @NonNull
        @Override
        public MyViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = View.inflate(ListActivity.this, R.layout.item_list, null);
            MyViewHoder myViewHoder = new MyViewHoder(view);
            return myViewHoder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHoder holder, @SuppressLint("RecyclerView") int position) {
            DrugInfo drugInfo = drugInfos.get(position);
            holder.tv_name.setText(drugInfo.name);
            holder.tv_price.setText(drugInfo.price);
            holder.tv_quantity.setText(drugInfo.quantity);
            holder.tv_type.setText(drugInfo.type);

            holder.listItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gotoChange(drugInfo._id);
                }
            });
        }

        @Override
        public int getItemCount() {
            return drugInfos.size();
        }
    }

    private void gotoChange(Integer id) {
        Intent intentInfo = new Intent(this,ManageActivity.class);
        intentInfo.putExtra("type","change");
        intentInfo.putExtra("id",String.valueOf(id));
        intentInfo.putExtra("user",username);
        startActivity(intentInfo);
    }

    class MyViewHoder extends RecyclerView.ViewHolder {
        TextView tv_name,tv_type,tv_price,tv_quantity;
        ConstraintLayout listItem;

        public MyViewHoder(@NonNull View itemView) {
            super(itemView);
            tv_name = itemView.findViewById(R.id.tv_name);
            tv_type = itemView.findViewById(R.id.tv_type);
            tv_price = itemView.findViewById(R.id.tv_price);
            tv_quantity = itemView.findViewById(R.id.tv_quantity);
            listItem = itemView.findViewById(R.id.listItem);
        }
    }

    private void init() {
        iv_userInfo = findViewById(R.id.iv_userInfo);
        iv_weatherImg = findViewById(R.id.iv_weatherImg);
        et_search = findViewById(R.id.et_search);
        tv_dataNull = findViewById(R.id.tv_dataNull);
        tv_helloWord = findViewById(R.id.tv_helloWord);
        tv_cityName = findViewById(R.id.tv_cityName);
        tv_weatherOther = findViewById(R.id.tv_weatherOther);
        tv_temperature = findViewById(R.id.tv_temperature);
        tv_weatherName = findViewById(R.id.tv_weatherName);
        btn_manage = findViewById(R.id.btn_manage);
        btn_search = findViewById(R.id.btn_search);
        btn_manage.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        iv_userInfo.setOnClickListener(this);
        tv_cityName.setOnClickListener(this);

        mRecyclerView = findViewById(R.id.rv_list);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_userInfo:
                Intent intentUserInfo = new Intent(this,AccountActivity.class);
                intentUserInfo.putExtra("user",username);
                startActivity(intentUserInfo);
                break;
            case R.id.btn_manage:
                Intent intent = new Intent(this,ManageActivity.class);
                intent.putExtra("type","add");
                intent.putExtra("user",username);
                startActivity(intent);
                break;
            case R.id.btn_search:
                String info = et_search.getText().toString();
                setListData(info);
                break;
            case R.id.tv_cityName:
                Intent intentCity = new Intent(this,CityActivity.class);
                intentCity.putExtra("user",username);
                intentCity.putExtra("cityCode",cityCode);
                startActivity(intentCity);
                break;
        }
    }
}
