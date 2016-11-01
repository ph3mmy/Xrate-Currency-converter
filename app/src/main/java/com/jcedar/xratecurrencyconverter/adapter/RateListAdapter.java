package com.jcedar.xratecurrencyconverter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.jcedar.xratecurrencyconverter.R;
import com.jcedar.xratecurrencyconverter.model.CurrencyModel;

import java.util.List;

/**
 * Created by OLUWAPHEMMY on 10/16/2016.
 */
public class RateListAdapter extends BaseAdapter {

    Context mContext;
    private LayoutInflater inflater;
    private List<CurrencyModel> currency;

    public RateListAdapter (Context context, List<CurrencyModel> mCurrency){
        this.mContext = context;
        this.currency = mCurrency;
    }

    @Override
    public int getCount() {
        return currency.size();
    }

    @Override
    public Object getItem(int i) {
        return currency.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return getMyView(i, view, viewGroup);
    }

    public View getMyView (int position, View view, ViewGroup viewGroup){
            if (inflater == null) {
                inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            if (view == null) {
                view = inflater.inflate(R.layout.list_item_rate, viewGroup, false);
            }

        ImageView images = (ImageView) view.findViewById(R.id.iv_rate_country);
        TextView tvSymbol = (TextView) view.findViewById(R.id.tv_rate_currency_symbol);
        TextView tvCurrencyName = (TextView) view.findViewById(R.id.tv_rate_currency_name);
        TextView tvBaseRate = (TextView) view.findViewById(R.id.tv_base_rate);
        TextView tvInvRate = (TextView) view.findViewById(R.id.tv_inv_rate);

        CurrencyModel currencyModel = currency.get(position);
        images.setImageResource(currencyModel.getFlag());
        tvSymbol.setText(currencyModel.getSymbol());
        tvCurrencyName.setText(currencyModel.getCurrencyName());
        tvBaseRate.setText(currencyModel.getBaseRate());
        tvInvRate.setText(currencyModel.getInvRate().toString());

        return view;
    }
}
