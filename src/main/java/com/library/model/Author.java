package com.library.model;

public class Author {
    private int    id;
    private String fullName;
    private String country;
    private int    birthYear;

    public Author() {}

    public Author(int id, String fullName, String country, int birthYear) {
        this.id = id; this.fullName = fullName;
        this.country = country; this.birthYear = birthYear;
    }

    public int    getId()        { return id; }
    public String getFullName()  { return fullName; }
    public String getCountry()   { return country; }
    public int    getBirthYear() { return birthYear; }

    public void setId(int id)              { this.id = id; }
    public void setFullName(String n)      { this.fullName = n; }
    public void setCountry(String country) { this.country = country; }
    public void setBirthYear(int y)        { this.birthYear = y; }
}