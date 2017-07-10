package com.jingdong.app.reader.reading;

import java.util.List;
import java.util.Locale;

import org.apache.http.Header;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.ReadOverlayActivity;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.loopj.android.http.RequestParams;

public class BookBackCoverView extends FrameLayout {
	
	public interface IBackCoverActionListener {
		public void purchaseFullBook();

		public void finishRating(float rating);
	}
    
    public void queryReadData(){
    	
    	if (!LoginUser.isLogin()) {
			return;
		}
		if (!NetWorkUtils.isNetworkConnected(getContext())) {
			return;
		}
		RequestParams request = RequestParamsPool.getReadingData(bookId, serverDocId);
		WebRequestHelper.get(URLText.bookReadingDataUrl, request, true,
				new MyAsyncHttpResponseHandler(getContext()) {

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						try {
							String response = new String(responseBody, "utf-8");
							JSONObject obj = new JSONObject(response);
							int timeInSec = obj.optInt("total_time_in_seconds");
	                        if (timeInSec >= 3600) {
	                            float time = timeInSec / 3600f;
	                            String timeText = String.format(Locale.getDefault(), "%.1f", time);
	                            timeText += getResources().getString(R.string.hour);
	                            readTime.setText(timeText);
	                        } else if (timeInSec >= 60 && timeInSec < 3600) {
	                            int time = timeInSec / 60;
	                            String timeText = String.valueOf(time);
	                            timeText += getResources().getString(R.string.minute);
	                            readTime.setText(timeText);
	                        } else {
	                        	String timeText = String.valueOf(timeInSec);
	                        	timeText += getResources().getString(R.string.second);
	                            readTime.setText(timeText);
	                        }
	                        float rating = (float) obj.optDouble("rating", -1f);
	                        if (rating >= 0) {
	                            ratingBar.setRating(rating);
	                        }
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {

					}
				});
    }
    
   
    private View backoverLayout;
    private View purchaseLayout;
    private View ratingPlease;
    private TextView readTime;
    private TextView readNote;
    private RatingBar ratingBar;
    private Button purchaseFullBook;
    private IBackCoverActionListener readFunction;
    private int documentId = 0;
    private long bookId = 0;
    private long serverDocId = 0;
    private String userId = "";
    private Context mContext;
    private boolean isLockTheme = false;
    
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        List<ReadNote> noteList = null;
        if (bookId > 0) {
        	noteList = MZBookDatabase.instance.listEBookReadNote(userId, bookId);
        } else if (documentId > 0) {
        	noteList = MZBookDatabase.instance.listDocReadNote(userId, documentId);
        }
        if (noteList != null && noteList.size() > 0) {
        	readNote.setText(String.format(
    				mContext.getString(R.string.backover_notes), noteList.size()));
        }
        queryReadData();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        WebRequestHelper.cancleRequest(mContext);
    }

	public BookBackCoverView(Context context) {
		super(context);
		mContext = context;
		initView(context);
	}
    
	public BookBackCoverView(Context context, boolean isLockTheme) {
		super(context);
		mContext = context;
		this.isLockTheme = isLockTheme;
		initView(context);
	}

