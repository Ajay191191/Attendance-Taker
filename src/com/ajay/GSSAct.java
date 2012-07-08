package com.ajay;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ajay.auth.AndroidAuthenticator;
import com.pras.SpreadSheet;
import com.pras.SpreadSheetFactory;


/**
 * @author Ajay
 * 
 */
public class GSSAct extends Activity {

	ArrayList<SpreadSheet> spreadSheets;
	TextView tv;
	ListView list;
	SpreadSheetFactory factory;
	EditText spreadsheetName;
	LinearLayout ll;
	boolean back = false;
	Button createNewSpreadsheet;
	int position;
	ArrayAdapter<String> arrayAdaper;
	ProgressDialog progress;
	Handler progressHandler ;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		list = new ListView(this.getApplicationContext());
		tv = new TextView(this.getApplicationContext());
		tv.setBackgroundColor(Color.WHITE);
		tv.setTextColor(Color.BLACK);
		registerForContextMenu(list);
		list.setBackgroundColor(Color.WHITE);
		if (haveNetworkConnection()) {
			runOnUiThread(new Runnable() {
				@SuppressWarnings("unchecked")
				public void run() {
					new MyTask().execute();
				}
			});
		} else {
			TextView t = new TextView(this.getApplicationContext());
			t.setTextSize(20);
			t.setText("No internet Connection");
			setContentView(t);
		}
	}

	public void createSpreadsheet() {
		ll = new LinearLayout(this.getApplicationContext());
		ll.setBackgroundColor(Color.WHITE);
		createNewSpreadsheet = new Button(this.getApplicationContext());
		ll.setOrientation(android.widget.LinearLayout.VERTICAL);
		spreadsheetName = new EditText(this.getApplicationContext());
		tv.setText("No spreadsheet exists in your account...");
		createNewSpreadsheet.setText("Add Class");
		// ll.addView(tv);
		ll.addView(createNewSpreadsheet);

		spreadsheetName.setHint("Enter Year");
		ll.addView(spreadsheetName);
		setContentView(ll);
		createNewSpreadsheet.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				runOnUiThread(new Runnable() {
					@SuppressWarnings("unchecked")
					public void run() {
						new addSpreadsheets().execute();
					}
				});
			}
		});
	}

	@SuppressWarnings("rawtypes")
	private class MyTask extends AsyncTask {

		Dialog dialog;
		private final int progr[]  = {30, 15, 20, 25, 20};
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new Dialog(GSSAct.this);
			dialog.setTitle("Please wait");
			TextView tv = new TextView(GSSAct.this.getApplicationContext());
			tv.setText("Featching SpreadSheet list from your account...");
			dialog.setContentView(tv);
			dialog.show();
		}

		@Override
		protected Object doInBackground(Object... params) {
			factory = SpreadSheetFactory.getInstance(new AndroidAuthenticator(
					GSSAct.this));
			spreadSheets = factory.getAllSpreadSheets(true, "", false);

			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (dialog.isShowing())
				dialog.cancel();

			if (spreadSheets == null || spreadSheets.size() == 0) {
				GSSAct.this.createSpreadsheet();
			} else {
				arrayAdaper = new ArrayAdapter<String>(
						GSSAct.this.getApplicationContext(),
						R.layout.adapterview);
				for (int i = 0; i < spreadSheets.size(); i++) {
					SpreadSheet sp = spreadSheets.get(i);
					arrayAdaper.add(sp.getTitle());
				}

				list.addHeaderView(tv);
				list.setAdapter(arrayAdaper);
				
				tv.setText("Number of SpreadSheets (" + spreadSheets.size()
						+ ")");

				list.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {
						position=arg2-1;
						return false;
					}
				});
				list.setOnItemClickListener(new OnItemClickListener() {

					public void onItemClick(AdapterView<?> adapterView,
							View view, int position, long id) {
						// Show details of the SpreadSheet
						if (position == 0)
							return;

						// Start SP Details Activity
						Intent i = new Intent(GSSAct.this, GSSDetails.class);
						i.putExtra("sp_id", position - 1);
						startActivity(i);

					}
				});
				setContentView(list);
			}
		}

	}

	private boolean haveNetworkConnection() {
		boolean haveConnectedWifi = false;
		boolean haveConnectedMobile = false;

		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
		for (NetworkInfo ni : netInfo) {
			if (ni.getTypeName().equalsIgnoreCase("WIFI"))
				if (ni.isConnected())
					haveConnectedWifi = true;
			if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
				if (ni.isConnected())
					haveConnectedMobile = true;
		}
		return haveConnectedWifi || haveConnectedMobile;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.add("Add");
		menu.add(1, 1, 1, "add");
		menu.add(1, 2, 2, "delete");
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Toast.makeText(this.getApplicationContext(), "Clicked",
		// Toast.LENGTH_SHORT);
		switch (item.getItemId()) {
		case 1:
			back = true;
			createSpreadsheet();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (back) {
				back = false;
				setContentView(list);
			} else
				super.onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, Menu.FIRST, 0, R.string.menu_delete);
		menu.add(0, R.string.menu_add, 0, R.string.menu_add);
		menu.setHeaderTitle("Select what you wanna do");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return (applyMenuChoice(item) || super.onContextItemSelected(item));
	}

	private boolean applyMenuChoice(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST:
			
			final String s = arrayAdaper.getItem(position);
			runOnUiThread(new Runnable() {
				@SuppressWarnings("unchecked")
				public void run() {
					new deleteSpreadsheets().execute(s);
				}
			});
			return (true);
		case R.string.menu_add:
			back = true;
			createSpreadsheet();
			return (true);
		}
		return false;
	}
	
	private class addSpreadsheets extends AsyncTask{

		Dialog dialog;
		@Override
		protected Object doInBackground(Object... params) {
			factory.createSpreadSheet(spreadsheetName.getText().toString());
			return null;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new Dialog(GSSAct.this);
			dialog.setTitle("Please wait");
			TextView tv = new TextView(GSSAct.this.getApplicationContext());
			tv.setText("Adding Class to account");
			dialog.setContentView(tv);
			dialog.show();
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (dialog.isShowing())
				dialog.cancel();
			finish();
			startActivity(getIntent());
		}
		
	}
	private class deleteSpreadsheets extends AsyncTask{

		Dialog dialog;
		@Override
		protected Object doInBackground(Object... params) {
			String s=(String)params[0];
			factory.deleteSpreadSheet(factory.getAllSpreadSheets(true, s, true).get(0).getKey());
			return null;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new Dialog(GSSAct.this);
			dialog.setTitle("Please wait");
			TextView tv = new TextView(GSSAct.this.getApplicationContext());
			tv.setText("Deleting Class from account");
			dialog.setContentView(tv);
			dialog.show();
		}
		
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (dialog.isShowing())
				dialog.cancel();
			finish();
			startActivity(getIntent());
		}
		
	}
}