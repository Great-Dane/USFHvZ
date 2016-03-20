package com.sethi.gurdane.usfhvz;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;

/**
 * Created by Dane on 3/20/2016.
 * Class maps items from Amazon AWS table
 * "USFHvZ_Announcement" to java objects
 */

@DynamoDBTable(tableName = "USFHvZ_Announcement")
public class USFHvZ_Announcement {
    private String dateTime;
    private String title;
    private String body;

    @DynamoDBHashKey(attributeName = "DateTime")
    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    @DynamoDBAttribute(attributeName = "Title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @DynamoDBAttribute(attributeName = "Body")
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
