package com.jingdong.app.reader.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.epub.epub.PlayItem;
import com.jingdong.app.reader.media.MediaPlayerHelper;
import com.jingdong.app.reader.service.MediaDownloadService;
import com.jingdong.app.reader.util.ViewHolder;

public class BookPlayListActivity extends BaseActivityWithTopBar {
	public static final String PlayListKey = "PlayListKey";
	public static final String AudioPathKey = "AudioPathKey";
	private ArrayList<PlayItem> playList = new ArrayList<PlayItem>();

	private ImageButton playButton;
	private ProgressBar playProgress;
	private Timer timer;
	private TimerTask timerTask;

	private TextView durationTextView;
	private TextView positionTextView;

	private String audioPath;
	private TextView audioName;
	private TextView audioAuthor;

	private PlayListAdapter playListAdapter = new PlayListAdapter();

	private int selectedIndex = -1;
	
	private ImageView last;
	private ImageView next;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_playlist);
		this.getTopBarView().setTitle(getString(R.string.all_audio));
		playList = getIntent().getParcelableArrayListExtra(PlayListKey);
		audioPath = getIntent().getStringExtra(AudioPathKey);

		audioName = (TextView) findViewById(R.id.audioName);
		audioAuthor = (TextView) findViewById(R.id.audioAuthor);
		
		last=(ImageView) findViewById(R.id.last);
		next=(ImageView) findViewById(R.id.next);
		
		
		last.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(selectedIndex<0)
					return;
				else if (selectedIndex==0) {
					selectedIndex=0;
				}
				else if (selectedIndex>=1) {
					selectedIndex=selectedIndex-1;
				}
				
				playByIndex(selectedIndex);
				playListAdapter.notifyDataSetChanged();
			}
		});
		next.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if(selectedIndex<0)
					return;
				else if (selectedIndex==playList.size()-1) {
					selectedIndex=playList.size()-1;
				}
				else if (selectedIndex<=playList.size()-2) {
					selectedIndex=selectedIndex+1;
				}
				
				playByIndex(selectedIndex);
				playListAdapter.notifyDataSetChanged();
			}
		});
		
		View navLabel = findViewById(R.id.nav_chapter);

		if (!TextUtils.isEmpty(MediaPlayerHelper.playSource)) {
			for (int i = 0; i < playList.size(); ++i) {
				PlayItem item = playList.get(i);
				if (MediaPlayerHelper.playSource.equals(MediaPlayerHelper
						.getLocalAudioPath(audioPath, item.mediaPath))) {
					selectedIndex = i;
					break;
				}
			}
		}

		playProgress = (ProgressBar) findViewById(R.id.audioProgress);

		playButton = (ImageButton) findViewById(R.id.playAudio);

		if (selectedIndex != -1
				&& MediaPlayerHelper.isPlaying(MediaPlayerHelper.playSource)) {
			updatePlayButton(true);
		} else {
			updatePlayButton(false);
		}

		playButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(MediaPlayerHelper.playSource)) {
					boolean isPlaying = MediaPlayerHelper
							.play(MediaPlayerHelper.playSource);
					updatePlayButton(isPlaying);

				}
			}

		});

		navLabel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (selectedIndex != -1) {
					PlayItem item = playList.get(selectedIndex);
					Intent data = new Intent();
					data.putExtra(BookPageViewActivity.PlayListLocationKey,
							item.navSrc);
					setResult(RESULT_OK, data);
				}
				finish();
			}

		});

		ListView listView = (ListView) findViewById(R.id.playList);
		listView.setAdapter(playListAdapter);

		if (selectedIndex != -1) {
			PlayItem item = playList.get(selectedIndex);
			// nav.setVisibility(View.VISIBLE);
			audioName.setText(item.title);
			audioAuthor.setText(item.author);
			// navLabel.setText(item.navTitle);
		}

		durationTextView = (TextView) findViewById(R.id.totalTime);
		positionTextView = (TextView) findViewById(R.id.playTime);
	}

	void updatePlayButton(boolean isPlaying) {
		if (isPlaying) {
			playButton.setImageResource(R.drawable.radio_play_pause);
		} else {
			playButton.setImageResource(R.drawable.radio_play);
		}
	}

	public void playByIndex(int index){
		PlayItem item = playList.get(index);
		String playPath = item.mediaPath;
		if (!TextUtils.isEmpty(audioPath)) {
			String fileName = URLUtil.guessFileName(item.mediaPath,
					null, null);
			File file = new File(audioPath, fileName);
			if (!file.exists()) {
				return;
			} else {
				playPath = file.getPath();
			}
			
			boolean isPlaying = MediaPlayerHelper.play(playPath);
			updatePlayButton(isPlaying);
		}
		
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		startTimer();
		registerDownloadReceiver();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopTimer();
		unregisterDownloadReceiver();
	}

	private BroadcastReceiver mediaDownloadReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					MediaDownloadService.ACTION_MEDIA_DOWNLOAD)) {
				playListAdapter.notifyDataSetChanged();
			}
		}

	};

	private void registerDownloadReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(MediaDownloadService.ACTION_MEDIA_DOWNLOAD);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mediaDownloadReceiver, filter);

	}

	private void unregisterDownloadReceiver() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mediaDownloadReceiver);
	}

	private String millisecondToString(int time) {
		if (time <= 0) {
			return "0:00";
		} else {
			int min = time / 1000 / 60;
			int sec = time / 1000 % 60;
			return String.format(Locale.getDefault(), "%d:%02d", min, sec);
		}
	}

	private void startTimer() {
		if (timer == null) {
			timer = new Timer();
		}
		if (timerTask == null) {
			timerTask = new TimerTask() {

				@Override
				public void run() {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							int currentPosition = MediaPlayerHelper
									.getCurrentPosition();
							int duration = MediaPlayerHelper.getDuration();
							if (currentPosition > 0 && duration > 0) {
								durationTextView
										.setText(millisecondToString(duration));
								positionTextView
										.setText(millisecondToString(currentPosition));
								playProgress.setProgress(currentPosition * 100
										/ duration);

							} else {
								durationTextView.setText("0:00");
								positionTextView.setText("0:00");
								playProgress.setProgress(0);
							}

						}

					});

				}

			};
		}
		timer.schedule(timerTask, 0, 1000);
	}

	private void stopTimer() {
		if (timerTask != null) {
			timerTask.cancel();
			timerTask = null;
		}
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	private class PlayListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return playList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			if (convertView == null) {
				convertView = LayoutInflater.from(BookPlayListActivity.this)
						.inflate(R.layout.item_radio_play, parent, false);
			}

			LinearLayout itemLayout = ViewHolder.get(convertView,
					R.id.radio_item);
			TextView name = ViewHolder.get(convertView, R.id.radio_name);
			TextView author = ViewHolder.get(convertView, R.id.radio_author);
			final ImageView state = ViewHolder.get(convertView,
					R.id.radio_download);
			final View indictor = ViewHolder.get(convertView, R.id.indictor);
			
			PlayItem item = playList.get(position);
			String src = item.mediaPath;

			name.setText(item.title);
			author.setText(item.author);

			if (position % 2 != 0)
				itemLayout.setBackgroundColor(getResources().getColor(R.color.white));
			else {
				itemLayout.setBackgroundColor(getResources().getColor(R.color.font_white_hl));
			}
			
			if (selectedIndex == position) {

				indictor.setVisibility(View.VISIBLE);
				name.setTextColor(getResources().getColor(R.color.bookshelf_gifts));
				author.setTextColor(getResources().getColor(R.color.bookshelf_gifts));
			} else {
				indictor.setVisibility(View.INVISIBLE);
				name.setTextColor(getResources().getColor(R.color.catalog_font_color));
				author.setTextColor(getResources().getColor(R.color.catalog_font_color));
			}

			if (MediaDownloadService.mediaDownloadingQueue
					.contains(item.mediaPath)) {
				state.setImageResource(R.drawable.radio_downloading);
			}
			if (!TextUtils.isEmpty(audioPath)) {
				String fileName = URLUtil.guessFileName(src, null, null);
				File file = new File(audioPath, fileName);
				if (!file.exists()) {
					state.setImageResource(R.drawable.radio_download);
				} else {
					state.setImageResource(R.drawable.radio_downloaded);
				}
			} else {
				state.setImageResource(R.drawable.radio_download);
			}

			if(selectedIndex!=-1)
			{
				audioName.setText(playList.get(selectedIndex).title);
				audioAuthor.setText(playList.get(selectedIndex).author);
			}

			
			itemLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					PlayItem item = playList.get(position);
					String playPath = item.mediaPath;
					if (item.mediaPath.toLowerCase(Locale.getDefault())
							.startsWith("http")
							&& !TextUtils.isEmpty(audioPath)) {
						String fileName = URLUtil.guessFileName(item.mediaPath,
								null, null);
						File file = new File(audioPath, fileName);
						if (!file.exists()) {
							if (!MediaDownloadService.mediaDownloadingQueue
									.contains(item.mediaPath)) {
								state.setImageResource(R.drawable.radio_downloading);
								Intent intent = new Intent(
										BookPlayListActivity.this,
										MediaDownloadService.class);
								intent.putExtra(
										MediaDownloadService.MediaUrlPathKey,
										item.mediaPath);
								intent.putExtra(
										MediaDownloadService.MediaSavePathKey,
										audioPath);
								MediaDownloadService
										.addDownloadUrl(item.mediaPath);
								startService(intent);
								// playListAdapter.notifyDataSetChanged();
							}
							return;
						} else {
							playPath = file.getPath();
							
						}
					}

					audioName.setText(item.title);
					audioAuthor.setText(item.author);
					boolean isPlaying = MediaPlayerHelper.play(playPath);
					updatePlayButton(isPlaying);
					selectedIndex = position;
					playListAdapter.notifyDataSetChanged();
				}
			});

			return convertView;
		}
	}
}
