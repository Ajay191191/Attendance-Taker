package com.ajay;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
import android.widget.TextView;
import android.widget.Toast;

import com.pras.SpreadSheet;
import com.pras.SpreadSheetFactory;
import com.pras.WorkSheet;

/**
 * Show list of WorkSheets
 * 
 * @author Ajay
 * 
 */
public class GSSDetails extends Activity {

	ListView list;
	int spID = -1;
	ArrayList<WorkSheet> workSheets;
	TextView tv;
	ArrayAdapter<String> arrayAdaper;
	String title;
	private int position;
	SpreadSheetFactory factory;
	ArrayList<SpreadSheet> sps;
	SpreadSheet sp;
	private boolean delete=false;

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		spID = intent.getIntExtra("sp_id", -1);
		
		if (spID == -1) {
			finish();
			return;
		}

		list = new ListView(this.getApplicationContext());
		tv = new TextView(this.getApplicationContext());
		registerForContextMenu(list);
		new MyTask().execute();
	}

	@SuppressWarnings("rawtypes")
	private class MyTask extends AsyncTask {

		Dialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new Dialog(GSSDetails.this);
			dialog.setTitle("Please wait");
			TextView tv = new TextView(GSSDetails.this.getApplicationContext());
			tv.setText("Featching SpreadSheet details...");
			tv.setBackgroundColor(Color.WHITE);
			tv.setTextColor(Color.BLACK);
			dialog.setContentView(tv);
			dialog.show();
		}

		@Override
		protected Object doInBackground(Object... params) {
			// Read Spread Sheet list from the server.
			factory = SpreadSheetFactory.getInstance();
			// Read from local Cache
			sps = factory.getAllSpreadSheets(false);
			sp = sps.get(spID);
			workSheets = sp.getAllWorkSheets();
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (dialog.isShowing())
				dialog.cancel();
			list.setBackgroundColor(Color.WHITE);
			
			if (workSheets == null || workSheets.size() == 0 || sp.getAllWorkSheets(true, "Sheet 1", true)!=null) {

				tv.setText("No spreadsheet exists in your account...");
				if(sp.getAllWorkSheets(true, "Sheet 1", true)!=null){
					addSpreadSheet();
					delete=true;
				}
				
			} else {
				arrayAdaper = new ArrayAdapter<String>(
						GSSDetails.this.getApplicationContext(),
						R.layout.adapterview);
				for (int i = 0; i < workSheets.size(); i++) {
					WorkSheet wk = workSheets.get(i);
					arrayAdaper.add(wk.getTitle());
				}
				
				list.addHeaderView(tv);
				list.setAdapter(arrayAdaper);
				list.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> arg0,
							View arg1, int arg2, long arg3) {

						position = arg2 - 1;
						return false;
					}

				});
				tv.setText("Number of WorkSheets (" + workSheets.size() + ")");

				list.setOnItemClickListener(new OnItemClickListener() {

					public void onItemClick(AdapterView<?> adapterView,
							View view, int position, long id) {
						if (position == 0)
							return;

						Intent i = new Intent(GSSDetails.this, WKDetails.class);
						i.putExtra("wk_id", position - 1);
						i.putExtra("sp_id", spID);
						startActivity(i);
					}
				});
				setContentView(list);
			}
		}

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
						new deleteWorkSheet().execute(s);
					}
				});
			return (true);
		case R.string.menu_add:
			Toast.makeText(getApplicationContext(), "Deleted",
					Toast.LENGTH_LONG).show();
			addSpreadSheet();
			return (true);
		}
		return false;
	}

	public void addSpreadSheet() {
		Button noOfStudents = new Button(
				GSSDetails.this.getApplicationContext());
		LinearLayout lv = new LinearLayout(
				GSSDetails.this.getApplicationContext());
		final EditText et = new EditText(
				GSSDetails.this.getApplicationContext());
		final EditText name = new EditText(
				GSSDetails.this.getApplicationContext());
		noOfStudents.setText("Add");
		et.setInputType(InputType.TYPE_CLASS_NUMBER);
		lv.setOrientation(LinearLayout.VERTICAL);
		et.setHint("Enter Total No of Students");
		name.setHint("Enter Subject");
		lv.addView(et);
		lv.addView(name);
		lv.addView(noOfStudents);
		lv.setBackgroundColor(Color.WHITE);
		setContentView(lv);
		noOfStudents.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				final String s[] = new String[Integer.parseInt(et.getText()
						.toString()) + 1];
				s[0] = new String("Dates");

				for (int i = 1; i < s.length; i++){
					s[i] = new String("roll"+i);
				}
				runOnUiThread(new Runnable() {
					@SuppressWarnings("unchecked")
					public void run() {
						new addWorkSheet().execute(name.getText().toString(),s);
					}
				});
			}

		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(1, 1, 1, "add");
		return true;
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			addSpreadSheet();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class addWorkSheet extends AsyncTask{

		Dialog dialog;
		@Override
		protected Object doInBackground(Object... params) {
			String name = (String)params[0];
			String s[] = (String []) params[1];
			sp.addListWorkSheet(name, 50, s);
			if(delete){
				sp.deleteWorkSheet(sp.getAllWorkSheets(true, "Sheet 1", true).get(0));
			}
			return null;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new Dialog(GSSDetails.this);
			dialog.setTitle("Please wait");
			TextView tv = new TextView(GSSDetails.this.getApplicationContext());
			tv.setBackgroundColor(Color.WHITE);
			tv.setTextColor(Color.BLACK);
			tv.setText("Adding Subject");
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
	private class deleteWorkSheet extends AsyncTask{

		Dialog dialog;
		@Override
		protected Object doInBackground(Object... params) {
			String s = (String)params[0];
			final ArrayList<WorkSheet> wk = sp.getWorkSheet(s, true);
			if (wk.size() != 0) {
			sp.deleteWorkSheet(wk.get(0));
			}
			return null;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new Dialog(GSSDetails.this);
			dialog.setTitle("Please wait");
			TextView tv = new TextView(GSSDetails.this.getApplicationContext());
			tv.setText("Deleting Subject ");
			tv.setBackgroundColor(Color.WHITE);
			tv.setTextColor(Color.BLACK);
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
