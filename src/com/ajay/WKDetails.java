package com.ajay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.pras.SpreadSheet;
import com.pras.SpreadSheetFactory;
import com.pras.WorkSheet;
import com.pras.WorkSheetCell;
import com.pras.WorkSheetRow;
import com.pras.conn.HttpConHandler;


/**
 * @author Ajay
 * 
*/
public class WKDetails extends Activity {

	int wkID;
	int spID;
	int row_id;
	private boolean goBack = false;
	ArrayList<WorkSheetRow> rows;
	String[] cols;
	TextView tv;
	TableLayout table;
	LinearLayout ln;
	ScrollView sv;
	HorizontalScrollView hsv;
	GridView list;
	GridView attendance;
	@SuppressWarnings("rawtypes")
	ArrayAdapter dates;
	Map<ArrayAdapter<String>, String> dateToAttendance;
	ArrayAdapter<String> rollToAttendance;
	Button newDate;
	Map<String, String> colNameToRealName;
	boolean noRecords = false;
	SpreadSheet sp;
	WorkSheet wk;
	Button calculateAttendance;
	
	private class myCompare implements Comparator<Object> {

		@Override
		public int compare(Object lhs, Object rhs) {
			Integer i1=Integer.parseInt((String)lhs);
			Integer i2=Integer.parseInt((String)rhs);
			
			return -i2.compareTo(i1);
		}
		
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			row_id = 0;
			Intent intent = getIntent();
			wkID = intent.getIntExtra("wk_id", -1);
			spID = intent.getIntExtra("sp_id", -1);

