package com.jcedar.xratecurrencyconverter.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Trigger;
import com.jcedar.xratecurrencyconverter.R;
import com.jcedar.xratecurrencyconverter.adapter.CurrencyRecyclerAdapter;
import com.jcedar.xratecurrencyconverter.helper.CurrencyJobService;
import com.jcedar.xratecurrencyconverter.helper.DataUtils;
import com.jcedar.xratecurrencyconverter.helper.FormatUtils;
import com.jcedar.xratecurrencyconverter.io.FetchCurrencyHandler;
import com.jcedar.xratecurrencyconverter.provider.AndroidDatabaseManager;
import com.jcedar.xratecurrencyconverter.provider.CurrencyContract;
import com.jcedar.xratecurrencyconverter.ui.DetailsActivity;

import me.tatarka.support.job.JobScheduler;

public class RateFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, CurrencyRecyclerAdapter.ClickListener,
        SearchView.OnQueryTextListener{
    private static final int JOB_ID = 100;
    private static final long SYNC_INTERVAL = (60 * 180000L);
    private final String TAG = RateFragment.class.getName();
//    public RateListAdapter adapter;
    FirebaseJobDispatcher dispatcher;
    private JobScheduler mJobScheduler;
    RecyclerView recyclerview;
    CurrencyRecyclerAdapter mAdapter;
    public TextView timeTextView;
    View root;

    private static final String SEARCH_KEY = "SEARCH_KEY";
    static int presentId;

    public static final int CURRENCY_LOADER = 0;
    public static final int SEARCH_CURRENCY_LOADER = 1;

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

        if (id == R.id.action_search){
            SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            searchView.setOnQueryTextListener(this);
            return true;
        }

        if (id == R.id.action_refresh){
            FetchCurrencyHandler.fetchTimeForTv(getActivity(), timeTextView);
            mAdapter.notifyDataSetChanged();
            getLoaderManager().restartLoader(0, null, this);
            return true;
        }

        if (id == R.id.action_check_db){
            Intent dbIntent = new Intent(getActivity(), AndroidDatabaseManager.class);
            startActivity(dbIntent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onQueryTextSubmit(String query) {

        if (!TextUtils.isEmpty(query)) {
            Bundle b = new Bundle();
            b.putString(SEARCH_KEY, query);

            getLoaderManager().restartLoader(SEARCH_CURRENCY_LOADER, b, RateFragment.this);
        }else
            getLoaderManager().restartLoader(CURRENCY_LOADER, null, RateFragment.this);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!TextUtils.isEmpty(newText)) {
            Bundle bundle = new Bundle();
            bundle.putString(SEARCH_KEY, newText);

            getLoaderManager().restartLoader(SEARCH_CURRENCY_LOADER, bundle, RateFragment.this);
        } else
            getLoaderManager().restartLoader(CURRENCY_LOADER, null, RateFragment.this);
        return true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(CURRENCY_LOADER, null, this);
        getLoaderManager().initLoader(SEARCH_CURRENCY_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
            root = inflater.inflate(R.layout.fragment_rate, container, false);
//        TextView errorMsg = (TextView) root.findViewById(R.id.tv_error);

//        mJobScheduler = JobScheduler.getInstance(getActivity());
//        constructJob();
        dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(getActivity()));
        schedulePeriodicJob();

        timeTextView = (TextView) root.findViewById(R.id.tv_time_stamp);
        recyclerview = (RecyclerView) root.findViewById(R.id.rate_recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new CurrencyRecyclerAdapter(getActivity());
        mAdapter.setClicklistener(this);
        recyclerview.setAdapter(mAdapter);

        if (DataUtils.isCurrencyFirstRun(getActivity())){
            timeTextView.setText("Never Updated");
        } else {
            String tt = fetchTimeForTv(getActivity());
            timeTextView.setText(tt);
        }
        getLoaderManager().restartLoader(0, null, this);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
//        getLoaderManager().restartLoader(CURRENCY_LOADER, null, this);
        getLoaderManager().restartLoader(SEARCH_CURRENCY_LOADER, null, this);

        try {
            String tt = fetchTimeForTv(getActivity());
            timeTextView.setText(tt);
        } catch (Exception e){
            e.printStackTrace();
            timeTextView.setText("Not Yet Updated");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = CurrencyContract.Currency.CONTENT_URI;

        if (id == SEARCH_CURRENCY_LOADER) {

            presentId = SEARCH_CURRENCY_LOADER;

            if (args != null) {
                String query = args.getString(SEARCH_KEY);

//                String selection1 = CurrencyContract.Currency.CURRENCY_SYMBOL +" LIKE '%" +query + "%'";


                String selection1 = CurrencyContract.Currency.CURRENCY_SYMBOL + " LIKE '%" +query + "%' OR "
                        +CurrencyContract.CurrencyName.CURRENCY_NAME + " LIKE '%" +query+ "%'";

                return new CursorLoader(
                        getActivity(),
                        uri,
                        CurrencyContract.Currency.PROJECTION_ALL,
                        selection1,    // selection
                        null,           // arguments
                        CurrencyContract.Currency.CURRENCY_SYMBOL + " ASC");

            } else {
                return new CursorLoader(
                        getActivity(),
                        uri,
                        CurrencyContract.Currency.PROJECTION_ALL,
                        null,    // selection
                        null,           // arguments
                        CurrencyContract.Currency.CURRENCY_SYMBOL + " ASC");
            }
        }

        else {
            presentId = CURRENCY_LOADER;
            return new CursorLoader(getActivity(),
                    uri,
                    null,
                    null,
                    null,
                    null);

        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);

        Log.e(TAG, "returned Cursor data " + data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    /** job performing frequent updates**/
/*    private void constructJob () {
        long longInterval = (freq * 60 * 60*1000);
//        long longInterval = (freq*5*1000);
        Log.e(TAG, "Job interval : " + longInterval);
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID, new ComponentName(getContext(), CurrencyService.class));
        builder.setPeriodic(longInterval)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                .setPersisted(true);

        mJobScheduler.schedule(builder.build());
    }*/

    private void schedulePeriodicJob() {

        int freq = Integer.parseInt(DataUtils.getUpdateFrequency(getActivity()));
        int interval = (freq * 60 * 60);
        dispatcher.mustSchedule(
                dispatcher.newJobBuilder()
                .setService(CurrencyJobService.class)
                .setTag("CurrencyJobService")
                .setRecurring(true)
                .setTrigger(Trigger.executionWindow(3595,3600))
                .build()
        );
    }

    @SuppressLint("CommitTransaction")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().getSupportFragmentManager().beginTransaction().add(this, "rate_frag");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        CurrencySyncAdapter.initializeSyncAdapter(getActivity());
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        getActivity().getContentResolver().registerContentObserver(CurrencyContract.Currency.CONTENT_URI, true, mObserver);

    }

    public  String fetchTimeForTv (Context context){
        Cursor c = context.getContentResolver().query(CurrencyContract.Currency.CONTENT_URI,
                new String[]{CurrencyContract.Currency.TIME_STAMP}, null, null, null);
//        assert c != null;
        String ff = null;
        if (c != null) {
            c.moveToFirst();
            long timeBase = Long.parseLong(c.getString(c.getColumnIndex(CurrencyContract.Currency.TIME_STAMP)));
            Log.e(TAG, "Returned time Stamp " + timeBase);
            ff = FormatUtils.getReadableDate(timeBase);
//            tv.setText(ff);
        } c.close();
        return ff;
    }

    private final ContentObserver mObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            if (!isAdded()) {
                return;
            }
            getLoaderManager().restartLoader(presentId, null, RateFragment.this);
        }
    };

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        getActivity().getContentResolver().unregisterContentObserver(mObserver);
    }

/*    @Override
    public void itemClickListener(Cursor cursor) {
        long id = cursor.getColumnIndex(CurrencyContract.Currency._ID);
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        mAdapter.getItem()
        Log.e(TAG, "onclick item id: " + id);
        intent.putExtra("data", id);
        startActivity(intent);
    }*/

    @Override
    public void itemClickListener(View view, int position) {
        Cursor cursor = mAdapter.getItem(position);
        long symbi = Long.parseLong(cursor.getString(cursor.getColumnIndex(CurrencyContract.Currency._ID)));
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        intent.putExtra("data", symbi);
        startActivity(intent);
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
