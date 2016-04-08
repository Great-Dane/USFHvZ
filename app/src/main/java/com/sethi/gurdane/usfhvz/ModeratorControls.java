package com.sethi.gurdane.usfhvz;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ModeratorControls extends AppCompatActivity {

    String killId="";

    EditText etTitle;
    EditText etBody;
    EditText etKillId;
    Button btPostAnnouncement;
    Button btKillHuman;
    Button btCureZombie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_controls);

        etTitle = (EditText)findViewById(R.id.title_text);
        etBody = (EditText)findViewById(R.id.body_text);
        etKillId = (EditText)findViewById(R.id.kill_id_text);
        btPostAnnouncement = (Button)findViewById(R.id.announcement_button);
        btKillHuman = (Button)findViewById(R.id.bt_kill_human);
        btCureZombie = (Button)findViewById(R.id.bt_cure_zombie);

        etTitle.setText("");
        etBody.setText("");
        etKillId.setText("");

        btPostAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //perform action
                //Do not post if title is blank
                if (etTitle.getText().toString().trim().equals("")) {
                    Toast.makeText(ModeratorControls.this, "No title entered for announcement!", Toast.LENGTH_SHORT).show();
                    //Do not post if body is blank
                } else if (etBody.getText().toString().trim().equals("")) {
                    Toast.makeText(ModeratorControls.this, "No body entered for announcement!", Toast.LENGTH_SHORT).show();
                } else { //Post announcement
                    PostAnnouncement obj = new PostAnnouncement();
                    obj.execute();
                }
            }
        });

        btKillHuman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //perform action
                //Do not post if KillId is blank
                if (etKillId.getText().toString().trim().equals("")) {
                    Toast.makeText(ModeratorControls.this, "No kill ID entered!", Toast.LENGTH_SHORT).show();
                    //Do not post if KillId is invalid
                } else if (etKillId.getText().toString().trim().length() != 6) {
                    Toast.makeText(ModeratorControls.this, "Invalid kill ID!", Toast.LENGTH_SHORT).show();
                } else { //Kill human
                    killId = etKillId.getText().toString();
                    KillHuman obj = new KillHuman();
                    obj.execute();
                }
            }
        });

        btCureZombie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //perform action
                //Do not post if KillId is blank
                if (etKillId.getText().toString().trim().equals("")) {
                    Toast.makeText(ModeratorControls.this, "No kill ID entered!", Toast.LENGTH_SHORT).show();
                    //Do not post if KillId is invalid
                } else if (etKillId.getText().toString().trim().length() != 6) {
                    Toast.makeText(ModeratorControls.this, "Invalid kill ID!", Toast.LENGTH_SHORT).show();
                } else { //Cure zombie
                    killId = etKillId.getText().toString();
                    CureZombie obj = new CureZombie();
                    obj.execute();
                }
            }
        });
    }

    public class PostAnnouncement extends AsyncTask<String, Void, String> {

        private String t = "";
        private String b = "";

        @Override
        protected void onPreExecute() {
            t = etTitle.getText().toString();
            b = etBody.getText().toString();
        }

        @Override
        protected String doInBackground(String...params) {
            try {
                AmazonDynamoDBClient client = new AmazonDynamoDBClient(Home.credentialsProvider);
                DynamoDBMapper map = new DynamoDBMapper(client);

                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String formattedDate = sdf.format(c.getTime());

                USFHvZ_Announcement announcement = new USFHvZ_Announcement();
                announcement.setDateTime(formattedDate);
                announcement.setTitle(t);
                announcement.setBody(b);
                map.save(announcement);
            } catch (Exception e) {
                //handle exception
            }
            return null;
        }

        protected void onPostExecute(String page) {
            //onPostExecute
            etTitle.setText("");
            etBody.setText("");
            Intent intent = new Intent (getApplicationContext(), Home.class);
            startActivity(intent);
        }
    }

    public class KillHuman extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String...params) {
            //try {
                //Initialize DynamoDB client and object mapper
                AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(Home.credentialsProvider);
                DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
                USFHvZ_Users user = mapper.load(USFHvZ_Users.class,killId);
                user.setState("Zombie");
                mapper.save(user);
            //} catch (Exception e) {
                //handle exception
            //}
            return null;
        }

        protected void onPostExecute(String page) {
            //onPostExecute
            etKillId.setText("");
        }
    }

    public class CureZombie extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String...params) {
            try {
                //Initialize DynamoDB client and object mapper
                AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(Home.credentialsProvider);
                DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
                USFHvZ_Users user = mapper.load(USFHvZ_Users.class,killId);
                user.setState("Human");
                mapper.save(user);
            } catch (Exception e) {
                //handle exception
            }
            return null;
        }

        protected void onPostExecute(String page) {
            //onPostExecute
            etKillId.setText("");
        }
    }
}
