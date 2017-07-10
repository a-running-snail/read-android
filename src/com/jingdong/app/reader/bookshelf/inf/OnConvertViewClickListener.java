package com.jingdong.app.reader.bookshelf.inf;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

/**
 * 
 */
public abstract class OnConvertViewClickListener implements View.OnClickListener{

    private View convertView;
    private int[] positionIds;
    public OnConvertViewClickListener(View convertView, int... positionIds) {
        this.convertView = convertView;
        this.positionIds = positionIds;
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
    @Override
    public void onClick(View v) {
        int len = positionIds.length;
        int[] positions = new int[len];
        for(int i = 0; i < len; i++){
            positions[i] = (int) convertView.getTag(positionIds[i]);
        }
        onClickCallBack(v, positions);
    }

    public abstract void onClickCallBack(View registedView, int... positionIds);

}
