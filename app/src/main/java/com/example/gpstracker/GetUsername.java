package com.example.gpstracker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetUsername {

    @SerializedName("FirstName")
    @Expose
    private String firstname;

    @SerializedName("LastName")
    @Expose
    private String lastname;

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }
}
