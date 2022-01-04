package com.example.troels.runner;

/**
 * Created by Troels on 23-05-2017.
 */

public class User {
    private String fullname;
    private String email;
    private String password;

    public String getFullName() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword(){return password;}

    public void setName(String name) {
        this.fullname = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public void setPassword(String password) {
        this.password = password;
    }

}
