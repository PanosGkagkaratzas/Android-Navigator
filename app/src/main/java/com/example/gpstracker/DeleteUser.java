package com.example.gpstracker;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeleteUser {

    @SerializedName("ID")
    @Expose
    private String id;

    public DeleteUser(String user_id)
    {
        this.id = user_id;
    }
}
