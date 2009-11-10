package org.hfoss.posit;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;

public class RWG {
	private Context mContext;
	private InputStreamReader input;
	private OutputStreamWriter output;
	private static final String inputPipe = "input"; // "/data/rwg/input";
	private static final String outputPipe = "output"; //"/data/rwg/output";
	private static final String TAG = "RWG";
	
	public RWG(Context c) {
		this.mContext = c;
		try {
			this.input = new InputStreamReader(mContext.openFileInput(inputPipe));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			this.output = new OutputStreamWriter(mContext.openFileOutput(outputPipe, mContext.MODE_APPEND));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write(char[] buf) throws IOException {
		this.output.write(buf);
	}
	
	public int read(char[] buf) throws IOException {
		return this.input.read(buf);
	}
	
	public boolean ready() throws IOException {
		return this.input.ready();
	}
}