			if (wkID == -1 || spID == -1) {
				finish();
				return;
			}
			tv = new TextView(this.getApplicationContext());
			ln = new LinearLayout(this.getApplicationContext());
			sv = new ScrollView(this.getApplicationContext());
			list = new GridView(this.getApplicationContext());
			attendance = new GridView(this.getApplicationContext());
			hsv = new HorizontalScrollView(this.getApplicationContext());
			dates = new ArrayAdapter(this.getApplicationContext(),
					R.layout.adapterview);
			dateToAttendance = new HashMap<ArrayAdapter<String>, String>();
			newDate = new Button(this.getApplicationContext());
			colNameToRealName = new TreeMap<String, String>(new myCompare());
			attendance.setBackgroundColor(Color.WHITE);
			list.setBackgroundColor(Color.WHITE);
			calculateAttendance = new Button(this.getApplicationContext());
		} catch (Exception e) {
			Log.i("Ajay", e.toString());
		}
		new MyTask().execute();
	}

	@SuppressWarnings("rawtypes")
	private class MyTask extends AsyncTask {

		Dialog dialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new Dialog(WKDetails.this);
			dialog.setTitle("Please wait");
			TextView tv = new TextView(WKDetails.this.getApplicationContext());
			tv.setText("Featching records...");
			tv.setBackgroundColor(Color.WHITE);
			tv.setTextColor(Color.BLACK);
			dialog.setContentView(tv);
			dialog.show();
		}

		@Override
		protected Object doInBackground(Object... params) {
			SpreadSheetFactory factory = SpreadSheetFactory.getInstance();
			ArrayList<SpreadSheet> sps = factory.getAllSpreadSheets(false);
			sp = sps.get(spID);
			wk = sp.getAllWorkSheets(false).get(wkID);

			cols = wk.getColumns();
			rows = wk.getData(false);

			return null;
		}

		@SuppressWarnings({ "unchecked", "unused" })
		@Override
		protected void onPostExecute(Object result) {
			super.onPostExecute(result);
			if (dialog.isShowing())
				dialog.cancel();
			
			ArrayAdapter arrayAdaper = new ArrayAdapter(
					WKDetails.this.getApplicationContext(),
					R.layout.adapterview);

			if (cols == null) {
				tv.setText("No Cols");
				Button noOfStudents = new Button(
						WKDetails.this.getApplicationContext());
				LinearLayout lv = new LinearLayout(
						WKDetails.this.getApplicationContext());
				final EditText et = new EditText(
						WKDetails.this.getApplicationContext());
				noOfStudents.setText("Add");
				et.setHint("Enter total Roll nos");
				lv.setOrientation(LinearLayout.VERTICAL);
				lv.addView(et);
				lv.addView(noOfStudents);
				setContentView(lv);
				noOfStudents.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						String s[] = new String[Integer.parseInt(et.getText()
								.toString()) + 1];
						s[0] = new String("Dates");
						for (int i = 1; i < s.length; i++)
							s[i] = new String(i + "roll");
						sp.addListWorkSheet("Attendance", 50, s);

						finish();
						Intent act = new Intent(WKDetails.this,
								GSSDetails.class);
						startActivity(act);
					}
				});
				return;

			}
			ln.setOrientation(LinearLayout.VERTICAL);
			/*if (rows.size() != 0) {

				{
					WorkSheetRow row = rows.get(0);
					ArrayList<WorkSheetCell> cells = row.getCells();
					int j = 0;
					for (String s : cols) {
						WorkSheetCell cell = cells.get(j);
						colNameToRealName.put(s, cell.getName());
						j++;
						if (j == cells.size())
							break;
					}
					Iterator it = colNameToRealName.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry m = (Entry) it.next();

					}

				}
			}*/// else 
			{
				for (String s : cols) {
					if(!s.equalsIgnoreCase("dates"))
					colNameToRealName.put(s.split("(?<=[\\w&&\\D])(?=\\d)")[1], s.split("(?<=[\\w&&\\D])(?=\\d)")[1]);
				}
			}
			if (!(rows == null || rows.size() == 0)) {

				tv.setText("Tables" + "\n");

				tv.append("\n");
				for (int i = 0; i < rows.size(); i++) {
					WorkSheetRow row = rows.get(i);

					ArrayList<WorkSheetCell> cells = row.getCells();

					for (int j = 0; j < cells.size(); j++) {
						WorkSheetCell cell = cells.get(j);
						if (j == 0){
							dates.add(cell.getValue());
							continue;
						}
						tv.append(cell.getValue() + "\t");

						for (Map.Entry e : colNameToRealName.entrySet()) {
							if (cell.getName().equals("roll"+e.getValue()))
								arrayAdaper.add("Roll : " + e.getKey()
										+ " Attendance :" + cell.getValue()
										+ "\t");
						}
					}
					tv.append("\n");
				}
				list.setAdapter(dates);

			}
			list.setOnItemClickListener(new OnItemClickListener() {

				public void onItemClick(AdapterView<?> adapterView, View view,
						int position, long id) {
					String absentNos = new String("Absent Nos: ");
					rollToAttendance = new ArrayAdapter<String>(WKDetails.this
							.getApplicationContext(),
							R.layout.adapterview);

					WorkSheetRow row = rows.get(position);

					ArrayList<WorkSheetCell> cells = row.getCells();

					for (int j = 1; j < cells.size(); j++) {
						WorkSheetCell cell = cells.get(j);
						for (Map.Entry e : colNameToRealName.entrySet()) {
							if (cell.getName().equals("roll"+e.getValue())) {
								rollToAttendance.add("Roll : " + e.getKey()
										+ " Attendance :" + cell.getValue()
										+ "\t");
								if (cell.getValue().equals("a")
										|| cell.getValue().equals("A"))
									absentNos += e.getKey() + ",";
							}
						}
						tv.append("\n");

					}
					rollToAttendance.add(absentNos);
					attendance.setAdapter(rollToAttendance);
					setContentView(attendance);
					goBack = true;
				}
			});

			newDate.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					LinearLayout ll = new LinearLayout(WKDetails.this
							.getApplicationContext());
					final ScrollView sv = new ScrollView(WKDetails.this
							.getApplicationContext());
					final TableLayout tl = new TableLayout(WKDetails.this
							.getApplicationContext());
					tl.setPadding(10,0, 0, 0);
					TableRow tr[] = new TableRow[cols.length + 2];
					final CheckBox cb[] = new CheckBox[cols.length - 1];
					TextView t[] = new TextView[cols.length - 1];
					Button submit = new Button(WKDetails.this
							.getApplicationContext());
					submit.setText("Submit");
					for (int cnt = 0; cnt < cols.length - 1; cnt++) {
						cb[cnt] = new CheckBox(WKDetails.this
								.getApplicationContext());
						cb[cnt].setTextColor(Color.BLACK);
						t[cnt] = new TextView(WKDetails.this
								.getApplicationContext());
						tr[cnt] = new TableRow(WKDetails.this
								.getApplicationContext());
						tr[cnt].setPadding(0, 10, 0, 10);
						t[cnt].setText((cnt + 1) + "");
						t[cnt].setTextSize(20);
						t[cnt].setTextColor(Color.BLACK);
						tr[cnt].addView(t[cnt]);

						tr[cnt].addView(cb[cnt]);
						tl.addView(tr[cnt]);
					}
					tr[cols.length - 1] = new TableRow(WKDetails.this
							.getApplicationContext());
					tr[cols.length - 1].addView(submit);
					tl.addView(tr[cols.length - 1]);
					submit.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
					
							Calendar c = Calendar.getInstance();
							SimpleDateFormat formatter = new SimpleDateFormat(
									"dd/MM/yyyy");
							String todaysDate = formatter.format(c.getTime());
							String absentNos = new String("");
							final HashMap<String, String> newDates = new HashMap<String, String>();
							Iterator it = colNameToRealName.entrySet()
									.iterator();
							int count = 0;
							newDates.put("dates", todaysDate);
							while (it.hasNext()) {
								Map.Entry m = (java.util.Map.Entry) it.next();
								//Log.i("Ajaysd",m.getValue().toString());
								
									if (cb[count++].isChecked())
										newDates.put("roll"+(String) m.getValue(), "P");
									else{
										newDates.put("roll"+(String) m.getValue(), "A");
										absentNos += (m.getValue()+"")+",";
								
								}
					}
							Log.i("Ajay",absentNos);
							AlertDialog.Builder alertD = new AlertDialog.Builder(WKDetails.this)
							.setTitle("Absent")
							.setMessage(absentNos);
							alertD.setNegativeButton(
									"Change",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
												setContentView(sv);
										}
									});
							alertD.setPositiveButton(
									"Submit",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											runOnUiThread(new Runnable() {
												@SuppressWarnings("unchecked")
												public void run() {
													new submitAttendance().execute(newDates);
												}
											});
										}
									});
							alertD.show();
						}
					});
					sv.addView(tl);
					tl.setBackgroundColor(Color.WHITE);

					setContentView(sv);
					goBack = true;

					// wk.addRecord("tOFuET0X2aYtGCBSRrqDfQg", newDates);
				}
			});
			calculateAttendance.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
						final EditText roll = new EditText(WKDetails.this.getApplicationContext());
						Button Submit =  new Button(WKDetails.this.getApplicationContext());
						LinearLayout lv = new LinearLayout(WKDetails.this.getApplicationContext());
						lv.addView(Submit);
						lv.addView(roll);
						setContentView(lv);
						lv.setOrientation(LinearLayout.VERTICAL);
						Submit.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								AlertDialog.Builder alertD = new AlertDialog.Builder(WKDetails.this)
								.setTitle("Absent");
								ArrayList < WorkSheetRow>w = wk.getData(false, false,HttpConHandler.encode("select roll5 where roll5")+"="+HttpConHandler.encode("P"), null);
								Log.i("Ajay",w.get(0).toString());
								//alertD.setMessage(wk.getData(false, false, "select%20COUNT(roll"+roll.getText().toString()+")%20where%20roll"+roll.getText().toString()+"=A", null).get(0).toString());
								alertD.setNeutralButton("OK", new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int which) {
											setContentView(ln);
									}
								});
							}
						});
				}
			});
			newDate.setText("Add Todays Date");
			
			goBack = false;
			ln.addView(list);
			ln.addView(newDate);
			ln.addView(calculateAttendance);
			ln.setBackgroundColor(Color.WHITE);
			setContentView(ln);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (goBack) {
				list.setAdapter(dates);
				goBack = false;
				setContentView(ln);
			} else
				super.onBackPressed();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	private class submitAttendance extends AsyncTask{

		Dialog dialog;
		@Override
		protected Object doInBackground(Object... params) {
			HashMap <String,String> newDates= (HashMap<String, String>)params[0];
			SpreadSheetFactory factory = SpreadSheetFactory
			.getInstance();
			ArrayList<SpreadSheet> sps = factory
			.getAllSpreadSheets(false);
			SpreadSheet sp = sps.get(spID);
			final WorkSheet wk = sp.getAllWorkSheets(false).get(wkID);
			wk.addListRow(newDates);
			return null;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new Dialog(WKDetails.this);
			dialog.setTitle("Please wait");
			TextView tv = new TextView(WKDetails.this.getApplicationContext());
			tv.setText("Adding Spreadsheet to account");
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
