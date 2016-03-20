package com.sethi.gurdane.usfhvz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class Login extends AppCompatActivity {

    EditText etEmail;
    EditText etPassword;
    Button btSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialize EditTexts and button
        etEmail = (EditText)findViewById(R.id.email);
        etPassword = (EditText)findViewById(R.id.password);
        btSignIn = (Button)findViewById(R.id.email_sign_in_button);

        btSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                //perform action
                enterApp();
            }
        });
    }

    private void enterApp() {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
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
