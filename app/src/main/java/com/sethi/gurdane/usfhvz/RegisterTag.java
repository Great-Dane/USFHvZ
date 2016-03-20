package com.sethi.gurdane.usfhvz;

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
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class RegisterTag extends AppCompatActivity {

    String killId = "";

    EditText etKillId;
    Button btRegisterTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_tag);

        //Initialize text field and button
        etKillId = (EditText)findViewById(R.id.enter_kill_id);
        btRegisterTag = (Button)findViewById(R.id.register_tag);

        btRegisterTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                //perform action
                killId = etKillId.getText().toString();
                UpdateTarget obj = new UpdateTarget();
                obj.execute();
                etKillId.setText("");
            }
        });
    }

    public class UpdateTarget extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String...params) {
            try {
                //Initialize DynamoDB client amd object mapper
                AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(Home.credentialsProvider);
                DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
                USFHvZ_Users user = mapper.load(USFHvZ_Users.class,killId);
                user.setState("Zombie");
                mapper.save(user);
            } catch (Exception e) {
                //handle exception
            }
            return null;
        }

        protected void onPostExecute(String page) {
            //onPostExecute
            Toast.makeText(RegisterTag.this, "Kill registered", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register_tag, menu);
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
