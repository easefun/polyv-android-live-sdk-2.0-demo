package com.easefun.polyvsdk.live.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class PolyvTabVPFragmentAdapter extends FragmentPagerAdapter {
	List<Fragment> fragmentList = new ArrayList<Fragment>();

	public PolyvTabVPFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	public PolyvTabVPFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList) {
		super(fm);
		this.fragmentList = fragmentList;
	}

	@Override
	public Fragment getItem(int position) {
		return fragmentList.get(position);
	}

	@Override
	public int getCount() {
		return fragmentList.size();
	}

}
