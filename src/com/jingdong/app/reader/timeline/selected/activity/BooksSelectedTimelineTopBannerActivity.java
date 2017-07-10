package com.jingdong.app.reader.timeline.selected.activity;

import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.common.MZReadCommonFragmentActivity;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.parser.BaseParserCreator;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment;
import com.jingdong.app.reader.timeline.selected.parser.BooksTopBannerJsonParser;
import com.jingdong.app.reader.timeline.selected.parser.BooksTopBannerUrlParser;
import com.jingdong.app.reader.ui.ActionBarHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BooksSelectedTimelineTopBannerActivity extends
		MZReadCommonFragmentActivity {

	public final static String TOP_KEY = "top_key_id";
	private int top_banner_id = 0;

	private ImageView imageView;
	private TextView textSameView;


	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_books_selected_top_banner);
		imageView = (ImageView) findViewById(R.id.selected_title_image);
		textSameView = (TextView) findViewById(R.id.title_same);
		Intent itIntent = getIntent();
		top_banner_id = itIntent.getIntExtra(TOP_KEY, 0);
		RequestTobBannerData task = new RequestTobBannerData();
		task.execute(top_banner_id);


	}

	class RequestTobBannerData extends AsyncTask<Integer, Void, String> {

		@Override
		protected String doInBackground(Integer... params) {

			if (top_banner_id == 0)
				return "error";
			String data = WebRequest.getWebDataWithContext(
					BooksSelectedTimelineTopBannerActivity.this,
					URLText.getBooksSelectedTopTimeline + top_banner_id
							+ ".json");

			return data;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			try {
				JSONObject object = new JSONObject(result);
				JSONObject bobject = object.getJSONObject("banner");
				String urlString = bobject.optString("image");
				String titleString = bobject.optString("banner_title");
				String urlPath = urlString
						+ GlobalVarable.BOOKS_SELECTED_BANNER_IMAGE_SIZE_2X;

				 ImageLoader.getInstance().displayImage(urlPath,imageView, GlobalVarable.getDefaultBookDisplayOptions());
				ActionBarHelper.customActionBarBack(BooksSelectedTimelineTopBannerActivity.this,titleString);
				textSameView.setText(titleString);
				textSameView.getPaint().setFakeBoldText(true);
			} catch (Exception e) {
				e.printStackTrace();
			}

			TimelineFragment fragment = new TimelineFragment();

			Bundle b = new Bundle();

			b.putInt("top_banner_id", top_banner_id);

			BaseParserCreator creator = new BaseParserCreator(
					BooksTopBannerUrlParser.class,
					BooksTopBannerJsonParser.class, b);

			Bundle bundel = new Bundle();

			bundel.putBoolean(TimelineFragment.TIMELINE_ADAPTER, true);

			bundel.putBoolean(TimelineFragment.HIDE_BOTTOM, false);
			
			bundel.putBoolean(TimelineFragment.ENABLE_LOADING_MORE, false);

			bundel.putParcelable(TimelineFragment.PARSER_CREATOR, creator);

			fragment.setArguments(bundel);

			getSupportFragmentManager().beginTransaction()
					.add(R.id.fragment_container, fragment).commit();
		}

	}
	


}
