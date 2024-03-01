package com.example.gpstracker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostRecoverPassword {

    @SerializedName("Email")
    @Expose
    private String email;

    @SerializedName("Recovery_PIN")
    @Expose
    private String pin;

    @SerializedName("Password")
    @Expose
    private String pass;

    @SerializedName("Password2")
    @Expose
    private String pass2;

    public PostRecoverPassword(String mail, String recovery_pin, String password, String password2)
    {
        this.email = mail;
        this.pin = recovery_pin;
        this.pass = password;
        this.pass2 = password2;
    }
}
