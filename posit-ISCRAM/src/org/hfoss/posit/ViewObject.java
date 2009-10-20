/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     pgautam - initial API and implementation
 ******************************************************************************/
package org.hfoss.posit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Paint;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
/**
 * A class to generate objects from the provided parameters like name, type and if it
 * has any extra features like barcode scanning.
 * @author pgautam
 *
 */
public class ViewObject {
	private static final String BOOL = "bool";
	private static final Object DOUBLE = "double";
	private static final String INTEGER = "integer";
	private static final String OPTIONS = "options";
	private static final String SELECTOR = "selector";
	private static final String TAG = "ViewObject";
	private static final String TEXT = "text";

	// We should be able to fit the text TERMINATOR.
	// at least better than eyeballing, better way would be to do the same for
	// max length text in the view Objects
	private static final int WIDTH_TEXTVIEW = (int) Math.ceil((new Paint())
			.measureText("TERMINATOR"));
	private static final String LONG = "long";
	private static final String FLOAT = "float";

	
	/*
	 * FIELDS
	 */
	private String name, type;
	private List<String> extras = new ArrayList<String>();
	private View view;
	private Button button;
	private boolean hasExtras=false;
	private ArrayList<String> spinnerItems; /* this is for binding the spinner to this arrayResId*/
	private Context mContext;
	/**
	 * Creates a View Object with the name and type. The View itself isn't generated yet.
	 * @param name
	 * @param type
	 */
	public ViewObject(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	public ViewObject (String name, String type, String extras) {
		this.name = name;
		this.type = type;
		this.extras = Arrays.asList(extras.split(","));
		hasExtras = true;
	}
	
	/**
	 * Get all the keys from a given set of ViewObjects This can be used as
	 * projection for mapping items from database.
	 * 
	 * @param items
	 * @return
	 */
	public static String[] getAllItemNames(ArrayList<ViewObject> items) {
		String[] names = new String[items.size()];
		for (int i = 0; i < items.size(); i++) {
			names[i] = items.get(i).getName();
		}
		return names;
	}
	
	/**
	 * return ContentValues for all the ViewObjects supplied. useful for saving
	 * to database.
	 * 
	 * @param items
	 * @return
	 */
	public static ContentValues getContentValuesFromViewObjects(ArrayList<ViewObject> items) {
		ContentValues args = new ContentValues();

		for (ViewObject item : items) {
			String type = item.getType();
			Object value = item.getValue();
			if (type.equals(INTEGER) || type.equals(BOOL) || isOptions(type)) {
				args.put(item.getName(), (Integer) value);
			} else if (type.equals(DOUBLE)) {
				args.put(item.getName(), (Double) value);
			} else if (type.equals(LONG)) {
				args.put(item.getName(), (Long) value);
			}else {
				args.put(item.getName(), (String) value);
			}
		}
		return args;
	}
	
	public static void saveStateForViewObjects(ArrayList<ViewObject> items,SharedPreferences mPreferences) {
		for (ViewObject viewObject : items) {
			viewObject.saveState(mPreferences);
		}
		mPreferences.edit().commit();
	}
	
	public static void putValuesFromPreferences(ArrayList<ViewObject> items,SharedPreferences mPreferences) {
		for (ViewObject viewObject : items) {
		}
	}
	/**
	 * simple way to check if the type has options in the text
	 * 
	 * @param type
	 * @return
	 */
	private static boolean isOptions(String type) {
		return type.startsWith(OPTIONS);
	}
	/**
	 * simple way to check if the type has options in the text
	 * 
	 * @param type
	 * @return
	 */
	private static boolean isSelector(String type) {
		return type.startsWith(SELECTOR);
	}
	
	/**
	 * Check if the type is one of the number types this is applicable to
	 * Editable type inputs
	 * 
	 * @param value
	 * @return
	 */
	private Object checkNumtype(String value) {
		
		
		if (type.equals(INTEGER)) {
			if (StringUtils.isBlank(value)) return 0;
			return Integer.parseInt(value);
		} else if (type.equals(DOUBLE)) {
			if (StringUtils.isBlank(value)) return 0D;
			return Double.parseDouble(value);
		} else if (type.equals(LONG)) {
			if (StringUtils.isBlank(value)) return 0L;
			return Long.parseLong(value); 
		}else {
			return value;
		}
	}
	
	
	
	/**
	 * Saves the preference of the ViewObject
	 * it doesn't commit anything to memory
	 * @param mPreferences
	 */
	public void saveState(SharedPreferences mPreferences) {
		SharedPreferences.Editor editor = mPreferences.edit();
		if (type.equals(INTEGER))
			editor.putInt(name, (Integer)getValue());
		else if (type.equals(DOUBLE)||type.equals(FLOAT))
			editor.putFloat(name, (Float)getValue());
		else if (type.equals(LONG))
			editor.putLong(name, (Long)getValue());
		else if (type.equals(BOOL))
			editor.putBoolean(name, (Integer)getValue()==1?true:false);
		else  //if all else fails, its got to be a string
			editor.putString(name, (String)getValue());
	}
	
	/**
	 * Generates Layout that can be edited.
	 * @param mContext
	 * @return
	 */
	public LinearLayout generateEditableLayout(Context mContext) {
		this.mContext = mContext;
		LinearLayout l = new LinearLayout(mContext);
		int widthLayout = LayoutParams.FILL_PARENT;
		l.setLayoutParams(new LayoutParams(widthLayout,
				LayoutParams.WRAP_CONTENT));
		l.setOrientation(LinearLayout.HORIZONTAL);
		TextView textView = new TextView(mContext);
		textView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		textView.setSingleLine();
		textView.setWidth(WIDTH_TEXTVIEW);
		textView.setText(name);
		l.addView(textView);
		if (hasExtras) {
			Log.w(TAG, "here");
			widthLayout = LayoutParams.WRAP_CONTENT;
		}
		if (type.equals(TEXT)) {
			EditText editView = new EditText(mContext);
			editView.setLayoutParams(new LayoutParams(widthLayout,
					LayoutParams.WRAP_CONTENT));
			l.addView(editView);
			view = editView;
		} else if (type.equals(INTEGER)||type.equals(LONG)) {
			EditText editView = new EditText(mContext);
			editView.setLayoutParams(new LayoutParams(widthLayout,
					LayoutParams.WRAP_CONTENT));
			// editView.setFilters(new InputFilter[] { new });
			editView.setKeyListener(new DigitsKeyListener());
			l.addView(editView);
			view = editView;
		} else if (type.equals(BOOL)) {
			CheckBox checkBox = new CheckBox(mContext);
			checkBox.setLayoutParams(new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			l.addView(checkBox);
			view = checkBox;
		} else if (isOptions(type)) {
			/*
			 * checks for type options-x1,x2 and creates a radio group with x1,
			 * x2 radio buttons
			 */
			String options = type.substring(8);
			String[] items = options.split(",");
			RadioGroup rg = new RadioGroup(mContext);
			rg.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			rg.setOrientation(LinearLayout.HORIZONTAL);
			int i = 0;
			for (String item : items) {
				Log.i(TAG, item);
				RadioButton rb = new RadioButton(mContext);
				rb.setId(i);
				i++;
				rb.setText(item);
				rg.addView(rb);
			}
			l.addView(rg);
			view = rg;

		} else if (isSelector(type)) {
			/*
			 * checks for type selector-@arrayname and loads the content from
			 * arrayname to a Spinner
			 */
			Resources res = mContext.getResources();
			Spinner s = new Spinner(mContext);
			s.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT));
			int resId = res.getIdentifier("org.hfoss.posit:array/"
					+ type.substring(10), null, null);
//			ArrayAdapter<CharSequence> adapter = ArrayAdapter
//					.createFromResource(mContext, arrayResId,
//							android.R.layout.simple_spinner_item);
			/*Explanation, this allows us to anticipate new values since selectors save strings */
			spinnerItems = new ArrayList<String>(Arrays.asList(res.getStringArray(resId))); 
			Utils.bindArrayListToSpinner(mContext, s,spinnerItems);
			l.addView(s);
			view = s;
		}
		if (extras.contains("barcode")){
			Log.w(TAG, "here");
			button = new Button(mContext);
			button.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
			button.setText("Barcode");
			l.addView(button);
		}
		return l;
	}

