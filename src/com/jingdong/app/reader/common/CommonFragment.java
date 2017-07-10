package com.jingdong.app.reader.common;


import com.jingdong.app.reader.application.MZBookApplication;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class CommonFragment extends Fragment {

	protected String fragmentTag = "CommonFragment";
	MZBookApplication application=	null;
	
	
	public void CommonFragment() {
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(getActivity()!=null){
			
			application=(MZBookApplication) getActivity().getApplication();
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if(getActivity()!=null){
			
//			NetUtils.netAvailable(getActivity());
			application=(MZBookApplication) getActivity().getApplication();
		}
	}
	public MZBookApplication getMZBookApplication() {
		if(application==null&&getActivity()!=null){
			application=(MZBookApplication) getActivity().getApplication();
		}
		return application;
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}

	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onPrepareOptionsMenu(menu);
	}
	
}
