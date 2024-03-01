package com.example.gpstracker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetVehicle {

    @SerializedName("Manufactor")
    @Expose
    private String mnfctr;

    @SerializedName("Model")
    @Expose
    private String mdl;

    @SerializedName("Type")
    @Expose
    private String tp;

    @SerializedName("Color")
    @Expose
    private String clr;

    @SerializedName("CC")
    @Expose
    private String cc;

    @SerializedName("HP")
    @Expose
    private String hp;

    @SerializedName("Date_of_construction")
    @Expose
    private String yr;

    public String getMnfctr() {
        return mnfctr;
    }

    public String getMdl() {
        return mdl;
    }

    public String getTp() {
        return tp;
    }

    public String getClr() {
        return clr;
    }

    public String getCc() {
        return cc;
    }

    public String getHp() {
        return hp;
    }

    public String getYr() {
        return yr;
    }
}
