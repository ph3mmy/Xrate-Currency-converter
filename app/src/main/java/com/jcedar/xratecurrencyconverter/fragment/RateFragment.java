package com.jcedar.xratecurrencyconverter.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jcedar.xratecurrencyconverter.R;
import com.jcedar.xratecurrencyconverter.adapter.RateListAdapter;
import com.jcedar.xratecurrencyconverter.io.FetchCurrencyHandler;
import com.jcedar.xratecurrencyconverter.io.JsonVolleyHandler;
import com.jcedar.xratecurrencyconverter.model.CurrencyModel;
import com.jcedar.xratecurrencyconverter.provider.AndroidDatabaseManager;

import java.util.List;


public class RateFragment extends ListFragment {
    private final String TAG = RateFragment.class.getName();
    public static RateListAdapter adapter;
    private List<CurrencyModel> currencyList = JsonVolleyHandler.currencyList;

    private String[] symbol = JsonVolleyHandler.currencySymbol;
    private String[] currencyArray = JsonVolleyHandler.currencyName;
    private Double[] invRate = JsonVolleyHandler.inverseValue;
    private String[] baseRate = JsonVolleyHandler.currencyRate;

    private int[] imgId = {
            R.drawable.ic_add_a_photo_black_24dp,
            R.drawable.ic_add_to_photos_black_24dp,
            R.drawable.ic_adjust_black_24dp,
            R.drawable.ic_assistant_black_24dp,
            R.drawable.ic_broken_image_black_24dp,
            R.drawable.ic_assistant_black_24dp
    };

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    // TODO: Rename and change types and number of parameters
    public static RateFragment newInstance(String param1, String param2) {
        RateFragment fragment = new RateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public RateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_rate_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh){
//        ""
//            String url = "http://www.mycurrency.net/service/rates";
            FetchCurrencyHandler.fetchCurrencyFromJson(getActivity());
            FetchCurrencyHandler.currencyNamesResult(getActivity());
            String url = "http://www.apilayer.net/api/live?access_key=3b0cacb7e3ade580c18a3a26f23427fd";
            JsonVolleyHandler.showJsonResult(url, getActivity());
            adapter.notifyDataSetChanged();
            return true;
        }

        if (id == R.id.action_check_db){
            Intent dbIntent = new Intent(getActivity(), AndroidDatabaseManager.class);
            startActivity(dbIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
            View root = inflater.inflate(R.layout.fragment_rate, container, false);
        ListView listView = (ListView) root.findViewById(android.R.id.list);
//        adapter = new RateListAdapter(getActivity(), currencyArray, imgId );

        if (currencyList != null) {
            adapter = new RateListAdapter(getActivity(), currencyList);
            adapter.notifyDataSetChanged();
        }
/*
        for (String mm : symbol)
        Log.e(TAG, "symbol array " + mm);*/

/*        for (int i = 0; i < symbol.length ; i++){
            CurrencyModel cModel = new CurrencyModel();
            cModel.setSymbol(symbol[i]);
            cModel.setCurrencyName(currencyArray[i]);
            cModel.setBaseRate(baseRate[i]);
            cModel.setInvRate(invRate[i]);
//            cModel.setFlag(imgId[i]);
            currencyList.add(cModel);
        }*/
        listView.setAdapter(adapter);
        return root;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
