package org.vt.ece4564;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HttpActivity extends Activity {

	// private static final String TAG = "HttpActivity";
	// create GUI elements
	private Button getButton_;
	private TextView getText_;
	private EditText initialWord_;
	private ProgressBar spinningCircle_;
	private CheckBox onlyCandidates_;

	// Called when the activity is first created.
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// connect GUI elements to variables
		getText_ = (TextView) findViewById(R.id.TextView01);
		getButton_ = (Button) findViewById(R.id.Button01);
		initialWord_ = (EditText) findViewById(R.id.EditText01);
		spinningCircle_ = (ProgressBar) findViewById(R.id.ProgressBar01);
		onlyCandidates_ = (CheckBox) findViewById(R.id.CheckBox01);

		// progress spinner to invisible
		spinningCircle_.setVisibility(4);

		// command for "Anagram it!" Button
		getButton_.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				// check for invalid lengths in input text
				if (initialWord_.length() > 30) {
					getText_.setText("Max input length (30) exceeded!");
				} else if (initialWord_.length() == 0) {
					getText_.setText("Input a word or phrase first!");
				} else {
					// code to only show candidate words
					String candidatesFlag = "&l=n";
					if (onlyCandidates_.isChecked()) {
						candidatesFlag = "&l=y";
					}
					// progress spinner to visible
					spinningCircle_.setVisibility(0);
					// get the initial phrase and change spaces to pluses
					String word = initialWord_.getText().toString()
							.replace(' ', '+');
					// build the URL from the words and the candidates only flag
					String url = "http://wordsmith.org/anagram/anagram.cgi?anagram="
							+ word + candidatesFlag + "&t=5000&a=n";
					// get the information from the specified URL in the
					// background
					new HttpGetter().execute(url);
				}
			}
		});
	}

	// method to scrape the anagrams from the HTTP response
	// called by onPostExecute in HttpGetter
	public void interpretResult(String text) {
		// progress spinner to invisible
		spinningCircle_.setVisibility(4);
		// split text until anagrams are remaining
		String[] splitText = text.split(" found");
		splitText = splitText[1].split("</b><br>");
		splitText = splitText[1].split("<!-- AdSpeed.com Serving Code");
		// change text view to final anagrams after some cleanup of string
		getText_.setText(splitText[0].replace("<br>", "").trim());
	}

	public class HttpGetter extends AsyncTask<String, Integer, Long> {
		private static final String TAG = "HttpGetter";
		private String httpResult_;

		private void getResource(String url) {
			// try to set up the HTTP client
			try {
				// connect to specified URL
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet(url);
				HttpResponse response = client.execute(request);
				// create input stream to store received data
				InputStream in = response.getEntity().getContent();
				// build a string containing the received data
				String line = null;
				StringBuilder sb = new StringBuilder();
				try {
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(in));
					while ((line = reader.readLine()) != null) {
						sb.append(line + "\n");
					}
				} catch (IOException e) {
					// print any exceptions to log
					Log.d(TAG, e.toString());
				}
				// store result to httpResult_
				httpResult_ = sb.toString();

			} catch (Exception e) {
				// print any exceptions to log
				Log.d(TAG, e.toString());
			}
		}

		@Override
		protected Long doInBackground(String... params) {
			// run the getResource method in the background
			getResource(params[0]);
			return new Long(0);
		}

		protected void onPostExecute(Long result) {
			// pass the resulting string to a method to be parsed
			interpretResult(httpResult_);
		}
	}

}
