package com.example.calendartest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.View;

/**
 * 
 * @author VijayK
 * 
 */
public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void clickCalled(View view) {
		addReminder();
	}

	private void addReminder() {

		final Handler mHandler = new Handler(Looper.getMainLooper());
		new Thread() {

			Uri eventsUri;
			Uri remainderUri;
			Cursor cursor;
			private int[] calendarId;

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {

				if (android.os.Build.VERSION.SDK_INT <= 7) {
					eventsUri = Uri.parse("content://calendar/events");
					remainderUri = Uri.parse("content://calendar/reminders");
					cursor = getContentResolver().query(Uri.parse("content://calendar/calendars"), 
							new String[] { "_id", "displayName" }, null, null, null);

				}

				else if (android.os.Build.VERSION.SDK_INT <= 14) {
					eventsUri = Uri.parse("content://com.android.calendar/events");
					remainderUri = Uri.parse("content://com.android.calendar/reminders");
					cursor = getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"), 
							new String[] { "_id", "displayName" }, null,
							null, null);

				}

				else {
					eventsUri = Uri.parse("content://com.android.calendar/events");
					remainderUri = Uri.parse("content://com.android.calendar/reminders");
					cursor = getContentResolver().query(Uri.parse("content://com.android.calendar/calendars"), 
							new String[] { "_id", "calendar_displayName" },
							null, null, null);

				}

				// Get calendars name
				String[] calendarNames = new String[cursor.getCount()];
				// Get calendars id
				calendarId = new int[cursor.getCount()];
				if (cursor != null && cursor.moveToFirst()) {
					for (int i = 0; i < calendarNames.length; i++) {
						calendarId[i] = cursor.getInt(0);
						calendarNames[i] = cursor.getString(1);
						cursor.moveToNext();
					}
				}

				cursor.close();
				mHandler.post(postOperationResult(calendarId[0], eventsUri));
			}
		}.start();

	}

	protected Runnable postOperationResult(final int calendarId, final Uri eventsUri) {

		Runnable runnable = new Runnable() {
			private Date startDate;
			private Date endDay;

			@Override
			public void run() {
				TimeZone timeZone = TimeZone.getDefault();

				ContentValues values = new ContentValues();
				values.put("calendar_id", calendarId);
				values.put("title", "Reminder Title");
				values.put("allDay", 0);

				try {
					/** HardCoded. can be fetched from view */
					startDate = new SimpleDateFormat("MM/dd/yyyy").parse("12/6/2013");
					values.put("dtstart", startDate.getTime());
					/** HardCoded. can be fetched from view */
					endDay = new SimpleDateFormat("MM/dd/yyyy").parse("12/7/2013");
					// values.put("dtend",endDay.getTime() );
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// count is used to
				int count = (int) ((endDay.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)) + 1;
				values.put("description", "Reminder description");
				values.put("visibility", 0);// default (0), confidential (1),
											// private (2), or public (3):
				values.put("hasAlarm", 1);// 0 for false, 1 for true
				values.put("eventTimezone", timeZone.getID());

				// "FREQ=DAILY" MONTLY,YEARLY,WEEKLY
				values.put("rrule", "FREQ=MONTHLY;COUNT=" + count);
				values.put("duration", "P1W");

				// To Insert
				getContentResolver().insert(eventsUri, values);
			}
		};
		return runnable;
	}

	/**
	 * Method to return getMonthsDifference. It also calculate day, week, year
	 * difference. Can be used as per the requirement
	 * 
	 * @param startDate
	 * @param endDate
	 * @return
	 */

	public static final int getMonthsDifference(Date startDate, Date endDate) {

		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		int year1 = cal.get(Calendar.YEAR);
		int month1 = cal.get(Calendar.MONTH);

		cal.setTime(endDate);
		int year2 = cal.get(Calendar.YEAR);
		int month2 = cal.get(Calendar.MONTH);

		// day difference
		int numberOfDay = (int) ((startDate.getTime() - endDate.getTime()) / (1000 * 60 * 60 * 24)) + 1;

		// month difference
		int numberOfMonth = (year2 * 12 + month2) - (year1 * 12 + month1);

		// year difference
		int numberOfYear = year2 - year1;

		// week difference
		int numberOfWeek = numberOfDay / 7;

		// return month difference
		return numberOfMonth;
	}

	/**
	 * Not in use. but can be useful to get calendar base uri
	 * 
	 * @param act
	 * @return
	 */
	private String getCalendarUriBase(Activity act) {

		String calendarUriBase = null;
		Uri calendars = Uri.parse("content://calendar/calendars");
		Cursor managedCursor = null;
		try {
			managedCursor = act.managedQuery(calendars, null, null, null, null);
		} catch (Exception e) {
		}
		if (managedCursor != null) {
			calendarUriBase = "content://calendar/";
		} else {
			calendars = Uri.parse("content://com.android.calendar/calendars");
			try {
				managedCursor = act.managedQuery(calendars, null, null, null, null);
			} catch (Exception e) {
			}
			if (managedCursor != null) {
				calendarUriBase = "content://com.android.calendar/";
			}
		}
		return calendarUriBase;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
