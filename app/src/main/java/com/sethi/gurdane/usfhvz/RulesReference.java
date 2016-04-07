package com.sethi.gurdane.usfhvz;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class RulesReference extends AppCompatActivity {

    private Spinner spinner;

    Button bt;

    WebView wv;

    private static final String GAME_RULES = "file:///android_asset/game_rules.html";
    private static final String PERKS = "file:///android_asset/perks.html";
    private static final String GAME_MAP_AND_TIMES = "file:///android_asset/game_map_and_times.html";
    private static final String ACHIEVEMENTS = "file:///android_asset/achievements.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules_reference);

        wv= (WebView)findViewById(R.id.wv_game_rules);
        wv.loadUrl(PERKS);

        //Initialize spinner
        spinner = (Spinner) findViewById(R.id.spinner_rules_reference);
        ArrayAdapter<CharSequence> menuAdapter = ArrayAdapter.createFromResource(this, R.array.rules_reference_spinner_values, R.layout.spinner_item);
        menuAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinner.setAdapter(menuAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos,long id) {
                switch (parent.getSelectedItem().toString()) {
                    case "Game Rules":
                        wv.loadUrl(GAME_RULES);
                        break;
                    case "Perks":
                        wv.loadUrl(PERKS);
                        break;
                    case "Game Map and Times":
                        wv.loadUrl(GAME_MAP_AND_TIMES);
                        break;
                    case "Achievements":
                        wv.loadUrl(ACHIEVEMENTS);
                        break;
                    default:
                        return;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // Auto-generated method stub
            }
        });
    }
}
