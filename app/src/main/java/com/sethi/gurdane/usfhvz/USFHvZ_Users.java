package com.sethi.gurdane.usfhvz;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

/**
 * Created by Dane on 3/19/2016.
 * Class maps items from Amazon AWS table
 * "USFHvZ_Users" to java objects
 */

@DynamoDBTable(tableName = "USFHvZ_Users")
public class USFHvZ_Users {
    private String killId;
    private String name;
    private String email;
    private String state;
    private String password;

    @DynamoDBHashKey(attributeName = "Kill_ID")
    public String getKillId() {
        return killId;
    }

    public void setKillId(String killId) {
        this.killId = killId;
    }

    @DynamoDBAttribute(attributeName = "Name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "email-index")
    @DynamoDBAttribute(attributeName = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "game_status-index")
    @DynamoDBAttribute(attributeName = "game_status")
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @DynamoDBAttribute(attributeName = "password")
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
