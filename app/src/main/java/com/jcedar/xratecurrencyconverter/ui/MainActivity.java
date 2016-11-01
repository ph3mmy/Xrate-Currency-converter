package com.jcedar.xratecurrencyconverter.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.jcedar.xratecurrencyconverter.R;
import com.jcedar.xratecurrencyconverter.adapter.ViewPagerAdapter;
import com.jcedar.xratecurrencyconverter.fragment.ConvertFragment;
import com.jcedar.xratecurrencyconverter.fragment.RateFragment;
import com.jcedar.xratecurrencyconverter.io.JsonVolleyHandler;

public class MainActivity extends AppCompatActivity implements ConvertFragment.OnFragmentInteractionListener,
        RateFragment.OnFragmentInteractionListener {


    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ConvertFragment convertFragment;
    private RateFragment rateFragment;


    @Override
    protected void onStart() {
        super.onStart();

        String urlName = "http://apilayer.net/api/list?access_key=3b0cacb7e3ade580c18a3a26f23427fd";
//        JsonVolleyHandler.currencyNamesResult(urlName, getBaseContext());/*
        String url = "http://www.apilayer.net/api/live?access_key=3b0cacb7e3ade580c18a3a26f23427fd";
//        JsonVolleyHandler.showJsonResult(url, getBaseContext());*/

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        String url = "http://www.mycurrency.net/service/rates";
        /*String urlName = "http://apilayer.net/api/list?access_key=3b0cacb7e3ade580c18a3a26f23427fd";
        JsonVolleyHandler.currencyNamesResult(urlName, getBaseContext());*/
        String url = "http://www.apilayer.net/api/live?access_key=3b0cacb7e3ade580c18a3a26f23427fd";
        JsonVolleyHandler.showJsonResult(url, getBaseContext());

        convertFragment = new ConvertFragment();
        rateFragment = new RateFragment();

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        viewPager = (ViewPager) findViewById(R.id.pager);
        setupViewPager(viewPager);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setTabTextColors(ContextCompat.getColorStateList(this, R.color.tab_selector));
        tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.indicator));

    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), this);
        adapter.addFragment(convertFragment, "Converter");
        adapter.addFragment(rateFragment, "Rates");
        viewPager.setAdapter(adapter);
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
