package com.example.gpstracker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostRegisterVehicle {

    @SerializedName("User_id")
    @Expose
    private String id;

    @SerializedName("Vehicle_plate")
    @Expose
    private String plt;

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

    public PostRegisterVehicle(String user_id, String plate, String manufactor, String model, String type, String color, String c, String h, String date)
    {
        this.id = user_id;
        this.plt = plate;
        this.mnfctr = manufactor;
        this.mdl = model;
        this.tp = type;
        this.clr = color;
        this.cc = c;
        this.hp = h;
        this.yr = date;
    }
}
