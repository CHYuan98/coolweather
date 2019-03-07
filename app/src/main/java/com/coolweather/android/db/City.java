package com.coolweather.android.db;

import org.litepal.crud.LitePalSupport;

public class City extends LitePalSupport {

    private int id;

    //市（区）名称
    private  String cityName;

    //市（区）代号
    private int cityCode;

    //记录当前市所属市的Id
    private int provinceId;

    public int getId() {
        return id;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }
}
