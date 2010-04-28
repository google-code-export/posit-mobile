/*
 * File: NativeActivity.java
 * 
 * Copyright (C) 2009 The Humanitarian FOSS Project (http://www.hfoss.org)
 * 
 * This file is part of POSIT, Portable Open Search and Identification Tool.
 *
 * POSIT is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) as published 
 * by the Free Software Foundation; either version 3.0 of the License, or (at
 * your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU LGPL along with this program; 
 * if not visit http://www.gnu.org/licenses/lgpl.html.
 * 
 */
package org.hfoss.posit;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class NativeActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ndk);
        NativeLib lib = new NativeLib();
        TextView tv = (TextView)findViewById(R.id.message);
        tv.setText(lib.helloWorld());
        tv = (TextView)findViewById(R.id.addition);
        int a = 5, b = 4;
        tv.setText(a+" + "+b+" = "+lib.add(a,b));
        Log.i("RESULT","RESULT : "+lib.startRWG());
    }
}
class NativeLib
{
   public native String helloWorld();
   
   public native int add(int a, int b);
   
   public native String startRWG();
   
   static {
       System.loadLibrary("posit");
   }

}