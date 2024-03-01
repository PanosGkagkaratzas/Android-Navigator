package com.example.gpstracker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostChangePass {

    @SerializedName("Email")
    @Expose
    private String email;

    @SerializedName("Password")
    @Expose
    private String pass;

    @SerializedName("newPassword")
    @Expose
    private String new_pass;

    @SerializedName("newPassword2")
    @Expose
    private String new_pass2;

    public PostChangePass(String mail, String password, String new_password, String new_password2)
    {
        this.email = mail;
        this.pass = password;
        this.new_pass = new_password;
        this.new_pass2 = new_password2;
    }
}
