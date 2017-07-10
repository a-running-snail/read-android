package com.jingdong.app.reader.epub.paging;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.activity.DictionarySettingActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.notes.NotesModel;
import com.jingdong.app.reader.reading.ReadNote;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ReadPopWindow extends PopupWindow {

    private Context context;
    private View rootView;
    private ImageView topArrow;
    private ImageView bottomArrow;
    private IPopEventHandler handler;
    private int rootViewHeight;
    private int peopleNoteIndex;
    private float arrowX;
    private boolean isCreateNote = false;
    private boolean isPrivateNote = false;
    private boolean isPopShowing = false;
    private ImageView recommandImageView;
    
    protected ReadPopWindow(Context context, IPopEventHandler handler) {
        super(context);
        this.context = context;
        this.handler = handler;
    }
    
    protected boolean isCreateNote() {
        return this.isCreateNote;
    }
    
    protected void setCreateNote(boolean isCreateNote) {
        this.isCreateNote = isCreateNote;
    }
    
    protected void setPrivateNote(boolean isPrivateNote) {
        this.isPrivateNote = isPrivateNote;
    }
    
    public boolean isPopShowing() {
        return isPopShowing;
    }

    public void setPopShowing(boolean isPopShowing) {
        this.isPopShowing = isPopShowing;
    }

    protected void showNoteActionBar() {
        rootView = View.inflate(context, R.layout.view_note_actionbar, null);
        topArrow = (ImageView) rootView.findViewById(R.id.topArrow);
        bottomArrow = (ImageView) rootView.findViewById(R.id.bottomArrow);

        if (rootViewHeight == 0) {
            rootView.setLayoutParams(new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT));
            int w = View.MeasureSpec.makeMeasureSpec(getWidth(),View.MeasureSpec.EXACTLY);
            int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            rootView.measure(w, h);
            rootViewHeight = rootView.getMeasuredHeight();
        }

        setContentView(rootView);
        final View noteLayout = rootView.findViewById(R.id.noteLayout);
        final View editLayout = rootView.findViewById(R.id.editLayout);
        final View shareLayout = rootView.findViewById(R.id.shareLayout);
        final View moreLayout = rootView.findViewById(R.id.moreLayout);
        
        OnClickListener listener = new OnClickListener() {
            
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                case R.id.noteCopy:
                case R.id.editCopy:
                    handler.onNoteCopy();
                    break;
                case R.id.noteText:
                    handler.onNoteCreate();
                    break;
                case R.id.editText:
                    handler.onNoteModify();
                    break;
                case R.id.noteLine:
                    handler.onDigestCreate(true);
                    break;
                case R.id.noteShare:
                    if (isCreateNote) {
                        if (TextUtils.isEmpty(BookPageViewActivity.getUserId())) {
                            ToastUtil.showToastInThread(R.string.read_note_need_login);
                            Intent it = new Intent(context, LoginActivity.class);
                            context.startActivity(it);
                            return;
                        }
                        handler.onDigestCreate(false);
                    }
                case R.id.editShare:
                    moreLayout.setVisibility(View.GONE);
                    editLayout.setVisibility(View.GONE);
                    shareLayout.setVisibility(View.VISIBLE);
                    Button shareMZBook = (Button) shareLayout.findViewById(R.id.shareCommunity);
                    shareMZBook.setText(isPrivateNote?R.string.note_public_to_jdreader:R.string.note_private_to_jdreader);
                    break;
                case R.id.noteDictionary:
                    handler.onNoteDictionary();
                    break;
                case R.id.editDelete:
                    handler.onNoteDestory();
                    break;
                case R.id.shareCommunity:
                    handler.onNoteShareCommunity();
                    break;
                case R.id.shareSinaWeibo:
                    handler.onNoteShareSinaWeibo();
                    break;
                case R.id.shareWeChat://微信朋友圈
                    handler.onNoteShareWeChat(1);
                    break;
                case R.id.shareWXFriends://微信好友
                    handler.onNoteShareWeChat(0);
                    break;
                case R.id.more:
                    noteLayout.setVisibility(View.GONE);
                    moreLayout.setVisibility(View.VISIBLE);
                    break;
                case R.id.back:
                    noteLayout.setVisibility(View.VISIBLE);
                    moreLayout.setVisibility(View.GONE);
                    break;
                }
            }
        };
        
        if (isCreateNote) {
            noteLayout.setVisibility(View.VISIBLE);
            editLayout.setVisibility(View.GONE);
            shareLayout.setVisibility(View.GONE);
            noteLayout.findViewById(R.id.noteCopy).setOnClickListener(listener);
            noteLayout.findViewById(R.id.noteText).setOnClickListener(listener);
            noteLayout.findViewById(R.id.noteLine).setOnClickListener(listener);
            noteLayout.findViewById(R.id.more).setOnClickListener(listener);
            moreLayout.findViewById(R.id.back).setOnClickListener(listener);
            moreLayout.findViewById(R.id.noteShare).setOnClickListener(listener);
            moreLayout.findViewById(R.id.noteDictionary).setOnClickListener(listener);
            shareLayout.findViewById(R.id.shareCommunity).setOnClickListener(listener);
            shareLayout.findViewById(R.id.shareSinaWeibo).setOnClickListener(listener);
            shareLayout.findViewById(R.id.shareWeChat).setOnClickListener(listener);
            shareLayout.findViewById(R.id.shareWXFriends).setOnClickListener(listener);
        } else {
            noteLayout.setVisibility(View.GONE);
            editLayout.setVisibility(View.VISIBLE);
            shareLayout.setVisibility(View.GONE);
            moreLayout.setVisibility(View.GONE);
            noteLayout.findViewById(R.id.noteCopy).setOnClickListener(listener);
            noteLayout.findViewById(R.id.noteText).setOnClickListener(listener);
            noteLayout.findViewById(R.id.noteLine).setOnClickListener(listener);
            noteLayout.findViewById(R.id.more).setOnClickListener(listener);
            editLayout.findViewById(R.id.editCopy).setOnClickListener(listener);
            editLayout.findViewById(R.id.editText).setOnClickListener(listener);
            editLayout.findViewById(R.id.editShare).setOnClickListener(listener);
            editLayout.findViewById(R.id.editDelete).setOnClickListener(listener);
            shareLayout.findViewById(R.id.shareCommunity).setOnClickListener(listener);
            shareLayout.findViewById(R.id.shareSinaWeibo).setOnClickListener(listener);
            shareLayout.findViewById(R.id.shareWeChat).setOnClickListener(listener);
            shareLayout.findViewById(R.id.shareWXFriends).setOnClickListener(listener);
        }
        
    }
    
    /**
     * 显示词典查询结果
     * @param word
     * @param result
     * 
     */
    protected void showDictionaryResult(final String word,String result,boolean isDictionaryReady) {
        rootView = View.inflate(context, R.layout.view_note_dictionary, null);

        topArrow = (ImageView) rootView.findViewById(R.id.topArrow);
        bottomArrow = (ImageView) rootView.findViewById(R.id.bottomArrow);
        TextView textview = (TextView) rootView.findViewById(R.id.dictionaryResult);
        RelativeLayout noDicLayout = (RelativeLayout) rootView.findViewById(R.id.no_dictionary_layout);
        if(isDictionaryReady){
        	textview.setVisibility(View.VISIBLE);
        	noDicLayout.setVisibility(View.GONE);
        	textview.setMovementMethod(ScrollingMovementMethod.getInstance()); 
            textview.setText(result);
        }else{
        	textview.setVisibility(View.GONE);
        	noDicLayout.setVisibility(View.VISIBLE);
        	
        	TextView downloadTv = (TextView) rootView.findViewById(R.id.download_text);
        	downloadTv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					Intent intent= new Intent(context, DictionarySettingActivity.class);
					intent.putExtra("word", word);
					intent.putExtra("startFlag", true);
					context.startActivity(intent);
				}
			});
        }
        
        
        LinearLayout translateHeader = (LinearLayout) rootView.findViewById(R.id.translateHeader);
        if(word == null) {
            translateHeader.setVisibility(View.GONE);
            rootView.findViewById(R.id.dictionarySupport).setVisibility(View.GONE);
            rootView.findViewById(R.id.baike).setVisibility(View.GONE);
            rootView.findViewById(R.id.dictionary_divideline1).setVisibility(View.GONE);
            rootView.findViewById(R.id.youdao).setVisibility(View.GONE);
            rootView.findViewById(R.id.dictionary_divideline2).setVisibility(View.GONE);
            rootView.findViewById(R.id.iciba).setVisibility(View.GONE);
        } else {
            TextView translateTv  = (TextView) rootView.findViewById(R.id.translateWords);
            ImageView dictionarySetting = (ImageView) rootView.findViewById(R.id.dictionarySetting);
            translateTv.setText(word);
            dictionarySetting.setOnClickListener(new OnClickListener() {
                
                @Override
                public void onClick(View arg0) {
                    Intent intent= new Intent(context,DictionarySettingActivity.class);
                    intent.putExtra("word", word);
                    intent.putExtra("startFlag", false);
                    context.startActivity(intent);
                }
            });
            //百度百科
            TextView baikeTv = (TextView) rootView.findViewById(R.id.baike);
            baikeTv.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    try {
                    	String encodeWord=URLEncoder.encode(word, WebRequestHelper.CHAR_SET);
                    	String urlText = String.format(URLText.getBaikeWords, encodeWord);
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.TypeKey, "baike");
                        intent.putExtra(WebViewActivity.UrlKey, urlText);
                        context.startActivity(intent);
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }

                }
            });
            //有道词典
            TextView youdaoTv = (TextView) rootView.findViewById(R.id.youdao);
            youdaoTv.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    try {
                    	String encodeWord=URLEncoder.encode(word, WebRequestHelper.CHAR_SET);
                    	String urlText = "http://m.youdao.com/dict?q=" + encodeWord;
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.UrlKey, urlText);
                        context.startActivity(intent);
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }
                }
            });

            TextView icibaTv = (TextView) rootView.findViewById(R.id.iciba);
            icibaTv.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    try {
                    	String encodeWord=URLEncoder.encode(word, WebRequestHelper.CHAR_SET);
                    	String urlText = "http://www.iciba.com/"+encodeWord;
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.UrlKey, urlText);
                        context.startActivity(intent);
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }

                }
            });
        }

        rootView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        int w = View.MeasureSpec.makeMeasureSpec(getWidth(),
                View.MeasureSpec.EXACTLY);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        rootView.measure(w, h);
        rootViewHeight = rootView.getMeasuredHeight();

        setContentView(rootView);
    }
    
    protected void showMyNoteContent(ReadNote note) {
        rootView = View.inflate(context, R.layout.view_note_review_me, null);

        topArrow = (ImageView) rootView.findViewById(R.id.topArrow);
        bottomArrow = (ImageView) rootView.findViewById(R.id.bottomArrow);
        TextView textview = (TextView) rootView.findViewById(R.id.noteContent);
        textview.setMovementMethod(ScrollingMovementMethod.getInstance()); 
        textview.setText(note.contentText);

        rootView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        int w = View.MeasureSpec.makeMeasureSpec(getWidth(),
                View.MeasureSpec.EXACTLY);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        rootView.measure(w, h);
        rootViewHeight = rootView.getMeasuredHeight();

        setContentView(rootView);
        
        final CheckBox checkBox = (CheckBox) rootView.findViewById(R.id.shareTimeline);
        if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            checkBox.setPadding(checkBox.getPaddingLeft(), checkBox.getPaddingTop(), checkBox.getPaddingRight(),
                    checkBox.getPaddingBottom());
        } else {
            checkBox.setPadding(checkBox.getPaddingLeft()
                    + context.getResources().getDrawable(R.drawable.post_tweet_checkbox).getIntrinsicWidth(),
                    checkBox.getPaddingTop(), checkBox.getPaddingRight(), checkBox.getPaddingBottom());
        }
        OnClickListener listener = new OnClickListener() {
            
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                case R.id.noteEdit:
                    handler.onNoteModify();
                    dismiss();
                    break;
                case R.id.noteDelete:
                    handler.onNoteDestory();
                    dismiss();
                    break;
                case R.id.shareTimeline:
                    checkBox.setChecked(!checkBox.isChecked());
                    handler.onNoteShareCommunity();
                    dismiss();
                    break;
                }
            }
        };
        
        checkBox.setChecked(!note.isPrivate);
        checkBox.setOnClickListener(listener);
        rootView.findViewById(R.id.noteEdit).setOnClickListener(listener);
        rootView.findViewById(R.id.noteDelete).setOnClickListener(listener);
    }
    
    protected void showPeopleNoteContent(ReadNote note, NotesModel model) {
        rootView = View.inflate(context, R.layout.view_note_review_people, null);

        topArrow = (ImageView) rootView.findViewById(R.id.topArrow);
        bottomArrow = (ImageView) rootView.findViewById(R.id.bottomArrow);
        setPeopleNotesView(note, model);
        
        rootView.findViewById(R.id.noteCountLayout).setVisibility(View.GONE);

        rootView.setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        int w = View.MeasureSpec.makeMeasureSpec(getWidth(),
                View.MeasureSpec.EXACTLY);
        int h = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED);
        rootView.measure(w, h);
        rootViewHeight = rootView.getMeasuredHeight();

        setContentView(rootView);
        
    }
    
    protected void showPeopleNoteContent(final List<ReadNote> noteList) {
        if (noteList == null || noteList.size() == 0) {
            return;
        }
        peopleNoteIndex = 0;
        ReadNote note = noteList.get(peopleNoteIndex);
        NotesModel model = handler.findUserNotesModel(note.userId);
        showPeopleNoteContent(note, model);
        
        if (noteList.size() <= 1) {
            rootView.findViewById(R.id.noteCountLayout).setVisibility(View.GONE);
            return;
        }
        rootView.findViewById(R.id.noteCountLayout).setVisibility(View.VISIBLE);
        TextView noteIndex = (TextView) rootView.findViewById(R.id.noteIndex);
        noteIndex.setText(String.valueOf(peopleNoteIndex+1) + "/" + String.valueOf(noteList.size()));
        OnClickListener listener = new OnClickListener() {
            ReadNote note;
            NotesModel model;
            
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                case R.id.prevNote:
                    if (peopleNoteIndex >= 1 && peopleNoteIndex < noteList.size()) {
                        peopleNoteIndex --;
                        setupNotesPopView();
                    }
                    break;
                case R.id.nextNote:
                    if (peopleNoteIndex >= 0 && peopleNoteIndex < noteList.size() - 1) {
                        peopleNoteIndex ++;
                        setupNotesPopView();
                    }
                    break;
                }
            }
            
            private void setupNotesPopView() {
                note = noteList.get(peopleNoteIndex);
                model = handler.findUserNotesModel(note.userId);
                setPeopleNotesView(note, model);
                TextView noteIndex = (TextView) rootView.findViewById(R.id.noteIndex);
                noteIndex.setText(String.valueOf(peopleNoteIndex+1) + "/" + String.valueOf(noteList.size()));
            }
        };
        
        rootView.findViewById(R.id.prevNote).setOnClickListener(listener);
        rootView.findViewById(R.id.nextNote).setOnClickListener(listener);
    }
    
    private void setPeopleNotesView(final ReadNote note, NotesModel model) {
        TextView textview = (TextView) rootView.findViewById(R.id.noteContent);
        textview.setMovementMethod(ScrollingMovementMethod.getInstance()); 
        textview.setText(note.contentText);
        
        if (model == null) {
            // TODO 没有找到对应用户，那么临时构建一个用户来使用，确保能正常浏览笔记而程序不会crash
            model = new NotesModel();
            model.userName = "ID"+note.userId;
            model.role = 0;
            handler.refreshUserNotesModel();
        }
        TextView username = (TextView) rootView.findViewById(R.id.user_name);
        username.setText(model.userName);
        
        RoundNetworkImageView avatarImageView = (RoundNetworkImageView) rootView.findViewById(R.id.thumb_nail);
        ImageLoader.getInstance().displayImage(model.avatarUrl, avatarImageView, GlobalVarable.getDefaultAvatarDisplayOptions(false));

        ImageView avatarLabel = (ImageView) rootView.findViewById(R.id.avatar_label);
        if (model.role == 1 || model.role == 2) {
            avatarLabel.setVisibility(View.VISIBLE);
            if (model.role == 1) {
                avatarLabel.setImageResource(R.drawable.profile_verify_person);
            } else {
                avatarLabel.setImageResource(R.drawable.profile_verify_organization);
            }
        } else {
            avatarLabel.setVisibility(View.INVISIBLE);
        }
        
        recommandImageView = (ImageView) rootView.findViewById(R.id.noteRecommandImage);
        recommandImageView.setImageResource(note.isRecommanded?R.drawable.btn_toolbar_like:R.drawable.btn_toolbar_unlike);
        recommandImageView.setTag(note.guid);
        
        if (note.isSyncReccommandStatus) {

            syncNoteRecommandedStatus(note);
        }
        
        OnClickListener listener = new OnClickListener() {
            
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                case R.id.noteComment:
                    handler.onNoteComment(note.guid, note.serverId);
                    dismiss();
                    break;
                case R.id.noteRecommand:
                    recommandNote(note);
                    break;
                }
            }
        };
        
        rootView.findViewById(R.id.noteComment).setOnClickListener(listener);
        rootView.findViewById(R.id.noteRecommand).setOnClickListener(listener);
    }
    
    public void syncNoteRecommandedStatus(final ReadNote note){
        String url = URLText.isNotesRecommanded;
        RequestParams request = RequestParamsPool.getNotesRecommanded(note.serverId);
        WebRequestHelper.post(url, request, true, new MyAsyncHttpResponseHandler(context) {
            
            @Override
            public void onResponse(int statusCode,
                    Header[] headers, byte[] responseBody) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(responseBody));
                    note.isRecommanded = jsonObject.optBoolean("recommended");
                    note.isSyncReccommandStatus = false;
                } catch (JSONException e) {
                    MZLog.e("readnoteRecommanded", Log.getStackTraceString(e));
                }
                
                String tag = (String) recommandImageView.getTag();
                if (note != null && !TextUtils.isEmpty(note.guid) && note.guid.equals(tag)) {
                    ((Activity) context).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            recommandImageView
                                    .setImageResource(note.isRecommanded ? R.drawable.btn_toolbar_like
                                            : R.drawable.btn_toolbar_unlike);
                        }
                    });
                }
                
            }
        });
        
    }
    
    public void recommandNote(final ReadNote note)
    {
        String url = "";
        if (note.isRecommanded) {
            url = URLText.unlikeEntityUrl;
        } else {
            url = URLText.likeEntityUrl;
        }
        
        RequestParams request = RequestParamsPool.getNotesLikeOrUnlike(note.guid);
        WebRequestHelper.post(url, request, true, new MyAsyncHttpResponseHandler(context) {
            
            @Override
            public void onResponse( int statusCode,
                    Header[] headers, byte[] responseBody) {
                try {
                    JSONObject jsonObject = new JSONObject(new String(responseBody));
                    String code = jsonObject.optString("code");
                    if ("0".equals(code)) {
                        note.isRecommanded = !note.isRecommanded;
                    }
                } catch (JSONException e) {
                    MZLog.e("parsePost", Log.getStackTraceString(e));
                }
                String tag = (String) recommandImageView.getTag();
                if (note != null && note.guid.equals(tag)) {
                    ((Activity) context).runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            recommandImageView
                                    .setImageResource(note.isRecommanded ? R.drawable.btn_toolbar_like
                                            : R.drawable.btn_toolbar_unlike);
                        }
                    });
                }
            }
        });
        

        
    }


    
    protected void showTopArrow() {
        if (topArrow != null) {
            topArrow.setVisibility(View.VISIBLE);
        }
        if (bottomArrow != null) {
            bottomArrow.setVisibility(View.GONE);
        }
    }

    protected void showBottomArrow() {
        if (topArrow != null) {
            topArrow.setVisibility(View.GONE);
        }
        if (bottomArrow != null) {
            bottomArrow.setVisibility(View.VISIBLE);
        }
    }

    protected void hideArrow() {
        if (topArrow != null) {
            topArrow.setVisibility(View.GONE);
        }
        if (bottomArrow != null) {
            bottomArrow.setVisibility(View.GONE);
        }
    }

    protected void setArrowX(float x) {
        if (topArrow != null) {
            topArrow.setX(x);
        }
        if (bottomArrow != null) {
            bottomArrow.setX(x);
        }
        arrowX = x;
    }
    
    protected float getArrowX() {
        return arrowX;
    }
    
    protected int getRootViewHeight() {
        return rootViewHeight;
    }
    
}
