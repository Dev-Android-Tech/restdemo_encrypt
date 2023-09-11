package com.example.restdemo;

public class Employee {
    int id;
    String sid;
    String name;

    public Employee() {}

    public Employee(int id, String sid, String name) {
        this.id = id;
        this.sid = sid;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
