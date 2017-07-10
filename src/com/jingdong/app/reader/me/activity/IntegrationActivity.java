package com.jingdong.app.reader.me.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.common.CommonFragmentActivity;
import com.jingdong.app.reader.me.fragment.BackHandledFragment;
import com.jingdong.app.reader.me.fragment.BackHandledInterface;
import com.jingdong.app.reader.me.fragment.ExchangeStatementFragment;
import com.jingdong.app.reader.me.fragment.IntegrationMainFragment;
import com.jingdong.app.reader.me.fragment.IntegrationMainFragment.SwitchPageListener;
import com.jingdong.app.reader.me.fragment.IntegrationRecordFrament;
import com.jingdong.app.reader.view.SearchTopBarView.TopBarViewListener;

/**
 * 积分页面
 * @author J.Beyond
 *
 */
public class IntegrationActivity extends CommonFragmentActivity implements TopBarViewListener,SwitchPageListener,BackHandledInterface{
	
	public static final String UrlKey = "UrlKey";
	public static final String NickName = "nickName";
	private FragmentManager mFramgmentMgr;
	private BackHandledFragment mBackHandedFragment;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.integration_root);
		String imgUrl = getIntent().getStringExtra(UrlKey);
		String nickName = getIntent().getStringExtra(NickName);
		//切换到主Fragment
		IntegrationMainFragment main = new IntegrationMainFragment();
		Bundle b = new Bundle();
		b.putString("HEADER_IMAGE_URL", imgUrl);
		b.putString("HEADER_NICK_NAME", nickName);
		main.setArguments(b);
		
		switchFragment(main,"main");
	}
	
	private void switchFragment(Fragment fragment,String tag){
		mFramgmentMgr = getSupportFragmentManager();
		FragmentTransaction ts = mFramgmentMgr.beginTransaction();
		ts.replace(R.id.integration_root_fl, fragment,tag);
		ts.addToBackStack(tag);
		ts.commit();
	}

	@Override
	public void onLeftMenuClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRightMenu_leftClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRightMenu_rightClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCenterMenuItemClick(int position) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void switchToRecord() {
		IntegrationRecordFrament recordFragment = new IntegrationRecordFrament();
		switchFragment(recordFragment,"record");
	}

	@Override
	public void switchToExchangeInfo() {
		ExchangeStatementFragment statementFragment = new ExchangeStatementFragment();
		switchFragment(statementFragment, "statement");
	}
	
	@Override
	public void onBackPressed() {
		if(mBackHandedFragment == null || !mBackHandedFragment.onBackPressed()){
			if(getSupportFragmentManager().getBackStackEntryCount() == 1){
//				super.onBackPressed();
				finish();
			}else{
				getSupportFragmentManager().popBackStack();
			}
		}
	}

	@Override
	public void setSelectedFragment(BackHandledFragment selectedFragment) {
		this.mBackHandedFragment = selectedFragment;
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_integral_manage));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_integral_manage));
	}
	

}
