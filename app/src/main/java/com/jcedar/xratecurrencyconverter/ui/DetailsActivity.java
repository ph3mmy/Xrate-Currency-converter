package com.jcedar.xratecurrencyconverter.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jcedar.xratecurrencyconverter.R;
import com.jcedar.xratecurrencyconverter.fragment.DetailsFragment;

/**
 * Created by OLUWAPHEMMY on 12/8/2016.
 */
public class DetailsActivity extends AppCompatActivity {

    private static final String TAG = DetailsActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Bundle b = getIntent().getExtras();
        if (b != null) {
            long id = b.getLong("data");
            DetailsFragment fragment = DetailsFragment.newInstance(id, this);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.details, fragment)
                    .commit();

        }
    }
}
