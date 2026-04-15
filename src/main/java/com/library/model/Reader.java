package com.library.model;

import java.time.LocalDate;

public class Reader {
    private int       id;
    private String    fullName;
    private String    email;
    private String    phone;
    private LocalDate regDate;

    public Reader() {}

    public Reader(int id, String fullName, String email,
                  String phone, LocalDate regDate) {
        this.id = id; this.fullName = fullName;
        this.email = email; this.phone = phone; this.regDate = regDate;
    }

    public int       getId()       { return id; }
    public String    getFullName() { return fullName; }
    public String    getEmail()    { return email; }
    public String    getPhone()    { return phone; }
    public LocalDate getRegDate()  { return regDate; }

    public void setId(int id)              { this.id = id; }
    public void setFullName(String n)      { this.fullName = n; }
    public void setEmail(String email)     { this.email = email; }
    public void setPhone(String phone)     { this.phone = phone; }
    public void setRegDate(LocalDate d)    { this.regDate = d; }
}