package com.coolweather.android;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.coolweather.android.demo.LogUtil;
import com.coolweather.android.util.HttpUtil;

import java.io.IOException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.weather.Weather;
import interfaces.heweather.com.interfacesmodule.bean.weather.forecast.ForecastBase;
import interfaces.heweather.com.interfacesmodule.bean.weather.lifestyle.LifestyleBase;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String MY_KY_ID = "HE1903062339061182";

    private static final String MY_KEY = "a4090fca42484f4a9f6b365a6289aedf";

    List<Weather> weatherList;

    private ScrollView weatherLayout;

    private TextView titleCity;

    private TextView titleUpdateTime;

    private TextView degreeTest;

    private TextView weatherInfoTest;

    private LinearLayout forecastLayout;

    private LinearLayout suggLayout;

    private TextView w_amount;

    private TextView w_rate;

    private TextView comfortText;

    private TextView drsgTest;

    private ProgressDialog progressDialog;

    private TextView fluText;

    private ImageView bingPicImg;

    public SwipeRefreshLayout swipeRefresh;

    private String mWeatherId;

    private Button navButton;

    public DrawerLayout drawerLayout;

    private String weatherId;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        HeConfig.init(MY_KY_ID, MY_KEY);
        HeConfig.switchToFreeServerNode();

        weatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        titleCity = (TextView) findViewById(R.id.title_city);
        titleUpdateTime = (TextView) findViewById(R.id.title_updata_time);
        degreeTest = (TextView) findViewById(R.id.degree_text);
        weatherInfoTest = (TextView) findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        suggLayout = (LinearLayout) findViewById(R.id.sugg);
        w_amount = (TextView) findViewById(R.id.w_amount);
        w_rate = (TextView) findViewById(R.id.w_rate);
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navButton = (Button) findViewById(R.id.nav_button);

        swipeRefresh.setColorSchemeColors(R.color.colorPrimary);
        SharedPreferences preferences =PreferenceManager.getDefaultSharedPreferences(this);
       weatherId =getIntent().getStringExtra("weather_id");
        String bingPic = preferences.getString("bing",null);
        if(bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else{
            loadBingPic();
        }
        String strCity = preferences.getString("weather_city",null);

        if (weatherList==null){
            requestWeather(weatherId==null?strCity:weatherId);
        }else{
            showProgressDialog();
        }


        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(mWeatherId==null){
                    mWeatherId = preferences.getString("weather_city",null);
                    requestWeather(mWeatherId);
                }else{
                    requestWeather(mWeatherId);
                }

            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /*
    * 加载每日一图
    * */
    private void loadBingPic() {
        String requestBingPic = "http:/guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor =PreferenceManager.getDefaultSharedPreferences(
                        WeatherActivity.this).edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }


    /*
    * 根据天气id请求城市天气信息
    * */
    public void requestWeather(String weatherID){
        showProgressDialog();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                WeatherActivity.this).edit();
        HeWeather.getWeather(WeatherActivity.this, weatherID, Lang.CHINESE_SIMPLIFIED,
                Unit.METRIC, new HeWeather.OnResultWeatherDataListBeansListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LogUtil.d("2", String.valueOf(throwable));
                                Toast.makeText(WeatherActivity.this,"获取天气信息失败抛出异常"+
                                        throwable,Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onSuccess(List<Weather> list) {
                        weatherList = list;
                        String string = null;
                        for(Weather weather : list) {
                            /*editor.putString("weather_city", weather.getBasic().getCid());*/
                            string = weather.getBasic().getCid();
                        }
                        mWeatherId = string;
                        editor.putString("weather_city",string);
                        editor.apply();
                        showWeatherInfo(weatherList);
                        swipeRefresh.setRefreshing(false);
                        closeProgressDialog();
                    }
                });
        loadBingPic();
    }


    /*
    * 处理并显示weather实体中的数据
    * */
    private void showWeatherInfo(List<Weather> list){
        showProgressDialog();
        for (Weather weather : list){
            //获取城市名
            String cityName = weather.getBasic().getLocation();
            titleCity.setText(cityName);
            //获取获取信息时间
            String updateTime = weather.getUpdate().getLoc();
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = dateFormat.parse(updateTime);
                SimpleDateFormat strFormat = new SimpleDateFormat("MM-dd HH:mm");
                titleUpdateTime.setText(strFormat.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //获取当前温度
            String degree = weather.getNow().getTmp();
            degreeTest.setText(degree + "℃");
            //获取当前天气状况
            String info = weather.getNow().getCond_txt();
            weatherInfoTest.setText(info);
            forecastLayout.removeAllViews();
            List<ForecastBase> forecastList = weather.getDaily_forecast();
            for(ForecastBase base : forecastList){
                View view = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.froecast_item,
                        forecastLayout,false);
                //获取TextView实例
                TextView dateText = (TextView) view.findViewById(R.id.date_text);
                TextView infoText = (TextView) view.findViewById(R.id.info_text);
                TextView maxText = (TextView) view.findViewById(R.id.max_text);
                TextView minText = (TextView) view.findViewById(R.id.min_text);

                //显示数据
                dateText.setText(base.getDate());
                infoText.setText(base.getCond_txt_d());
                maxText.setText(base.getTmp_max());
                minText.setText(base.getTmp_min());
                forecastLayout.addView(view);
                w_amount.setText(base.getPcpn());
                w_rate.setText(base.getPop()+"%");
            }
            suggLayout.removeAllViews();
            List<LifestyleBase> lifestyleList = weather.getLifestyle();
            int i=0;
            for (LifestyleBase base : lifestyleList){
                View view = LayoutInflater.from(WeatherActivity.this).inflate(R.layout.sugg_item,
                        suggLayout,false);
                TextView suggText = (TextView) view.findViewById(R.id.suggText);
                LogUtil.d("2",base.getTxt());
                switch (i){
                    case 0:
                        suggText.setText("舒适度指数: " + base.getTxt());
                        break;
                    case 1:
                        suggText.setText("穿衣指数: " + base.getTxt());
                        break;
                    case 2:
                        suggText.setText("感冒指数: " + base.getTxt());
                        break;
                    case 3:
                        suggText.setText("运动指数: " + base.getTxt());
                        break;
                    case 4:
                        suggText.setText("旅游指数: " + base.getTxt());
                        break;
                    case 5:
                        suggText.setText("紫外线指数: " + base.getTxt());
                        break;
                    default:
                        break;
                }
                i++;
                suggLayout.addView(view);
            }
        }
        closeProgressDialog();
    }

    /*
     * 显示对话框
     * */
    private void showProgressDialog() {
        if(progressDialog == null){
            progressDialog = new ProgressDialog(WeatherActivity.this);
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /*
     * 关闭进度对话框
     * */
    private void closeProgressDialog() {
        if(progressDialog != null){
            progressDialog.hide();
        }
    }

}
