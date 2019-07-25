/*
 * Copyright (c) 2017 Washington State Department of Transportation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */

package gov.wa.wsdot.android.wsdot.ui.tollrates;

import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;

import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.tollrates.constant.TollRateTable;
import gov.wa.wsdot.android.wsdot.database.tollrates.constant.tolltable.tollrows.TollRowEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.Converters;
import gov.wa.wsdot.android.wsdot.util.Utils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;
import gov.wa.wsdot.android.wsdot.util.sort.SortTollGroupByDirection;
import gov.wa.wsdot.android.wsdot.util.sort.SortTollGroupByLocation;

public class SR520TollRatesFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener, Injectable {
	
    private static final String TAG = SR520TollRatesFragment.class.getSimpleName();
    private Adapter mAdapter;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private View mEmptyView;
    private View mMessageView;
    private static SwipeRefreshLayout swipeRefreshLayout;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    TollRatesViewModel viewModel;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new Adapter();

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        swipeRefreshLayout = root.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.holo_blue_bright,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light);

        mEmptyView = root.findViewById(R.id.empty_list_view);
        mMessageView = root.findViewById(R.id.message_text);

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TollRatesViewModel.class);

        viewModel.getResourceStatus().observe(getViewLifecycleOwner(), resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        swipeRefreshLayout.setRefreshing(true);
                        break;
                    case SUCCESS:
                        swipeRefreshLayout.setRefreshing(false);
                        break;
                    case ERROR:
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(this.getContext(), "connection error", Toast.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getTollRatesFor(520).observe(getViewLifecycleOwner(), tollRateTable -> {

            if (tollRateTable != null) {

                mAdapter.mData.clear();
                mEmptyView.setVisibility(View.GONE);

                if (!tollRateTable.tollRateTableData.getMessage().equals("")) {
                    mMessageView.setVisibility(View.VISIBLE);
                    ((TextView) mMessageView).setText(tollRateTable.tollRateTableData.getMessage());
                }

                for (TollRowEntity row: tollRateTable.rows) {

                    if (row.getHeader()) {
                        mAdapter.addSeparatorItem(row);
                    } else {
                        mAdapter.addItem(row);
                    }
                }

            } else {
                Log.e(TAG, "its null");
            }
        });

        viewModel.refresh(false);

        return root;
    }	

    /**
     * Custom adapter for items in recycler view.
     *
     * Extending RecyclerView adapter this adapter binds the custom ViewHolder
     * class to it's data.
     *
     * @see RecyclerView.Adapter
     */
    private class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;

        private TreeSet<Integer> mSeparatorsSet = new TreeSet<>();
        private ArrayList<TollRowEntity> mData = new ArrayList<>();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = null;

            switch (viewType) {
                case TYPE_ITEM:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.tollrates_three_col_row, parent, false);
                    return new ItemViewHolder(itemView);
                case TYPE_SEPARATOR:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.tollrates_three_col_header, parent, false);
                    return new TitleViewHolder(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewholder, int position) {

            ItemViewHolder itemholder;
            TitleViewHolder titleholder;

            TollRowEntity row = mData.get(position);

            String[] rowArray = Converters.fromJsonString(row.getRowValues());

            try {
                if (getItemViewType(position) == TYPE_ITEM) {
                    itemholder = (ItemViewHolder) viewholder;
                    itemholder.hours.setText(rowArray[0]);
                    itemholder.hours.setTypeface(tf);
                    itemholder.goodToGoPass.setText(rowArray[1]);
                    itemholder.goodToGoPass.setTypeface(tf);
                    itemholder.payByMail.setText(rowArray[2]);
                    itemholder.payByMail.setTypeface(tf);

                    TypedValue typedValue = new TypedValue();

                    TypedArray a = getActivity().obtainStyledAttributes(typedValue.data, new int[] { android.R.attr.textColorPrimary });
                    int textColor = a.getColor(0, 0);
                    a.recycle();

                    TypedArray primary = getActivity().obtainStyledAttributes(typedValue.data, new int[] { R.attr.colorPrimary });
                    int primaryColor = primary.getColor(0, 0);
                    primary.recycle();

                    TypedArray bg = getActivity().obtainStyledAttributes(typedValue.data, new int[] { android.R.attr.background });
                    int bgColor = bg.getColor(0, 0);
                    primary.recycle();

                    int white = getResources().getColor(R.color.white);

                    itemholder.itemView.setBackgroundColor(bgColor);
                    itemholder.hours.setTextColor(textColor);
                    itemholder.goodToGoPass.setTextColor(textColor);
                    itemholder.payByMail.setTextColor(textColor);

                    if (Utils.isCurrentHour(row.getStartTime(), row.getEndTime(), Calendar.getInstance())){
                        if ((row.getWeekday() && !Utils.isWeekendOrWAC_468_270_071Holiday(Calendar.getInstance()))
                                || (!row.getWeekday() && Utils.isWeekendOrWAC_468_270_071Holiday(Calendar.getInstance()))) {

                            itemholder.itemView.setBackgroundColor(primaryColor);
                            itemholder.hours.setTextColor(white);
                            itemholder.goodToGoPass.setTextColor(white);
                            itemholder.payByMail.setTextColor(white);
                        }
                    }


                } else {
                    titleholder = (TitleViewHolder) viewholder;
                    titleholder.hours.setText(rowArray[0]);
                    titleholder.hours.setTypeface(tfb);
                    titleholder.goodToGoPass.setText(rowArray[1]);
                    titleholder.goodToGoPass.setTypeface(tfb);
                    titleholder.payByMail.setText(rowArray[2]);
                    titleholder.payByMail.setTypeface(tfb);
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "map values null at:");
                Log.e(TAG, String.valueOf(position));
            }
        }

        @Override
        public int getItemViewType(int position) {
            return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
        }

        public void addItem(final TollRowEntity  item) {
            mData.add(item);
            notifyDataSetChanged();
        }

        public void addSeparatorItem(final TollRowEntity  item) {
            mData.add(item);
            // save separator position
            mSeparatorsSet.add(mData.size() - 1);
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        protected TextView hours;
        protected TextView goodToGoPass;
        protected TextView payByMail;

        public ItemViewHolder(View itemView) {
            super(itemView);
            hours = itemView.findViewById(R.id.hours);
            goodToGoPass = itemView.findViewById(R.id.goodtogo_pass);
            payByMail = itemView.findViewById(R.id.pay_by_mail);
        }
    }

    public static class TitleViewHolder extends RecyclerView.ViewHolder {
        protected TextView hours;
        protected TextView goodToGoPass;
        protected TextView payByMail;

        public TitleViewHolder(View itemView) {
            super(itemView);
            hours = itemView.findViewById(R.id.hours_title);
            goodToGoPass = itemView.findViewById(R.id.goodtogo_pass_title);
            payByMail = itemView.findViewById(R.id.pay_by_mail_title);
        }
    }

    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        viewModel.refresh(true);
    }
}
