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
package gov.wa.wsdot.android.wsdot.ui.mountainpasses;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.database.mountainpasses.MountainPassEntity;
import gov.wa.wsdot.android.wsdot.di.Injectable;
import gov.wa.wsdot.android.wsdot.provider.WSDOTContract.MountainPasses;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.ui.widget.CursorRecyclerAdapter;
import gov.wa.wsdot.android.wsdot.util.ParserUtils;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class MountainPassesFragment extends BaseFragment implements
        SwipeRefreshLayout.OnRefreshListener, Injectable {

    private static final String TAG = MountainPassesFragment.class.getSimpleName();
    private View mEmptyView;
    private static SwipeRefreshLayout swipeRefreshLayout;

    private static MountainPassAdapter mAdapter;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

    private List<MountainPassEntity> mPasses = new ArrayList<>();

    private static MountainPassViewModel viewModel;

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list_with_swipe_refresh, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MountainPassAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        swipeRefreshLayout = root.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeResources(
                R.color.holo_blue_bright,
                R.color.holo_green_light,
                R.color.holo_orange_light,
                R.color.holo_red_light);

        mEmptyView = root.findViewById( R.id.empty_list_view );
        mEmptyView.setVisibility(View.GONE);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MountainPassViewModel.class);

        viewModel.getResourceStatus().observe(this, resourceStatus -> {
            if (resourceStatus != null) {
                switch (resourceStatus.status) {
                    case LOADING:
                        swipeRefreshLayout.setRefreshing(true);
                        Log.e(TAG, "HERE1");
                        break;
                    case SUCCESS:
                        swipeRefreshLayout.setRefreshing(false);
                        Log.e(TAG, "HERE2");
                        break;
                    case ERROR:
                        Log.e(TAG, "HERE3");
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(this.getContext(), "connection error", Toast.LENGTH_LONG).show();
                }
            }
        });

        viewModel.getPasses().observe(this, passes -> {
            //if (passes.size() > 0) {
            mPasses = passes;
            mAdapter.notifyDataSetChanged();
            //}
        });

        return root;
    }

    /**
     * Custom adapter for items in recycler view.
     *
     * Binds the custom ViewHolder class to it's data.
     *
     * @see android.support.v7.widget.RecyclerView.Adapter
     */
    private class MountainPassAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Regular.ttf");
        private Typeface tfb = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Bold.ttf");
        private Context context;

        private List<MtPassVH> mItems = new ArrayList<>();

        public MountainPassAdapter(Context context) {
            this.context = context;
        }

        @Override
        public MtPassVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.list_item_details_with_icon, null);
            MtPassVH viewholder = new MtPassVH(view);
            view.setTag(viewholder);
            mItems.add(viewholder);
            return viewholder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

            MtPassVH mtpassVH = (MtPassVH) viewHolder;

            MountainPassEntity pass = mPasses.get(position);

            String title = pass.getName();
            mtpassVH.title.setText(title);
            mtpassVH.title.setTypeface(tfb);

            mtpassVH.setPos(position);

            String created_at = pass.getDateUpdated();

            mtpassVH.created_at.setText(ParserUtils.relativeTime(created_at, "MMMM d, yyyy h:mm a", false));
            mtpassVH.created_at.setTypeface(tf);

            String text = pass.getWeatherCondition();

            if (text.equals("")) {
                mtpassVH.text.setVisibility(View.GONE);
            } else {
                mtpassVH.text.setVisibility(View.VISIBLE);
                mtpassVH.text.setText(text);
                mtpassVH.text.setTypeface(tf);
            }

            int icon = getResources().getIdentifier(pass.getWeatherIcon(), "drawable", getActivity().getPackageName());

            mtpassVH.icon.setImageResource(icon);

            mtpassVH.star_button.setTag(pass.getId());

            // Seems when Android recycles the views, the onCheckedChangeListener is still active
            // and the call to setChecked() causes that code within the listener to run repeatedly.
            // Assigning null to setOnCheckedChangeListener seems to fix it.
            mtpassVH.star_button.setOnCheckedChangeListener(null);
            mtpassVH.star_button.setContentDescription("favorite");
            mtpassVH.star_button
                    .setChecked(pass.getIsStarred() != 0);
            mtpassVH.star_button.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
                    int rowId = (Integer) buttonView.getTag();
                    ContentValues values = new ContentValues();
                    values.put(MountainPasses.MOUNTAIN_PASS_IS_STARRED, isChecked ? 1 : 0);

                    Snackbar added_snackbar = Snackbar
                            .make(getView(), R.string.add_favorite, Snackbar.LENGTH_SHORT);

                    Snackbar removed_snackbar = Snackbar
                            .make(getView(), R.string.remove_favorite, Snackbar.LENGTH_SHORT);

                    added_snackbar.setCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar snackbar) {
                            super.onShown(snackbar);
                            snackbar.getView().setContentDescription("added to favorites");
                            snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                        }
                    });

                    removed_snackbar.setCallback(new Snackbar.Callback() {
                        @Override
                        public void onShown(Snackbar snackbar) {
                            super.onShown(snackbar);
                            snackbar.getView().setContentDescription("removed from favorites");
                            snackbar.getView().sendAccessibilityEvent(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                        }
                    });

                    if (isChecked){
                        added_snackbar.show();
                    }else{
                        removed_snackbar.show();
                    }

                    getActivity().getContentResolver().update(
                            MountainPasses.CONTENT_URI,
                            values,
                            MountainPasses._ID + "=?",
                            new String[] {Integer.toString(rowId)}
                    );
                }
            });
        }

        @Override
        public int getItemCount() {
            return mPasses.size();
        }

        // View Holder for Mt pass list items.
        private class MtPassVH extends RecyclerView.ViewHolder implements View.OnClickListener {
            ImageView icon;
            TextView title;
            TextView created_at;
            TextView text;
            CheckBox star_button;
            int itemId;

            public MtPassVH(View view) {
                super(view);
                title = view.findViewById(R.id.title);
                created_at = view.findViewById(R.id.created_at);
                text = view.findViewById(R.id.text);
                icon = view.findViewById(R.id.icon);
                star_button = view.findViewById(R.id.star_button);
                view.setOnClickListener(this);
            }

            public void setPos(int position){
                this.itemId = position;
            }

            public void onClick(View v) {
                Bundle b = new Bundle();
                Intent intent = new Intent(getActivity(), MountainPassItemActivity.class);
                b.putInt("id", mPasses.get(this.itemId).getPassId());
                intent.putExtras(b);
                startActivity(intent);
            }
        }
    }

    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        viewModel.forceRefreshPasses();
    }
}