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

import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.camera2.params.BlackLevelPattern;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gov.wa.wsdot.android.wsdot.R;
import gov.wa.wsdot.android.wsdot.ui.BaseFragment;
import gov.wa.wsdot.android.wsdot.util.decoration.SimpleDividerItemDecoration;

public class SR520TollRatesFragment extends BaseFragment {

    private static final String TAG = SR520TollRatesFragment.class.getSimpleName();
    private Adapter mAdapter;

    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;

	@Override
    public void onCreate(Bundle savedInstanceState) {

	    // TODO
	    getTollIndex();

        super.onCreate(savedInstanceState);        
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_list, null);

        mRecyclerView = root.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new Adapter();

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));

        // For some reason, if we omit this, NoSaveStateFrameLayout thinks we are
        // FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top of the activity.
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        return root;
    }

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
        HashMap<String, String> map = null;

        String[][] weekdayData = {
        		{"Midnight to 5 AM", "$1.25", "$3.25"},
        		{"5 AM to 6 AM", "$2.00", "$4.00"},
        		{"6 AM to 7 AM", "$3.40", "$5.40"},
        		{"7 AM to 9 AM", "$4.30", "$6.30"},
        		{"9 AM to 10 AM", "$3.40", "$5.40"},
        		{"10 AM to 2 PM", " $2.70", "$4.70"},
        		{"2 PM to 3 PM", "$3.40", "$5.40"},
        		{"3 PM to 6 PM", "$4.30", "$6.30"},
        		{"6 PM to 7 PM", "$3.40", "$5.40"},
        		{"7 PM to 9 PM", "$2.70", "$4.70"},
        		{"9 PM to 11 PM", "$2.00", "$4.00"},
        		{"11 PM to 11:59 PM", "$1.25", "$3.25"}
        		};

        String[][] weekendData = {
        		{"Midnight to 5 AM", "$1.25", "$3.25"},
        		{"5 AM to 8 AM", "$1.40", "$3.40"},
        		{"8 AM to 11 AM", "$2.05", "$4.05"},
        		{"11 AM to 6 PM", "$2.65", "$4.65"},
        		{"6 PM to 9 PM", "$2.05", "$4.05"},
        		{"9 PM to 11 PM", " $1.40", "$3.40"},
        		{"11 PM to 11:59 PM", "$1.25", "$3.25"}
        		};
        
        map = new HashMap<>();
        map.put("hours", "Monday to Friday");
        map.put("goodtogo_pass", "Good To Go! Pass");
        map.put("pay_by_mail", "Pay By Mail");
        mAdapter.addSeparatorItem(map);

        BuildAdapterData(weekdayData);
        
        map = new HashMap<>();
        map.put("hours", "Weekends and Holidays");
        map.put("goodtogo_pass", "Good To Go! Pass");
        map.put("pay_by_mail", "Pay By Mail");
        mAdapter.addSeparatorItem(map);
        
        BuildAdapterData(weekendData);		
	}

	private void BuildAdapterData(String[][] data) {
    	HashMap<String, String> map = null;
    	
        for (int i = 0; i < data.length; i++) {
        	map = new HashMap<>();
        	map.put("hours", data[i][0]);
            map.put("goodtogo_pass", data[i][1]);
            map.put("pay_by_mail", data[i][2]);
            mAdapter.addItem(map);
        }
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
        private ArrayList<HashMap<String, String>> mData = new ArrayList<>();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = null;

            switch (viewType) {
                case TYPE_ITEM:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.tollrates_sr520_row, parent, false);
                    return new ItemViewHolder(itemView);
                case TYPE_SEPARATOR:
                    itemView = LayoutInflater.
                            from(parent.getContext()).
                            inflate(R.layout.tollrates_sr520_header, parent, false);
                    return new TitleViewHolder(itemView);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewholder, int position) {

            ItemViewHolder itemholder;
            TitleViewHolder titleholder;

            HashMap<String, String> map = mData.get(position);

            if (getItemViewType(position) == TYPE_ITEM) {
                itemholder = (ItemViewHolder) viewholder;
                itemholder.hours.setText(map.get("hours"));
                itemholder.hours.setTypeface(tf);
                itemholder.goodToGoPass.setText(map.get("goodtogo_pass"));
                itemholder.goodToGoPass.setTypeface(tf);
                itemholder.payByMail.setText(map.get("pay_by_mail"));
                itemholder.payByMail.setTypeface(tf);

                if (position == getTollIndex()) {
                    itemholder.itemView.setBackgroundColor(getResources().getColor(R.color.primary_default));
                    itemholder.hours.setTextColor(getResources().getColor(R.color.white));
                    itemholder.goodToGoPass.setTextColor(getResources().getColor(R.color.white));
                    itemholder.payByMail.setTextColor(getResources().getColor(R.color.white));
                }

            } else {
                titleholder = (TitleViewHolder) viewholder;
                titleholder.hours.setText(map.get("hours"));
                titleholder.hours.setTypeface(tfb);
                titleholder.goodToGoPass.setText(map.get("goodtogo_pass"));
                titleholder.goodToGoPass.setTypeface(tfb);
                titleholder.payByMail.setText(map.get("pay_by_mail"));
                titleholder.payByMail.setTypeface(tfb);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return mSeparatorsSet.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
        }

        public void addItem(final HashMap<String, String> map) {
            mData.add(map);
            notifyDataSetChanged();
        }

        public void addSeparatorItem(final HashMap<String, String> item) {
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

    private static int getTollIndex() {

        if (Build.VERSION.SDK_INT > 25) {

            ZoneId z = ZoneId.of("America/Montreal");
            LocalDate today = LocalDate.now(z);
            DayOfWeek dow = today.getDayOfWeek();
            Set<DayOfWeek> weekend = EnumSet.of( DayOfWeek.SATURDAY , DayOfWeek.SUNDAY );

            Boolean isWeekend = weekend.contains(dow);

            Boolean isHoliday = is_WAC_468_270_071_Holiday(today);

            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

            if (isWeekend || isHoliday){
                switch (hour) {
                    case 0: case 1: case 2: case 4:
                        // midnight to 5 am
                        return 14;
                    case 5: case 6: case 7:
                        // 5 am to 8 am
                        return 15;
                    case 8: case 9: case 10:
                        // 8 am to 11 am
                        return 16;
                    case 11: case 12: case 13: case 14: case 15: case 16: case 17:
                        // 11 am to 6 pm
                        return 17;
                    case 18: case 19: case 20:
                        // 6 pm to 9 pm
                        return 18;
                    case 21: case 22:
                        // 9 pm to 11 pm
                        return 19;
                    case 23:
                        // "11 pm"
                        return 20;
                    default:
                        return -1;
                }
            } else {
                switch (hour) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                        // Midnight to 5 am
                        return 1;
                    case 5:
                        // 5am to 6am
                        return 2;
                    case 6:
                        // 6am to 7am
                        return 3;
                    case 7:
                    case 8:
                        // 7am to 9am
                        return 4;
                    case 9:
                        // 9am to 10am
                        return 5;
                    case 10:
                    case 11:
                    case 12:
                    case 13:
                        // 10am to 2pm
                        return 6;
                    case 14:
                        // 2pm to 3pm
                        return 7;
                    case 15:
                    case 16:
                    case 17:
                        // 3pm to 6pm
                        return 8;
                    case 18:
                        // 6pm to 7pm
                        return 9;
                    case 19:
                    case 20:
                        // 7pm to 9pm
                        return 10;
                    case 21:
                    case 22:
                        // 9pm to 11pm
                        return 11;
                    case 23:
                        // 11pm
                        return 12;
                    default:
                        return -1;
                }
            }
        } else {
            return -1;
        }
    }

    private static Boolean is_WAC_468_270_071_Holiday(LocalDate date) {

        if (Build.VERSION.SDK_INT > 25) {

            int weekday = date.getDayOfWeek().getValue();
            int day = date.getDayOfMonth();
            int month = date.getMonth().getValue();
            int week = date.getDayOfWeek().ordinal();

            Log.e(TAG, String.valueOf(month));
            Log.e(TAG, String.valueOf(week));
            Log.e(TAG, String.valueOf(weekday));
            Log.e(TAG, String.valueOf(day));

            if (day == 1 && month == 1) { // New Years
                return true;
            } else if (month == 5 && day == getMemorialDayForYear(date.getYear())) { // Memorial Day
                return true;
            } else if (day == 4 && month == 7) { // Independence Day
                return true;
            } else if (month == 9 && weekday == 2 && week == 1) {  // Labor Day - 1st Mon in Sept
                return true;
            } else if (month == 11 && weekday == 5 && week == 4) { // Thanksgiving = 4th Thurs in Nov
                return true;
            } else if (month == 12 && day == 25) { // Christmas day
                return true;
            }
        }

        return false;
    }

    // returns the date of memorial day for the given year
    private static int getMemorialDayForYear(int year) {

        if (Build.VERSION.SDK_INT > 25) {

            Calendar cal = Calendar.getInstance();

            // set calendar to current year based on date given
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, Calendar.MAY);

            LocalDate refDate =  LocalDateTime.ofInstant(cal.getTime().toInstant(), ZoneId.of("America/Montreal")).toLocalDate();

            LocalDate memorialDay = YearMonth.from(refDate)
                    .atEndOfMonth()                                             // Get the last day of month as a `LocalDate`.
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));  // Move to another date for a Monday, or remain if already on a Monday.

            // return the day of the month
            return memorialDay.getDayOfMonth();
        }
        return -1;
    }
}