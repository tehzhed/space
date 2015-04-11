package com.spaceapps.meatanagram.spaceappsproject;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.origamilabs.library.views.StaggeredGridView;
import com.parse.ParseFacebookUtils;
import com.parse.ui.ParseLoginActivity;
import com.parse.ui.ParseLoginBuilder;

public class MainActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ParseFacebookUtils.getSession() == null)
        {
            ParseLoginBuilder builder = new ParseLoginBuilder(this);

            // builder.setAppLogo(R.drawable.app_logo);
            builder.setFacebookLoginEnabled(true);
            builder.setParseLoginEnabled(false);

            Intent loginIntent = builder.build();
            loginIntent.setClass(this, ParseLoginActivity.class);

            startActivityForResult(loginIntent, R.integer.login_request_code);
        }

        /*
        sgv = (StaggeredGridView)findViewById(R.id.staggeredGridView1);
        sgv.setItemMargin(margin); // set the GridView margin
        sgv.setPadding(margin, 0, margin, 0); // have the margin on the sides as well

        StaggeredAdapter adapter = new StaggeredAdapter(MainActivity.this, R.id.imageView1, urls);

        sgv.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        */

        this.getSupportFragmentManager().beginTransaction().replace(R.id.frag_holder, new MainFragment()).commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
