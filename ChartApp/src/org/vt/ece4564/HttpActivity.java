package org.vt.ece4564;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class HttpActivity extends Activity {

	// private static final String TAG = "HttpActivity";
	// create GUI elements
	private Button getButton_, chartSetup_;
	private ImageButton sendChart_;
	private TextView dataLabel_, labelLabel_;
	private EditText initialWord_, chartData01_, chartData02_, chartData03_,
			chartData04_;
	private ProgressBar spinningCircle_;
	private ImageView imageViewer_;
	private Spinner chartSelect_;

	private String state = "bar";
	private Drawable chartImage_;

	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// connect GUI elements to variables
		// getText_ = (TextView) findViewById(R.id.TextView01);
		getButton_ = (Button) findViewById(R.id.createChart);
		chartSetup_ = (Button) findViewById(R.id.chartSetup);
		sendChart_ = (ImageButton) findViewById(R.id.chartSend);
		initialWord_ = (EditText) findViewById(R.id.EditText01);
		chartData01_ = (EditText) findViewById(R.id.chartData01);
		chartData02_ = (EditText) findViewById(R.id.chartData02);
		chartData03_ = (EditText) findViewById(R.id.chartData03);
		chartData04_ = (EditText) findViewById(R.id.chartData04);
		spinningCircle_ = (ProgressBar) findViewById(R.id.ProgressBar01);
		imageViewer_ = (ImageView) findViewById(R.id.ImageView01);
		chartSelect_ = (Spinner) findViewById(R.id.chartSelect);
		dataLabel_ = (TextView) findViewById(R.id.TextView01);
		labelLabel_ = (TextView) findViewById(R.id.TextView02);
		
		// create spinner
		ArrayAdapter<CharSequence> adapter = ArrayAdapter
				.createFromResource(this, R.array.chart_array,
						android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		chartSelect_.setAdapter(adapter);

		// stuff to invisible
		spinningCircle_.setVisibility(4);
		initialWord_.setVisibility(0);
		chartSetup_.setVisibility(4);
		sendChart_.setVisibility(4);
		chartData02_.setVisibility(4);
		chartData03_.setVisibility(4);

		// command for chart Button
		getButton_.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// set visibilities for chart display
				if (!state.equals("qrcode")) {
					chartSetup_.setVisibility(0);
					getButton_.setVisibility(4);
					chartSelect_.setVisibility(4);
					initialWord_.setVisibility(4);
				}
				chartData01_.setVisibility(4);
				chartData02_.setVisibility(4);
				chartData03_.setVisibility(4);
				chartData04_.setVisibility(4);
				dataLabel_.setVisibility(4);
				labelLabel_.setVisibility(4);

				spinningCircle_.setVisibility(0);

				// initialize url
				String url = null;

				// initialize data strings from input
				String data1 = chartData01_.getText().toString()
						.replace(' ', ',');
				String data2 = chartData02_.getText().toString()
						.replace(' ', ',');
				String data3 = chartData03_.getText().toString()
						.replace(' ', ',');
				String data4 = chartData04_.getText().toString()
						.replace(',', '|').replace(" ", "|");

				// initialize values arrays
				int[] valuesArray1 = new int[100];
				int[] valuesArray2 = new int[100];
				int[] valuesArray3 = new int[100];
				int max = 0;

				// normalize data between 0 and 100 for ChartAPI
				if (data1.equals("")) {
					data1 = "0";
				} else {
					valuesArray1 = enumerateData(data1);
					max = findMax(valuesArray1);
				}
				if (state == "line") {
					if (data2.equals("")) {
						data2 = "0";
					} else {
						valuesArray2 = enumerateData(data2);
						int temp = findMax(valuesArray2);
						if (temp > max) {
							max = temp;
						}
					}
					if (data3.equals("")) {
						data3 = "0";
					} else {
						valuesArray3 = enumerateData(data3);
						int temp = findMax(valuesArray3);
						if (temp > max) {
							max = temp;
						}
					}
				}
				data1 = normalizeData(valuesArray1, max);
				if (state == "line") {
					data2 = normalizeData(valuesArray2, max);
					data3 = normalizeData(valuesArray3, max);
				}
				
				// build URL to send information to Google
				// bar chart
				if (state.equals("bar")) {
					url = "http://chart.apis.google.com/chart?cht=bvs&chbh=a&chd=t:"
							+ data1
							// 200
							+ "&chs=640x400&chxt=x&chco=FDD017|4CC417|6698FF&chls=2.0&chxl=0:|"
							+ data4;
				// pie chart
				} else if (state.equals("pie")) {
					url = "http://chart.apis.google.com/chart?cht=p&chd=t:"
							+ data1
							// 145
							+ "&chs=640x290&chl=" + data4;
				// line chart
				} else if (state.equals("line")) {
					url = "http://chart.apis.google.com/chart?&cht=lc&chd=t:"
							+ data1 + "|" + data2 + "|" + data3
							// 192
							+ "&chs=640x384&chco=ff0000,00ff00,0000ff&chdl="
							+ data4;
				// qr code
				} else {
					String word = initialWord_.getText().toString()
							.replace(' ', '+');
					url = "http://chart.apis.google.com/chart?chs=500x500&cht=qr&chl="
							+ word + "&choe=UTF-8";
				}
				
				// get image from URL
				new HttpGetter().execute(url);

			}
		});
		// chart setup button
		chartSetup_.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// set visibilities for setup screen
				sendChart_.setVisibility(4);
				chartSetup_.setVisibility(4);
				getButton_.setVisibility(0);
				chartSelect_.setVisibility(0);
				imageViewer_.setVisibility(4);

				chartData01_.setVisibility(0);
				chartData02_.setVisibility(0);
				chartData03_.setVisibility(0);
				chartData04_.setVisibility(0);
				dataLabel_.setText("Data (comma separated)");
				dataLabel_.setVisibility(0);
				labelLabel_.setVisibility(0);
			}
		});
		// send chart button
		sendChart_.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				final String TAG = "sendChart";
				// create bitmap from drawable chart
				Bitmap bmp = ((BitmapDrawable) chartImage_).getBitmap();

				// create chart file at DCIM/chart.jpeg
				File imageFile = new File(
						Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
						"/chart.jpg");
				if (!imageFile.exists()) {
					try {
						imageFile.createNewFile();
					} catch (IOException e) {
						Log.d(TAG, e.toString());
					}
				}
				FileOutputStream fout = null;
				try {
					fout = new FileOutputStream(imageFile);
				} catch (FileNotFoundException e) {
					Log.d(TAG, e.toString());
				}
				// compress to a jpeg
				bmp.compress(CompressFormat.JPEG, 100, fout);

				try {
					fout.flush();
				} catch (IOException e) {
					Log.d(TAG, e.toString());
				}
				try {
					fout.close();
				} catch (IOException e) {
					Log.d(TAG, e.toString());
				}

				// send chart via intent ACTION_SEND
				Intent picMessageIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				picMessageIntent.setType("image/jpeg");

				File downloadedPic = new File(
						Environment
								.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
						"/chart.jpg");

				// create intent fields
				picMessageIntent.putExtra(Intent.EXTRA_STREAM,
						Uri.fromFile(downloadedPic));
				picMessageIntent.putExtra(Intent.EXTRA_SUBJECT,
						"My Chart");
				picMessageIntent.putExtra(Intent.EXTRA_TEXT,
						"\n\n--\nCreated using Chart Express");
				startActivity(Intent.createChooser(picMessageIntent,
						"Send this chart using:"));

			}
		});
		chartSelect_.setOnItemSelectedListener(new MyOnItemSelectedListener());
	}
	
	// method to convert input string to an array of integers
	public int[] enumerateData(String inputData) {
		String[] dataArray;

		dataArray = inputData.split(",");
		int[] valuesArray = new int[dataArray.length];

		for (int i = 0; i < dataArray.length; i++) {
			try {
				valuesArray[i] = Integer.parseInt(dataArray[i]);
			} catch (Exception e) {
				Log.d("parseInt", "Warning! Input formatting error likely!");
				dataLabel_.setText("Warning! Input formatting error likely!");
				dataLabel_.setVisibility(0);
			}
		}
		return valuesArray;
	}
	
	// method to find the maximum value of all input data
	public int findMax(int valuesArray[]) {
		int maxValue = valuesArray[0];

		for (int j = 1; j < valuesArray.length; j++) {
			if (valuesArray[j] > maxValue) {
				maxValue = valuesArray[j];
			}
		}
		return maxValue;
	}

	// scale the input data between 0 and 100 and output to string
	public String normalizeData(int valuesArray[], int maxValue) {
		String outputData = "";
		for (int k = 0; k < valuesArray.length; k++) {
			valuesArray[k] = (int) (((float) valuesArray[k]) * ((float) 100 / (float) maxValue));
		}
		for (int l = 0; l < valuesArray.length; l++) {
			outputData = outputData.concat("," + valuesArray[l]);
		}
		outputData = outputData.substring(1);
		return outputData;
	}

	// method to display the image
	// called by onPostExecute in HttpGetter
	public void interpretResult(Drawable result) {
		// progress spinner to invisible
		chartImage_ = result;
		imageViewer_.setImageDrawable(chartImage_);
		imageViewer_.setVisibility(0);
		spinningCircle_.setVisibility(4);
		sendChart_.setVisibility(0);

	}

	// code to control the spinner UI element
	// uses names in strings.xml and displays a Toast element on selection
	public class MyOnItemSelectedListener implements OnItemSelectedListener {

		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			Toast.makeText(parent.getContext(),
					parent.getItemAtPosition(pos).toString() + " Selected",
					Toast.LENGTH_LONG).show();

			chartData01_.setVisibility(0);
			chartData02_.setVisibility(0);
			chartData03_.setVisibility(0);
			chartData04_.setVisibility(0);
			dataLabel_.setVisibility(0);
			labelLabel_.setVisibility(0);
			sendChart_.setVisibility(4);

			if (parent.getItemAtPosition(pos).toString().equals("Bar Chart")) {
				state = "bar";
				initialWord_.setVisibility(4);
				chartData02_.setEnabled(false);
				chartData03_.setEnabled(false);
				imageViewer_.setVisibility(4);

			} else if (parent.getItemAtPosition(pos).toString()
					.equals("Pie Chart")) {
				state = "pie";
				initialWord_.setVisibility(4);
				chartData02_.setEnabled(false);
				chartData03_.setEnabled(false);
				imageViewer_.setVisibility(4);
			} else if (parent.getItemAtPosition(pos).toString()
					.equals("Line Chart")) {
				state = "line";
				initialWord_.setVisibility(4);
				chartData02_.setEnabled(true);
				chartData03_.setEnabled(true);
				imageViewer_.setVisibility(4);
			} else {
				state = "qrcode";
				initialWord_.setVisibility(0);
				dataLabel_.setVisibility(4);
				labelLabel_.setVisibility(4);
				chartData01_.setVisibility(4);
				chartData02_.setVisibility(4);
				chartData03_.setVisibility(4);
				chartData04_.setVisibility(4);
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	// HttpGetter class
	public class HttpGetter extends AsyncTask<String, Integer, Drawable> {
		private static final String TAG = "HttpGetter";

		// private Bitmap chart_;

		public InputStream getResource(String url) {
			// try to set up the HTTP client
			InputStream content = null;
			URL imageUrl = null;
			try {
				imageUrl = new URL(url);
			} catch (MalformedURLException e) {
				Log.d(TAG, e.toString());
			}
			try {
				// get the input stream making up the image
				HttpURLConnection conn = (HttpURLConnection) imageUrl
						.openConnection();
				conn.setDoInput(true);
				conn.connect();
				// int length = conn.getContentLength();
				content = conn.getInputStream();

			} catch (Exception e) {
				// print any exceptions to log
				Log.d(TAG, e.toString());
			}
			return content;
		}

		@Override
		protected Drawable doInBackground(String... params) {
			// run the getResource method in the background
			InputStream stream = null;
			// return a drawable from the remote image
			stream = getResource(params[0]);
			return Drawable.createFromStream(stream, "chart.png");
		}

		protected void onPostExecute(Drawable result) {
			// pass the resulting drawable to a method to be displayed
			interpretResult(result);

		}
	}

}
