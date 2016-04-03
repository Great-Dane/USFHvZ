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

    private static CognitoCachingCredentialsProvider credentialsProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialize EditTexts and button
        etEmail = (EditText)findViewById(R.id.email);
        etPassword = (EditText)findViewById(R.id.password);
        btSignIn = (Button)findViewById(R.id.email_sign_in_button);

        //Initialize AWS credentials
        credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),                            //application context
                "us-east-1:75be3ada-e2d4-46b6-8cd3-f18a69903d11",   //identity pool ID
                Regions.US_EAST_1                                   //Amazon region
        );

        btSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                //perform action
                email = etEmail.getText().toString();
                password = etPassword.getText().toString();
                VerifyCredentials obj = new VerifyCredentials();
                obj.execute();
            }
        });
    }

    private void enterApp() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    //Verify user email and password
    public class VerifyCredentials extends AsyncTask<String, Void, String> {
        PaginatedQueryList<USFHvZ_Users> result;
        int resultSize = 0;

        @Override
        protected String doInBackground(String...params) {
            //try {
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
            //} catch (Exception e) {
                //handle exception
            //}
            return null;
        }

        protected void onPostExecute(String page) {
            if (loginUser != null) {
                if (loginUser.getPassword().equals(password)) {
                    enterApp();
                } else {
                    Toast.makeText(getApplicationContext(), "Invalid password.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Invalid e-mail.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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
