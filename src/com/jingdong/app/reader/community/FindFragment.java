package com.jingdong.app.reader.community;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookStoreBookListActivity;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bookshelf.SignTipDialog;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.me.activity.IntegrationActivity;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.me.model.SignSuccessionResult;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.tendcloud.tenddata.TCAgent;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class FindFragment extends CommonFragment implements OnItemClickListener{
	public ListView mListView = null;
	private List<FindEntity> mDataList = null;
	public static String[] titleStr = {"畅读卡", "限时抢", "折扣", "签到", "热销榜", "智能推荐", "积分管理"};
	public static String[] detailStr = {"购卡会员无限畅读", "海量图书限时抢购", "折扣专区畅享优惠", 
					"每日积分签到获得", "权威榜单不容错过", "畅销热销定制筛选", "个人积分批量管理"};
	public static final int icons[] = {R.drawable.find_changduka_icon, R.drawable.find_xianshiqiang_icon,
			R.drawable.find_zhekou_icon, R.drawable.find_qiandao_icon, R.drawable.find_rexiaobang_icon,
			R.drawable.find_zhinengtuijian_icon, R.drawable.find_jifenguanli_icon};

	public FindFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_find, null);
		mListView = (ListView)rootView.findViewById(R.id.mListView);
		loadData();
		mListView.setAdapter(new ListAdapter(getActivity(), mDataList));
		mListView.setOnItemClickListener(this);
		return rootView;
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if(isVisibleToUser){
			TCAgent.onPageStart(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_community_find));
			StatService.onPageStart(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_community_find));
		}else{
			TCAgent.onPageEnd(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_community_find));
			StatService.onPageEnd(getActivity(), MZBookApplication.getInstance().getString(R.string.mtj_community_find));
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if (!NetWorkUtils.isNetworkConnected(getActivity())) {
			Toast.makeText(getActivity(), getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
			return;
		}else if(!LoginUser.isLogin()){
			Intent it = new Intent(getActivity(), LoginActivity.class);
			startActivity(it);
			return;
		}		
		
		switch (arg2) {
			case 0:	//畅读卡
				Intent intent = new Intent(getActivity(), WebViewActivity.class);
				String webUrl = "http://e.m.jd.com/readCard.html";
				intent.putExtra(WebViewActivity.UrlKey,webUrl);
				intent.putExtra(WebViewActivity.TopbarKey, true);
				intent.putExtra(WebViewActivity.BrowserKey, false);
				intent.putExtra(WebViewActivity.TitleKey, "畅读卡");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				break;
			case 1: //限时抢
				Intent xianshi = new Intent(getActivity(), BookStoreBookListActivity.class);
				xianshi.putExtra("fid", 248);
				xianshi.putExtra("ftype", 2);
				xianshi.putExtra("relationType", 1);
				xianshi.putExtra("showName", "限时抢购");
				xianshi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(xianshi);
				break;
			case 2:	//折扣
				TalkingDataUtil.onBookStoreEvent(getActivity(), "优惠", "好书折扣");
				Intent zhekou = new Intent(getActivity(), BookStoreBookListActivity.class);
				zhekou.putExtra("fid", 88);
				zhekou.putExtra("ftype", 2);
				zhekou.putExtra("relationType", 1);
				zhekou.putExtra("showName", "好书折扣");
				zhekou.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(zhekou);
				break;
			case 3:	//签到
				if (!LoginUser.isLogin()) {
					Intent qiandao = new Intent(getActivity(), LoginActivity.class);
					startActivity(qiandao);
				} else {
					processSign();
				}
				break;
			case 4:	//热销榜
				TalkingDataUtil.onBookStoreEvent(getActivity(), "排行", "热销TOP100");
				Intent rexiao = new Intent(getActivity(), BookStoreBookListActivity.class);
				rexiao.putExtra("fid", 92);
				rexiao.putExtra("ftype", 2);
				rexiao.putExtra("relationType", 1);
				rexiao.putExtra("showName", "热销TOP100");
				rexiao.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(rexiao);
				break;
			case 5:	//智能推荐
				Intent tuijian = new Intent(getActivity(), BookStoreBookListActivity.class);
				tuijian.putExtra(BookStoreBookListActivity.LIST_TYPE, BookStoreBookListActivity.TYPE_RECOMMEND);
				tuijian.putExtra("showName", "智能推荐");
				tuijian.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(tuijian);				
				break;
			case 6:	//积分管理
				Intent intent4 = new Intent(getActivity(), IntegrationActivity.class);
				intent4.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent4);
				break;
				
			default:
				break;
		}
	}
	
	/**
	* @Description: 加载发现功能数据
	* @author xuhongwei1
	* @date 2015年10月29日 下午3:48:55 
	* @throws 
	*/ 
	private void loadData() {
		mDataList = new ArrayList<FindFragment.FindEntity>();
		for(int i=0; i<titleStr.length; i++) {
			FindEntity data = new FindEntity();
			data.id = i;
			data.title = titleStr[i];
			data.detail = detailStr[i];
			
			mDataList.add(data);
		}
	}
	
	class ListAdapter extends ArrayAdapter<FindEntity> {
		private int count;
		
		public ListAdapter(Context context, List<FindEntity> list) {
			super(context, 0, list);
			count = list.size();
		}
		
		@Override
		public int getCount() {
			return count;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if(null == convertView) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_find_item, null);
				holder.icon = (ImageView)convertView.findViewById(R.id.icon);
				holder.title = (TextView)convertView.findViewById(R.id.title);
				holder.detail = (TextView)convertView.findViewById(R.id.detail);
				
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			
			FindEntity data = getItem(position);
			holder.icon.setImageResource(icons[data.id]);
			holder.title.setText(data.title);
			holder.detail.setText(data.detail);
			
			return convertView;
		}
		
	}
	
	static class ViewHolder {
		ImageView icon;
		TextView title;
		TextView detail;
	}
	
	class FindEntity {
		/** 功能id */
		int id;
		/** 标题名称 */
		String title;
		/** 标题描述 */
		String detail;
	}
	
	/**
	* @Description: 签到
	* @author xuhongwei1
	* @date 2015年10月30日 上午9:08:25 
	* @throws 
	*/ 
	public void processSign() {
		IntegrationAPI.signGetScore(getActivity(), false, new GrandScoreListener() {
			@Override
			public void onGrandSuccess(SignScore score) {
				new SignTipDialog(getActivity(), score);
			}

			@Override
			public void onGrandFail() {
				CustomToast.showToast(getActivity(), "已签到，明天再来吧！");
			}
		});
	}
	
}

