package com.jingdong.app.reader.bookstore.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity.ModuleChildList;
import com.jingdong.app.reader.entity.extra.BookStoreModuleBookListEntity.ModuleLinkChildList;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * 
 *
 *
 *                #####################################################
 *                #                                                   #
 *                #                       _oo0oo_                     #
 *                #                      o8888888o                    #
 *                #                      88" . "88                    #
 *                #                      (| -_- |)                    #
 *                #                      0\  =  /0                    #
 *                #                    ___/`---'\___                  #
 *                #                  .' \\|     |# '.                 #
 *                #                 / \\|||  :  |||# \                #
 *                #                / _||||| -:- |||||- \              #
 *                #               |   | \\\  -  #/ |   |              #
 *                #               | \_|  ''\---/''  |_/ |             #
 *                #               \  .-\__  '-'  ___/-. /             #
 *                #             ___'. .'  /--.--\  `. .'___           #
 *                #          ."" '<  `.___\_<|>_/___.' >' "".         #
 *                #         | | :  `- \`.;`\ _ /`;.`/ - ` : | |       #
 *                #         \  \ `_.   \_ __\ /__ _/   .-` /  /       #
 *                #     =====`-.____`.___ \_____/___.-`___.-'=====    #
 *                #                       `=---='                     #
 *                #     ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~   #
 *                #                                                   #
 *                #               佛祖保佑         永无BUG              #
 *                #                                                   #
 *                #####################################################
 *
 *
 *
 * @ClassName: HeaderGridViewAdapter
 * @Description: 书城首页顶部网格布局
 * @author J.Beyond
 * @date 2015年7月25日 下午5:32:15
 *
 */
public class HeaderGridViewAdapter extends BaseAdapter {

	private Context context;
	private LayoutInflater inflater;
	private ImageLoadingListenerImpl loadingListenerImpl;
	private List<ModuleLinkChildList> mDataList;
	private int imageWidth;
	private int imageHeight;

	public HeaderGridViewAdapter(Context context, List<BookStoreModuleBookListEntity> headerMudleList) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
		this.loadingListenerImpl = new ImageLoadingListenerImpl();
		this.mDataList = new ArrayList<ModuleLinkChildList>();
		for (BookStoreModuleBookListEntity moduleBookListEntity : headerMudleList) {
			List<ModuleLinkChildList> childLists = moduleBookListEntity.moduleLinkChildList;
			mDataList.addAll(childLists);
		}

	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mDataList.size();
	}

	@Override
	public Object getItem(int position) {
		return mDataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.bookstore_style_topic_item, null);
			holder = new ViewHolder();
			holder.topicImg = (RoundNetworkImageView) convertView.findViewById(R.id.topic_image);
			holder.topicName = (TextView) convertView.findViewById(R.id.title);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		ModuleLinkChildList moduleChildList = mDataList.get(position);
		holder.topicName.setText(moduleChildList.showName);
		ImageLoader.getInstance().displayImage(moduleChildList.picAddressAll, holder.topicImg, GlobalVarable.getDefaultBookDisplayOptions(),
				loadingListenerImpl);

		return convertView;
	}

	static class ViewHolder {
		RoundNetworkImageView topicImg;
		TextView topicName;

	}

	public static class ImageLoadingListenerImpl extends SimpleImageLoadingListener {
		public static final List<String> displayedImages = Collections.synchronizedList(new LinkedList<String>());

		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap bitmap) {
			if (bitmap != null) {
				ImageView imageView = (ImageView) view;
				boolean isFirstDisplay = !displayedImages.contains(imageUri);
				if (isFirstDisplay) {
					// 图片的淡入效果
					FadeInBitmapDisplayer.animate(imageView, 500);
					displayedImages.add(imageUri);
				}
			}
		}
	}

}
