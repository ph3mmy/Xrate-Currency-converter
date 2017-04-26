package com.jcedar.xratecurrencyconverter.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.jcedar.xratecurrencyconverter.R;
import com.jcedar.xratecurrencyconverter.adapter.ConverterSpinnerAdapter;
import com.jcedar.xratecurrencyconverter.adapter.RateListAdapter;
import com.jcedar.xratecurrencyconverter.helper.AppSingleton;
import com.jcedar.xratecurrencyconverter.helper.DataUtils;
import com.jcedar.xratecurrencyconverter.helper.Lists;
import com.jcedar.xratecurrencyconverter.model.CurrencyModel;
import com.jcedar.xratecurrencyconverter.provider.CurrencyContract;
import com.jcedar.xratecurrencyconverter.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConvertFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConvertFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConvertFragment extends Fragment {

    private EditText editText;
    private Spinner currencySpinner;
    private ConverterSpinnerAdapter spinnerAdapter;
    private List<CurrencyModel> currencyModelList = new ArrayList<>();

    private String[] symbol = {
            "AFN", "GBP", "CFA", "EUR", "NGR", "USD"
    };
    private String[] currencyName = {
            "Afghan Afgani", "Great Britain Pounds", "Central African Franc", "European Euro", "Nigerian Naira", "U.S. Dollars"
    };
    private int[] imgId = {
            R.drawable.ic_add_a_photo_black_24dp,
            R.drawable.ic_add_to_photos_black_24dp,
            R.drawable.ic_adjust_black_24dp,
            R.drawable.ic_assistant_black_24dp,
            R.drawable.ic_broken_image_black_24dp,
            R.drawable.ic_assistant_black_24dp,
    };


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConvertFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConvertFragment newInstance(String param1, String param2) {
        ConvertFragment fragment = new ConvertFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ConvertFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_convert, container, false);

        editText = (EditText) view.findViewById(R.id.et_converter);
        currencySpinner = (Spinner) view.findViewById(R.id.spinner_converter);

        spinnerAdapter = new ConverterSpinnerAdapter(getActivity(), currencyModelList);

        for(int i = 0; i < currencyName.length; i++){
            CurrencyModel currencyModel = new CurrencyModel();
            currencyModel.setSymbol(symbol[i]);
            currencyModel.setCurrencyName(currencyName[i]);
            currencyModel.setFlag(imgId[i]);
            currencyModelList.add(currencyModel);
        }
        currencySpinner.setAdapter(spinnerAdapter);

        return view;
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
