package de.thieme.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {

    private String email;

    @SerializedName("pwd")
    private String password;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}