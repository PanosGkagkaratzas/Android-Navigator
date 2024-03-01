package com.example.gpstracker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostMission {

    @SerializedName("User_id")
    @Expose
    private String id;

    @SerializedName("Vehicle_plate")
    @Expose
    private String plate;

    @SerializedName("Start")
    @Expose
    private String start;

    @SerializedName("Stops")
    @Expose
    private String stops;

    @SerializedName("Coordinates")
    @Expose
    private String latlng;

    @SerializedName("Active")
    @Expose
    private Boolean active;

    @SerializedName("End_Time")
    @Expose
    private String end;

    public PostMission(String user_id, String plate, String start, String stops, String coordinates, Boolean ac, String end_time)
    {
        this.id = user_id;
        this.plate = plate;
        this.start = start;
        this.stops = stops;
        this.latlng = coordinates;
        this.active = ac;
        this.end = end_time;
    }
}
