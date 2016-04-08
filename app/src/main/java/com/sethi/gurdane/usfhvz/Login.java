package com.sethi.gurdane.usfhvz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class Login extends AppCompatActivity {

    //Declare class variables
    String email;
    String password;
    USFHvZ_Users loginUser;

    //Declare views
    EditText etEmail;
    EditText etPassword;
    Button btSignIn;
    ImageView logo;

    //Shared Preferences variables
    public static SharedPreferences pref; //Shared Preferences
    public static SharedPreferences.Editor editor; //Editor for Shared Preferences
    public static final int PRIVATE_MODE = 0; //Shared Preferences mode
    public static final String PREF_NAME = "USFHvZPref"; //Shared Preferences file name

    //Shared Preferences keys
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_NAME = "name";
    public static final String KEY_STATE = "team";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";

    private static CognitoCachingCredentialsProvider credentialsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        //Initialize views
        etEmail = (EditText)findViewById(R.id.email);
        etPassword = (EditText)findViewById(R.id.password);
        btSignIn = (Button)findViewById(R.id.email_sign_in_button);
        logo = (ImageView) findViewById(R.id.iv_logo);
        logo.setImageResource(R.drawable.usfhvz_logo);

        //Initialize AWS credentials
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),                            //application context
                "us-east-1:75be3ada-e2d4-46b6-8cd3-f18a69903d11",   //identity pool ID
                Regions.US_EAST_1                                   //Amazon region
        );

        //Initialize Shared Prefernces
        Context context = this.getApplicationContext();
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();

        //Check login state
        checkLogin();

        btSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //perform action
                email = etEmail.getText().toString();
                password = etPassword.getText().toString();
                VerifyCredentials obj = new VerifyCredentials();
                obj.execute();
            }
        });
    }

    private void checkLogin() {
        if (pref.getBoolean(IS_LOGIN, false)) {
            //Update user state
            CheckState obj = new CheckState();
            obj.execute();
            enterApp();
        }
    }

    private void createLoginSession(String name, String team, String email, String password) {
        editor.putBoolean(IS_LOGIN, true); //Store login value as true
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_STATE, team);
        editor.putString(KEY_EMAIL, email); // Storing email
        editor.putString(KEY_PASSWORD, password); // Storing password
        editor.commit(); //Commit changes
    }

    private void enterApp() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    //Update user state
    public class CheckState extends AsyncTask<String, Void, String> {
        PaginatedQueryList<USFHvZ_Users> result;
        int resultSize = 0;

        @Override
        protected String doInBackground(String...params) {
            //try {
                //Set up DynamoDB client and mapper
                AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
                DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

                //Create USFHvZ_Users object with existing email
                USFHvZ_Users user = new USFHvZ_Users();
                user.setEmail(pref.getString(KEY_EMAIL, ""));

                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                        .withHashKeyValues(user)
                        .withConsistentRead(false);

                result = mapper.query(USFHvZ_Users.class, queryExpression);
                resultSize = result.size();
                if (result.size() > 0) {
                    loginUser = result.get(0);
                } else {
                    loginUser = null;
                }
            //} catch (Exception e) {
                //handle exception
            //}
            return null;
        }

        protected void onPostExecute(String page) {
            if (loginUser != null) {
                editor.putString(KEY_STATE, loginUser.getState());
                editor.apply();
            }
        }
    }

    //Verify user email and password
    public class VerifyCredentials extends AsyncTask<String, Void, String> {
        PaginatedQueryList<USFHvZ_Users> result;
        int resultSize = 0;

        @Override
        protected String doInBackground(String...params) {
            try {
                //Set up DynamoDB client and mapper
                AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
                DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

                //Create USFHvZ_Users object with entered email and password
                USFHvZ_Users user = new USFHvZ_Users();
                user.setEmail(email);

                DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                        .withHashKeyValues(user)
                        .withConsistentRead(false);

                result = mapper.query(USFHvZ_Users.class, queryExpression);
                resultSize = result.size();
                if (result.size() > 0) {
                    loginUser = result.get(0);
                } else {
                    loginUser = null;
                }
            } catch (Exception e) {
                //handle exception
            }
            return null;
        }

        protected void onPostExecute(String page) {
            if (loginUser != null) {
                if (loginUser.getPassword().equals(password)) {
                    if (!loginUser.getKillId().equals("000000")) { //Do not keep moderator logged in
                        //Keep user logged in to the application until they log out
                        createLoginSession(loginUser.getName(), loginUser.getState(),
                                loginUser.getEmail(), loginUser.getPassword());
                    }
                    enterApp();
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid password.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Invalid e-mail.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
