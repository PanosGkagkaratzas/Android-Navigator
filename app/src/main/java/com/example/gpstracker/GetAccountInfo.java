package com.example.gpstracker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetAccountInfo {

    @SerializedName("FirstName")
    @Expose
    private String name;

    @SerializedName("LastName")
    @Expose
    private String surname;

    @SerializedName("Email")
    @Expose
    private String email;

    @SerializedName("Register_time")
    @Expose
    private String time;

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    public String getEmail() {
        return email;
    }

    public String getTime() {
        return time;
    }
}
