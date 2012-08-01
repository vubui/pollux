package com.vietapp.diemthidh2012;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

public class MainActivity extends Activity {

	private static final String LINK = "http://timdiemthi.com/";
	private Spinner spinner;
	private EditText ten;
	private ImageButton tim;
	private WebView webview;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tim = (ImageButton) findViewById(R.id.tim);
		spinner = (Spinner) findViewById(R.id.chon_truong);
		ten = (EditText) findViewById(R.id.ten_sbd);
		webview = (WebView) findViewById(R.id.webView1);

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			new fillSpinnerTask().execute();
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Bạn cần kết nối tới Internet.")
					.setCancelable(false)
					.setPositiveButton("Thử Lại",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									onRestart();
								}
							})
					.setNegativeButton("Thôi!",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									finish();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		}

		/*
		 * Set event handler for "tim" button
		 */
		tim.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				new fillWebview().execute();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public Document JSoupConnect(String url) {
		Document doc = null;
		try {
			doc = Jsoup.connect(url).get();
		} catch (IOException e) {
			e.printStackTrace();
			/*
			 * MainActivity.this.runOnUiThread(new Runnable() {
			 * 
			 * public void run() { Toast t =
			 * Toast.makeText(getApplicationContext(),
			 * "Kiểm Tra Lại Kết Nối Tới Internet!", Toast.LENGTH_SHORT);
			 * t.show(); } });
			 */
		}
		return doc;
	}

	private class fillSpinnerTask extends
			AsyncTask<Void, Void, ArrayAdapter<String>> {

		ProgressDialog dialog = new ProgressDialog(MainActivity.this);

		protected void onPreExecute() {

			dialog.setCancelable(true);
			dialog.setMessage("Đang tải dữ liệu ...");
			dialog.show();

		}

		@Override
		protected ArrayAdapter<String> doInBackground(Void... voids) {

			Document doc = JSoupConnect(LINK);

			Elements tags = doc.select("option");

			ArrayList<String> ten_truong = new ArrayList<String>();

			for (int i = 0; i < tags.size(); i++) {
				ten_truong.add(tags.eq(i).text());
			}

			/*
			 * Populate spinner with the school names
			 */
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getApplicationContext(),
					android.R.layout.simple_spinner_item, ten_truong);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			/*
			 * Update the number of available schools
			 */
			String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
					+ "<html><head>"
					+ "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />"
					+ "<head><body><h2 style=\"color:#33B5E5;font-size:15px;\">";
			content += "Đã cập nhật điểm của <span style=\"color:red;font-size:15px;\">"
					+ Integer.toString(tags.size())
					+ "</span> trường Đại học và Cao đẳng năm 2012!</h2></body></html>";
			webview.loadData(content, "text/html; charset=utf-8", "UTF-8");

			return adapter;
		}

		protected void onPostExecute(ArrayAdapter<String> adapter) {
			spinner.setAdapter(adapter);
			dialog.dismiss();

		}
	}

	private class fillWebview extends AsyncTask<Void, Void, String> {

		ProgressDialog dialog = new ProgressDialog(MainActivity.this);

		protected void onPreExecute() {

			dialog.setCancelable(true);
			dialog.setMessage("Đang tải dữ liệu ...");
			dialog.show();

		}

		@Override
		protected String doInBackground(Void... arg0) {

			String content = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"
					+ "<html><head>"
					+ "<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />"
					+ "<style type=\"text/css\">.tdHoten{width:180px;text-align:left;}.tdMon{text-align:left;width:40px;}.tdDiemTong{text-align:center;width:70px;}.td50{text-align:center;width:50px;}</style>"
					+ "<head><body>";

			/*
			 * Get the school code
			 */
			Document doc = JSoupConnect(LINK);
			Elements tags = doc.select("option");

			Map<String, String> map = new HashMap<String, String>();

			for (int i = 0; i < tags.size(); i++) {
				map.put(tags.eq(i).text(), tags.eq(i).attr("value"));
			}

			String ten_truong = spinner.getSelectedItem().toString();

			if (ten_truong != null) {
				String ma_truong = map.get(ten_truong);

				String ho_ten = ten.getText().toString();

				String url = "http://timdiemthi.com/search.aspx?u=" + ma_truong
						+ "&s=" + Uri.encode(ho_ten) + "&y=2012&t=daihoc";

				Document webview_doc = JSoupConnect(url);

				String kq = webview_doc.select("span.note-text").first()
						.toString();

				content += "<p>" + kq + ".</p>";

				Elements td50 = webview_doc.select("td.td50");
				Elements tdHoten = webview_doc.select("td.tdHoten");
				Elements tdMon = webview_doc.select("td.tdMon");
				Elements tdDiemtong = webview_doc.select("td.tdDiemtong");

				int a = 0;
				ArrayList<String> td50_sbd = new ArrayList<String>();
				ArrayList<String> td50_khoi = new ArrayList<String>();
				ArrayList<String> td50_kv = new ArrayList<String>();
				while (a < td50.size()) {
					td50_sbd.add(td50.eq(a).text());
					td50_khoi.add(td50.eq(a + 1).text());
					td50_kv.add(td50.eq(a + 2).text());
					a += 4;
				}

				int b = 0;
				ArrayList<String> tdMon_1 = new ArrayList<String>();
				ArrayList<String> tdMon_2 = new ArrayList<String>();
				ArrayList<String> tdMon_3 = new ArrayList<String>();
				while (b < tdMon.size()) {
					tdMon_1.add(tdMon.eq(b).text());
					tdMon_2.add(tdMon.eq(b + 1).text());
					tdMon_3.add(tdMon.eq(b + 2).text());
					b += 3;
				}

				int c = 0;
				ArrayList<String> tdDiemtong_1 = new ArrayList<String>();
				ArrayList<String> tdDiemtong_2 = new ArrayList<String>();
				while (c < tdDiemtong.size()) {
					tdDiemtong_1.add(tdDiemtong.eq(c).text());
					tdDiemtong_2.add(tdDiemtong.eq(c + 1).text());
					c += 2;
				}

				content += "<table border='1' bordercolor='#33B5E5'>";
				for (int i = 0; i < tdHoten.size(); i++) {
					content += "<tr><td class='td50'>" + td50_sbd.get(i)
							+ "</td><td class='tdHoten'>"
							+ tdHoten.eq(i).text() + "</td><td class='td50'>"
							+ td50_khoi.get(i) + "</td><td class='tdMon'>"
							+ tdMon_1.get(i) + "</td><td class='tdMon'>"
							+ tdMon_2.get(i) + "</td><td class='tdMon'>"
							+ tdMon_3.get(i) + "</td><td class='tdDiemTong'>"
							+ tdDiemtong_1.get(i) + "</td><td class='td50'>"
							+ td50_kv.get(i) + "</td><td class='tdDiemTong'>"
							+ tdDiemtong_2.get(i) + "</td></tr>";
				}

			} else {
				content += "Bạn muốn tra điểm trường nào?";
			}

			content += "</table></body></head>";

			return content;
		}

		protected void onPostExecute(String content) {
			webview.loadData(content, "text/html; charset=utf-8", "UTF-8");
			dialog.dismiss();
		}
	}

	protected void onPause() {
		super.onPause();
	}

	protected void onStop() {
		super.onStop();

	}

	protected void onRestart() {
		super.onRestart();
	}

	protected void onResume() {
		super.onResume();
	}
}