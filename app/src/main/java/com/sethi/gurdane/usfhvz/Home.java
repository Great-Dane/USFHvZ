package com.sethi.gurdane.usfhvz;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

//Amazon Web Services imports
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Home extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinner;

    private TextView tvPlayerName;
    private TextView tvPlayerState;
    private TextView tvHumanCount;
    private TextView tvZombieCount;
    private Button bt;

    private ListView announcementsListView;
    List<USFHvZ_Announcement> announcements;
    private AnnouncementAdapter adapter;

    public static CognitoCachingCredentialsProvider credentialsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Initialize TextViews and button
        tvPlayerName  = (TextView)findViewById(R.id.player_name);
        tvPlayerState = (TextView)findViewById(R.id.team);
        tvHumanCount = (TextView)findViewById(R.id.humans_count);
        tvZombieCount = (TextView)findViewById(R.id.zombies_count);

        //Initialize menu
        spinner = (Spinner) findViewById(R.id.spinner_home);
        ArrayAdapter<CharSequence> menuAdapter = ArrayAdapter.createFromResource(this, R.array.app_menu, android.R.layout.simple_spinner_item);
        spinner.setAdapter(menuAdapter);
        spinner.setOnItemSelectedListener(this);

        //Initialize AWS credentials
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),                            //application context
                "us-east-1:75be3ada-e2d4-46b6-8cd3-f18a69903d11",   //identity pool ID
                Regions.US_EAST_1                                   //Amazon region
        );

        announcementsListView = (ListView) findViewById(R.id.list_Home);
        announcements = new ArrayList<>();
        LoadAnnouncements la = new LoadAnnouncements();
        la.execute();
        if (announcements != null) {
            adapter = new AnnouncementAdapter(Home.this, announcements);
        }
        else {
            //Toast.makeText(Home.this, "announcements == null", Toast.LENGTH_SHORT).show();
        }
        announcementsListView.setAdapter(adapter);

        LoadPlayerCounts lpc = new LoadPlayerCounts();
        lpc.execute();
    }

    //Load moderator/game announcements
    public class LoadAnnouncements extends AsyncTask<String, Void, String> {
        PaginatedScanList<USFHvZ_Announcement> result;
        int size;

        @Override
        protected String doInBackground(String...params) {
            //try {
                //Set up DynamoDB client and mapper
                AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
                DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

                DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                result = mapper.scan(USFHvZ_Announcement.class, scanExpression);

                size = result.size();

                //announcements.addAll(result);

                //Delete below if addAll works
                for (int i=0; i<result.size(); i++) {
                    USFHvZ_Announcement a = result.get(i);
                    announcements.add(a);
                }

            //} catch (Exception e) {
                //handle exception
            //}
            return null;
        }

        protected void onPostExecute(String page) {
            //onPostExecute
            Toast.makeText(getApplicationContext(), "result.size() = " + size, Toast.LENGTH_SHORT).show();
        }
    }

    //Load counts of human and zombie players into home screen.
    public class LoadPlayerCounts extends AsyncTask<String, Void, String> {
        int zCount = 0;
        int hCount = 0;
        PaginatedQueryList<USFHvZ_Users> result;

        @Override
        protected String doInBackground(String...params) {
            try {
                //Set up DynamoDB client and mapper
                AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
                DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

                //Load zombie player count.
                USFHvZ_Users zombies = new USFHvZ_Users();
                zombies.setState("Zombie");

                DynamoDBQueryExpression queryExpression1 = new DynamoDBQueryExpression()
                        .withHashKeyValues(zombies)
                        .withConsistentRead(false);

                result = mapper.query(USFHvZ_Users.class, queryExpression1);
                zCount = result.size();

                //Load human player count.
                USFHvZ_Users humans = new USFHvZ_Users();
                humans.setState("Human");

                DynamoDBQueryExpression queryExpression2 = new DynamoDBQueryExpression()
                        .withHashKeyValues(humans)
                        .withConsistentRead(false);

                result = mapper.query(USFHvZ_Users.class, queryExpression2);
                hCount = result.size();
            } catch (Exception e) {
                //handle exception
            }
            return null;
        }

        protected void onPostExecute(String page) {
            if (zCount == 0) {
                tvZombieCount.setText("Something went wrong.");
                tvHumanCount.setText("Something went wrong.");
            } else {
                tvZombieCount.setText("ZOMBIES: " + zCount);
                tvHumanCount.setText("HUMANS: " + hCount);
            }
        }
    }

    public class LoadUser extends AsyncTask<String, Void, String> {

        String iname = "";
        String istate = "";

        @Override
        protected String doInBackground(String...params) {
            try {
                AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
                DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
                USFHvZ_Users suser = mapper.load(USFHvZ_Users.class, "IDGAF");
                iname = suser.getName();
                istate = suser.getState();
            } catch (Exception e) {
                //handle exception
            }
            return null;
        }

        protected void onPostExecute(String page) {
            //onPostExecute
            if (!iname.equals("") && !istate.equals("")) {
                tvPlayerName.setText(iname);
                tvPlayerState.setText(istate);
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        Class selection = null;
        switch (parent.getSelectedItem().toString()) {
            case "Home":
                selection = Home.class;
                break;
            case "Rules Reference":
                selection = RulesReference.class;
                break;
            case "Register Tag":
                selection = RegisterTag.class;
                break;
            case "Dynamic Map":
                selection = HeatMap.class;
                break;
            case "Moderator Controls":
                selection = ModeratorControls.class;
                break;
            case "Log Out":
                selection = Login.class;
                break;
            default:
                return;
        }
        Intent intent = new Intent (this, selection);
        startActivity(intent);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            //Refresh player counts
            LoadPlayerCounts lpc = new LoadPlayerCounts();
            lpc.execute();
            //Refresh announcements
            LoadAnnouncements la = new LoadAnnouncements();
            la.execute();
        }

        return super.onOptionsItemSelected(item);
    }
}
