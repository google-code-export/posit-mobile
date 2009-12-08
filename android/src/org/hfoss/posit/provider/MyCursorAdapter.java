package org.hfoss.posit.provider;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

public class MyCursorAdapter extends SimpleCursorAdapter {


	public MyCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to) {
		super(context, layout, c, from, to);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void bindView(View arg0, Context arg1, Cursor arg2) {
		super.bindView(arg0,arg1,arg2);
	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addTags(String[] tags, int[] views) {
		
	}
}
