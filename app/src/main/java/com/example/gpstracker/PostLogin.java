package com.example.gpstracker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostLogin {

    @SerializedName("Email")
    @Expose
    private String email;

    @SerializedName("Password")
    @Expose
    private String pass;

    public PostLogin(String email, String pass)
    {
        this.email = email;
        this.pass = pass;
    }
}
