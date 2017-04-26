package com.jcedar.xratecurrencyconverter.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.jcedar.xratecurrencyconverter.R;
import com.jcedar.xratecurrencyconverter.helper.DataUtils;
import com.jcedar.xratecurrencyconverter.provider.CurrencyContract;

/**
 * Created by OLUWAPHEMMY on 11/18/2016.
 */
public class CurrencyRecyclerAdapter extends RecyclerViewCursorAdapter<CurrencyRecyclerAdapter.CurrencyViewHolder>  {

    LayoutInflater inflater;
    private ClickListener clicklistener;
    Context mContext;
    String userId;

    public CurrencyRecyclerAdapter(Context context) {

        inflater = LayoutInflater.from(context);
        this.mContext = context;


    }

    @Override
    public CurrencyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.list_item_rate, parent, false);

//        view.setOnClickListener(this);
        return new CurrencyViewHolder(view, mContext);
    }

    public void setClicklistener (ClickListener clicklistener){
        this.clicklistener = clicklistener;
    }

    @Override
    public void onBindViewHolder(CurrencyViewHolder holder, Cursor cursor) {

        userId = cursor.getString(
                cursor.getColumnIndex(CurrencyContract.Currency._ID));

        final String country = cursor.getString(cursor.getColumnIndex(CurrencyContract.Currency.CURRENCY_CODE));
        String symbol = cursor.getString(cursor.getColumnIndex(CurrencyContract.Currency.CURRENCY_SYMBOL));

//        String name = cursor.getString(cursor.getColumnIndex(CurrencyContract.Currency.BASE_CURRENCY_SYMBOL));
        String base_rate = cursor.getString(cursor.getColumnIndex(CurrencyContract.Currency.BASE_RATE));
        String inv_rate = cursor.getString(cursor.getColumnIndex(CurrencyContract.Currency.INVERTED_RATE));
//        String timeBase = cursor.getString(cursor.getColumnIndex(CurrencyContract.Currency.TIME_STAMP));

        String codeName = getCountryCode(symbol);

//        holder.images.setImageResource();

        holder.tvSymbol.setText(symbol.toUpperCase());
        holder.tvCurrencyName.setText(codeName);
        holder.tvBaseRate.setText(formatBaseRate(symbol, base_rate));
        holder.tvInvRate.setText(formatInvRate(symbol, inv_rate));

        int id = DataUtils.imageId(symbol, country, mContext);
        if (id != 0) {
            Drawable res = mContext.getResources().getDrawable(id);
            holder.images.setImageDrawable(res);
        } else
            holder.images.setImageResource(R.mipmap.ic_launcher);


//        loadCountryImage(CurrencyViewHolder.images, country.toLowerCase(), symbol);

    }


 /*   private int imageId (String symbol, String country) {
        int id;
        String countryCode = country.toLowerCase();
        if (countryCode.equalsIgnoreCase("do")) {
            String mUri = "@drawable/do1";
            id = mContext.getResources().getIdentifier(mUri, "raw", mContext.getPackageName());
        } else if (symbol.equalsIgnoreCase("xaf")) {
            String mUri = "@drawable/xaf";
            id = mContext.getResources().getIdentifier(mUri, "raw", mContext.getPackageName());
        } else if (symbol.equalsIgnoreCase("xag")) {
            String mUri = "@drawable/xag";
            id = mContext.getResources().getIdentifier(mUri, "raw", mContext.getPackageName());
        } else if (symbol.equalsIgnoreCase("xau")) {
            String mUri = "@drawable/xau";
            id = mContext.getResources().getIdentifier(mUri, "raw", mContext.getPackageName());
        } else {
            String uri = "@drawable/" + countryCode;
            id = mContext.getResources().getIdentifier(uri, "raw", mContext.getPackageName());
        }

        return id;
    }*/

/*    @Override
    public void onClick(View view) {
        if (clicklistener != null) {

            RecyclerView recyclerView = (RecyclerView) view.getParent();
            int position = recyclerView.getChildLayoutPosition(view);
            if (position!= RecyclerView.NO_POSITION) {

                final Cursor cursor = this.getItem(position) ;
                Log.e("ADAPTER", "adapter onclick cursor: " + cursor);
                clicklistener.itemClickListener(cursor);
            }

        }
    }*/

  /*  @Override
    public int getItemCount() {
        return 0;
    }*/

    class CurrencyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        ImageView images;
        TextView tvSymbol, tvCurrencyName, tvBaseRate, tvInvRate;
        Context context;

        public CurrencyViewHolder(View itemView, Context context) {
            super(itemView);
            this.context = context;

            Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/proxima-nova-semibold.ttf");
            Typeface tfSym= Typeface.createFromAsset(context.getAssets(), "fonts/proxima-nova-cond-semibold.ttf");

             images = (ImageView) itemView.findViewById(R.id.iv_rate_country);
            tvSymbol = (TextView) itemView.findViewById(R.id.tv_rate_currency_symbol);
            tvSymbol.setTypeface(tfSym);
            tvCurrencyName = (TextView) itemView.findViewById(R.id.tv_rate_currency_name);
            tvCurrencyName.setTypeface(tf);
            tvBaseRate = (TextView) itemView.findViewById(R.id.tv_base_rate);
            tvInvRate = (TextView) itemView.findViewById(R.id.tv_inv_rate);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (clicklistener != null) {
                clicklistener.itemClickListener(view, getAdapterPosition());
            }
        }

/*        @Override
        public void onClick(View view) {
            if (clicklistener != null) {

                RecyclerView recyclerView = (RecyclerView) view.getParent();
                int position = recyclerView.getChildAdapterPosition(view);
                if (position!= RecyclerView.NO_POSITION) {

                   final Cursor cursor = ;
                    Log.e("ADAPTER", "adapter onclick cursor: " + cursor);
                    clicklistener.itemClickListener(cursor);
                }

            }
        }*/
    }

    public interface ClickListener  {
        public void itemClickListener(View view, int position);
//        public void itemClickListener(Cursor cursor);
    }


    public String getCountryCode(String code) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        return sp.getString(code, "");
    }


    private String formatInvRate(String currencySym, String inverseRate) {
        return ("1 " +currencySym + " = " + inverseRate + " USD");
    }

    private String formatBaseRate(String currencySym, String baseRate) {
        return ("1 USD = " + baseRate + " " +currencySym);
    }

}
