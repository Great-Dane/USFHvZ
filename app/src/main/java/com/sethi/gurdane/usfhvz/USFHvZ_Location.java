package com.sethi.gurdane.usfhvz;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

/**
 * Created by Dane on 4/4/2016.
 * Class maps items from Amazon AWS table
 * "USFHvZ_Location" to java objects
 */

@DynamoDBTable(tableName = "USFHvZ_Location")
public class USFHvZ_Location {
    private String team;
    private String dateTime;
    private int hour;
    private int minute;
    private double latitude;
    private double longitude;

    @DynamoDBHashKey(attributeName = "Team")
    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    @DynamoDBRangeKey(attributeName = "DateTime")
    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @DynamoDBAttribute(attributeName = "Hour")
    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    @DynamoDBAttribute(attributeName = "Minute")
    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    @DynamoDBAttribute(attributeName = "Latitude")
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @DynamoDBAttribute(attributeName = "Longitude")
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
