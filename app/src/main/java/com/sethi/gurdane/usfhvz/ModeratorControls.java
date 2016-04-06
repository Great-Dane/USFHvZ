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

    EditText etTitle;
    EditText etBody;
    Button btPostAnnouncement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moderator_controls);

        etTitle = (EditText)findViewById(R.id.title_text);
        etBody = (EditText)findViewById(R.id.body_text);
        btPostAnnouncement = (Button)findViewById(R.id.announcement_button);

        etTitle.setText("");
        etBody.setText("");

        btPostAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                //perform action
                PostAnnouncement obj = new PostAnnouncement();
                obj.execute();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_moderator_controls, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
