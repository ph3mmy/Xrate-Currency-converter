package com.jcedar.xratecurrencyconverter.fragment;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcedar.xratecurrencyconverter.R;
import com.jcedar.xratecurrencyconverter.helper.DataUtils;
import com.jcedar.xratecurrencyconverter.provider.CurrencyContract;
import com.jcedar.xratecurrencyconverter.ui.DetailsActivity;

/**
 * Created by OLUWAPHEMMY on 1/15/2017.
 */

public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = DetailsFragment.class.getSimpleName();
    private static final int DETAIL_LOADER = 0;
    View rootView;
    static Uri dataUri;


    //reuired public constructor
    public DetailsFragment () {

    }

    public static DetailsFragment newInstance(long id,
                                                     DetailsActivity detailsActivity) {
        DetailsFragment fragment = new DetailsFragment();
        Uri uri = CurrencyContract.Currency.buildCurrencyUri(id);
        Log.e(TAG, uri+" uri");
        dataUri = uri;
        Log.e(TAG, dataUri+" data uri");
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_details, container, false);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                                                        getActivity(),
                                                        dataUri,
                                                        null,
                                                        null,
                                                        null,
                                                        null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (!data.moveToFirst()) {
            return;
        }

        TextView tvSym = (TextView) rootView.findViewById(R.id.detail_tv_symbol);
        TextView tvName = (TextView) rootView.findViewById(R.id.detail_tv_name);
        ImageView icon_flag = (ImageView) rootView.findViewById(R.id.detail_iv_image);

        String symbol = data.getString(data.getColumnIndex(CurrencyContract.Currency.CURRENCY_SYMBOL));
        String currCode = data.getString(data.getColumnIndex(CurrencyContract.Currency.CURRENCY_CODE));

        tvSym.setText(symbol);
        tvName.setText(getCountryCode(symbol));

        int id = DataUtils.imageId(symbol, currCode, getActivity());
        if (id != 0) {
            Drawable res = getActivity().getResources().getDrawable(id);
            icon_flag.setImageDrawable(res);
        } else
            icon_flag.setImageResource(R.mipmap.ic_launcher);
    }


    @Override
    public void onLoaderReset(Loader loader) {
        getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }

    public String getCountryCode(String code) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return sp.getString(code, "");
    }
}
