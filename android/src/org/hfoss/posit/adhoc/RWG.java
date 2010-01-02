/*
 * File: RWG.java
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

package org.hfoss.posit.adhoc;

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
