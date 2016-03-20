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
import android.widget.Spinner;
import android.widget.TextView;

//Amazon Web Services imports
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.*;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.*;
import com.amazonaws.services.dynamodbv2.model.*;


public class Home extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinner;

    private TextView tvPlayerName;
    private TextView tvPlayerState;
    private TextView tvHumanCount;
    private TextView tvZombieCount;
    private Button bt;

    private int humanCount;
    private int zombieCount;

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
        bt = (Button)findViewById(R.id.testButton);

        //initialize menu
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

        LoadPlayerCounts();

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                //perform action
                LoadUser obj = new LoadUser();
                obj.execute();
            }
        });
    }

    private void LoadPlayerCounts() {
        //do nothing...yet. Muahaha.
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

    /*public class LoadPlayerCounts extends AsyncTask<String, Void, String> {
        int humanPlayers = -1;
        int zombiePlayers = -1;

        @Override
        protected String doInBackground(String...params) {
            try {
                AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
                DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
            } catch (Exception e) {
                //handle exception
            }
            return null;
        }

        protected void onPostExecute(String page) {
            //onPostExecute
        }
    }*/

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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
