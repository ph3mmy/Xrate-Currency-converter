package com.jcedar.xratecurrencyconverter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcedar.xratecurrencyconverter.R;
import com.jcedar.xratecurrencyconverter.model.CurrencyModel;

import java.util.List;

/**
 * Created by OLUWAPHEMMY on 10/18/2016.
 */
public class ConverterSpinnerAdapter extends BaseAdapter {
    Context mContext;
    private LayoutInflater inflater;
    private List<CurrencyModel> currency;

    public ConverterSpinnerAdapter (Context context, List<CurrencyModel> currencyModels){
        this.mContext = context;
        this.currency = currencyModels;
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

        if (inflater == null){
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE); }
        if (view == null){
            view = inflater.inflate(R.layout.spinner_row_item, viewGroup, false);
        }
        ImageView imagesSp = (ImageView) view.findViewById(R.id.spinner_country_flag);
        TextView tvSymbolSp = (TextView) view.findViewById(R.id.spinner_currency_symbol);
        TextView tvCurrencyNameSp = (TextView) view.findViewById(R.id.spinner_currency_name);

        CurrencyModel currencyModel = currency.get(i);
        imagesSp.setImageResource(currencyModel.getFlag());
        tvSymbolSp.setText(currencyModel.getSymbol());
        tvSymbolSp.setTextSize(20);
        tvCurrencyNameSp.setVisibility(View.GONE);
//        tvCurrencyNameSp.setText(currencyModel.getCurrencyName());

        return view;
    }
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return spinnerView(position, convertView, parent);
    }

    public View spinnerView (int position, View convertView, ViewGroup viewGroup){
        if (inflater == null){
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE); }
        if (convertView == null){
            convertView = inflater.inflate(R.layout.spinner_row_item, viewGroup, false);
        }
        ImageView imagesSp = (ImageView) convertView.findViewById(R.id.spinner_country_flag);
        TextView tvSymbolSp = (TextView) convertView.findViewById(R.id.spinner_currency_symbol);
        TextView tvCurrencyNameSp = (TextView) convertView.findViewById(R.id.spinner_currency_name);

        CurrencyModel currencyModel = currency.get(position);
        imagesSp.setImageResource(currencyModel.getFlag());
        tvSymbolSp.setText(currencyModel.getSymbol());
        tvCurrencyNameSp.setText(currencyModel.getCurrencyName());

        return convertView;
    }
}
