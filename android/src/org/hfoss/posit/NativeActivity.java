/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

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