	public BookBackCoverView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView(context);
	}

	public BookBackCoverView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		initView(context);
	}

    public void setBackCoverActionListener(IBackCoverActionListener readFunction) {
        this.readFunction = readFunction;

    }

    private void initView(Context context) {
        inflate(context, R.layout.view_book_backover, this);
        backoverLayout = findViewById(R.id.backoverLayout);
        purchaseLayout = findViewById(R.id.purchaseLayout);
        readTime = (TextView) findViewById(R.id.readTimeText);
        readNote = (TextView) findViewById(R.id.readNoteText);
		readNote.setText(String.format(
				context.getString(R.string.backover_notes), 0));
        ratingPlease = findViewById(R.id.ratingPlease);
        purchaseFullBook = (Button) findViewById(R.id.purchaseFullbook);
        purchaseFullBook.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                readFunction.purchaseFullBook();
            }

        });
        
        updateTheme(context);
    }
    
    public void updateTheme(Context context) {
    	int read_back_cover_time_img;
    	int read_back_cover_note_img;
    	int read_back_cover_share_img;
    	int read_button_bg;
    	int r_theme;
    	int r_bg_main;
    	int r_text_sub;
    	int r_text_main;
    	int progress = 0;
    	if (ratingBar != null) {
    		progress = ratingBar.getProgress();
    		ratingBar.setVisibility(View.GONE);
    	}
    	if (isLockTheme) {
    		ratingBar = (RatingBar) ratingPlease.findViewById(R.id.book_rating_standard);
			read_back_cover_time_img = R.drawable.backcover_time_standard;
			read_back_cover_note_img = R.drawable.backcover_note_standard;
			read_back_cover_share_img = R.drawable.backcover_share_standard;
			read_button_bg = R.drawable.reader_btn_standard_s12;
			r_theme = R.color.r_theme;
			r_bg_main = R.color.r_bg_main;
			r_text_sub = R.color.r_text_sub;
			r_text_main = R.color.r_text_main;
    	} else if (LocalUserSetting.readStyle == ReadOverlayActivity.READ_STYLE_NIGHT) {
    		ratingBar = (RatingBar) ratingPlease.findViewById(R.id.book_rating_night);
    		read_back_cover_time_img = R.drawable.backcover_time_night;
			read_back_cover_note_img = R.drawable.backcover_note_night;
			read_back_cover_share_img = R.drawable.backcover_share_night;
			read_button_bg = R.drawable.reader_btn_night_s12;
			r_theme = R.color.n_theme;
			r_bg_main = R.color.n_bg_main;
			r_text_sub = R.color.n_text_sub;
			r_text_main = R.color.n_text_main;
		} else if (LocalUserSetting.readStyle == ReadOverlayActivity.READ_STYLE_MINT) {
			ratingBar = (RatingBar) ratingPlease.findViewById(R.id.book_rating_mint);
			read_back_cover_time_img = R.drawable.backcover_time_mint;
			read_back_cover_note_img = R.drawable.backcover_note_mint;
			read_back_cover_share_img = R.drawable.backcover_share_mint;
			read_button_bg = R.drawable.reader_btn_mint_s12;
			r_theme = R.color.m_theme;
			r_bg_main = R.color.m_bg_main;
			r_text_sub = R.color.m_text_sub;
			r_text_main = R.color.m_text_main;
		} else if (LocalUserSetting.readStyle == ReadOverlayActivity.READ_STYLE_SOFT) {
			ratingBar = (RatingBar) ratingPlease.findViewById(R.id.book_rating_soft);
			read_back_cover_time_img = R.drawable.backcover_time_soft;
			read_back_cover_note_img = R.drawable.backcover_note_soft;
			read_back_cover_share_img = R.drawable.backcover_share_soft;
			read_button_bg = R.drawable.reader_btn_soft_s12;
			r_theme = R.color.s_theme;
			r_bg_main = R.color.s_bg_main;
			r_text_sub = R.color.s_text_sub;
			r_text_main = R.color.s_text_main;
		} else {
			ratingBar = (RatingBar) ratingPlease.findViewById(R.id.book_rating_standard);
			read_back_cover_time_img = R.drawable.backcover_time_standard;
			read_back_cover_note_img = R.drawable.backcover_note_standard;
			read_back_cover_share_img = R.drawable.backcover_share_standard;
			read_button_bg = R.drawable.reader_btn_standard_s12;
			r_theme = R.color.r_theme;
			r_bg_main = R.color.r_bg_main;
			r_text_sub = R.color.r_text_sub;
			r_text_main = R.color.r_text_main;
		}
    	ratingBar.setProgress(progress);
    	ratingBar.setVisibility(View.VISIBLE);
        ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser && readFunction != null) {
                    readFunction.finishRating(rating);
                }
            }

        });
    	
    	Resources res = context.getResources();
    	findViewById(R.id.read_backover).setBackgroundColor(res.getColor(r_bg_main));
    	((TextView)findViewById(R.id.read_backover_text)).setTextColor(res.getColor(r_text_sub));
    	((ImageView)findViewById(R.id.readTimeImage)).setImageResource(read_back_cover_time_img);
    	readTime.setTextColor(res.getColor(r_text_sub));
    	((ImageView)findViewById(R.id.readNoteImage)).setImageResource(read_back_cover_note_img);
    	((TextView)findViewById(R.id.readNoteText)).setTextColor(res.getColor(r_text_sub));
    	((TextView)findViewById(R.id.ratingPleaseText)).setTextColor(res.getColor(r_theme));
    	((ImageView)findViewById(R.id.bookShareImage)).setImageResource(read_back_cover_share_img);
    	((TextView)findViewById(R.id.bookShareText)).setTextColor(res.getColor(r_text_main));
    	((TextView)findViewById(R.id.purchaseText)).setTextColor(res.getColor(r_text_sub));
    	purchaseFullBook.setBackgroundResource(read_button_bg);
    	purchaseFullBook.setTextColor(res.getColor(r_bg_main));
    }

    public void setBookId(long bookId) {
        this.bookId = bookId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setServerDocId(long serverDocId) {
        this.serverDocId = serverDocId;
    }

	public void setDocumentId(int documentId) {
		this.documentId = documentId;
	}

	public void showRatingBar(boolean show) {
		ratingPlease.setVisibility(show ? View.VISIBLE : View.GONE);
	}
	
	public void showPurchase(boolean show) {
		backoverLayout.setVisibility(show ? View.GONE : View.VISIBLE);
		purchaseLayout.setVisibility(show ? View.VISIBLE : View.GONE);
	}
}
