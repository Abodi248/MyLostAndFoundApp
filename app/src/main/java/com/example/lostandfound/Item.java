package com.example.lostandfound;

public class Item {
    private int    id;
    private String postType;
    private String name;
    private String phone;
    private String description;
    private String date;
    private String location;
    private String category;
    private String imagePath;
    private String timestamp;
    // NEW
    private double latitude;
    private double longitude;

    public Item() {}

    public int    getId()                     { return id; }
    public void   setId(int id)               { this.id = id; }

    public String getPostType()               { return postType; }
    public void   setPostType(String v)       { this.postType = v; }

    public String getName()                   { return name; }
    public void   setName(String v)           { this.name = v; }

    public String getPhone()                  { return phone; }
    public void   setPhone(String v)          { this.phone = v; }

    public String getDescription()            { return description; }
    public void   setDescription(String v)    { this.description = v; }

    public String getDate()                   { return date; }
    public void   setDate(String v)           { this.date = v; }

    public String getLocation()               { return location; }
    public void   setLocation(String v)       { this.location = v; }

    public String getCategory()               { return category; }
    public void   setCategory(String v)       { this.category = v; }

    public String getImagePath()              { return imagePath; }
    public void   setImagePath(String v)      { this.imagePath = v; }

    public String getTimestamp()              { return timestamp; }
    public void   setTimestamp(String v)      { this.timestamp = v; }

    public double getLatitude()               { return latitude; }
    public void   setLatitude(double v)       { this.latitude = v; }

    public double getLongitude()              { return longitude; }
    public void   setLongitude(double v)      { this.longitude = v; }
}