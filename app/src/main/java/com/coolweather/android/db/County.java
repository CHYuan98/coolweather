package com.coolweather.android.db;

import org.litepal.crud.LitePalSupport;

public class County  extends LitePalSupport {

    private int id;

    //记录县的名称
    private String countyName;

    //记录县的天气id
    private int weatherId;

    //记录当前县所属市的代号
    private int cityId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
    }

    public int getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(int weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
