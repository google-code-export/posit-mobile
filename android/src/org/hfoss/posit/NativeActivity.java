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