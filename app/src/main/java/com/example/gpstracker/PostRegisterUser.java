package com.example.gpstracker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostRegisterUser {

    @SerializedName("FirstName")
    @Expose
    private String name;

    @SerializedName("LastName")
    @Expose
    private String surname;

    @SerializedName("Account_Type")
    @Expose
    private String type;

    @SerializedName("Email")
    @Expose
    private String email;

    @SerializedName("Password")
    @Expose
    private String pass;

    @SerializedName("Confirm_password")
    @Expose
    private String c_pass;

    @SerializedName("Recovery_PIN")
    @Expose
    private String pin;

    public PostRegisterUser(String fname, String lname, String account_type, String email, String pass, String c_pass, String recovery_pin)
    {
        this.name = fname;
        this.surname = lname;
        this.type = account_type;
        this.email = email;
        this.pass = pass;
        this.c_pass = c_pass;
        this.pin = recovery_pin;
    }
}