	public void setOnButtonClickListener (OnClickListener l) {
		button.setOnClickListener(l);
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	public Object getValue() {
		/*
		 * note: CheckBox has to be checked first, otherwise checkbox will be of
		 * some other type
		 */
		if (view instanceof CheckBox) {
			return (((CheckBox) view).isChecked()) ? 1 : 0; //this is so that the value can be passed directly to database, kind of
		} else if (view instanceof TextView) {
			String value = ((TextView) view).getText().toString();
			return checkNumtype(value);
		} else if (view instanceof EditText) {
			String value = ((EditText) view).getText().toString();
			return checkNumtype(value);
		} else if (view instanceof RadioGroup)
			return ((RadioGroup) view).getCheckedRadioButtonId();
		else if (view instanceof Spinner)
			return ((Spinner) view).getSelectedItem();
		else
			return -1;

	}

	/**
	 * the name to set
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 	 
	 * the type to set
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}
	public void setValue(Context mContext, Intent data) {
		if (extras.contains("barcode")) {
			String val = data.getStringExtra("SCAN_RESULT");
			setValue(val);
		}
	}
	/**
	 * TODO any wierd input throws ClassCastException. Fix that in a cleaner way.
	 * FIXME there has to be a better way for getting position for Spinners based on String.
	 * @param value
	 */
	public void setValue(String value) {
		
		if (view instanceof CheckBox) {
			if (value==null) value=""+0;
			((CheckBox) view).setChecked(
					(Integer.parseInt(value)==1?true:false));
		
		}else if (view instanceof RadioGroup) {
			if (value==null) value=""+-1;
			((RadioGroup)view).check(Integer.parseInt(value));
		}
		else if (view instanceof TextView) {
			if (value==null) value="";
			((TextView) view).setText(value.toString());
		}else if (view instanceof EditText) {
			if (value==null) value="";
			((EditText) view).setText(value.toString());
		}else if (view instanceof Spinner) {
			int position = spinnerItems.indexOf(value.toString());
			if (position==-1 && value!=null) {
				spinnerItems.add(value);
				Utils.bindArrayListToSpinner(mContext, (Spinner)view, spinnerItems);
			}
			((Spinner)view).setSelection(position);
		}
	}
	/**
	 * resets each of the views
	 */
	public void reset() {
		
		if (view instanceof CheckBox) {((CheckBox) view).setChecked(false);}
		else if (view instanceof TextView) ((TextView) view).setText("");
		else if (view instanceof EditText) ((EditText) view).setText("");
		else if (view instanceof RadioGroup) ((RadioGroup)view).check(-1);
		else if (view instanceof Spinner)((Spinner)view).setSelection(0);
	}
	public Intent getIntent() {
		if (extras.contains("barcode"))
			return new Intent("com.google.zxing.client.android.SCAN");
		else return null;
	}

	public boolean hasExtras() {
		return hasExtras;
	}
}
