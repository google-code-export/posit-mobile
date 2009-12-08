/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/

package org.hfoss.posit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.util.Log;

public class RWGBinaryInstaller implements RWGConstants {

	private static final String TAG = "RWG";
	
	public RWGBinaryInstaller(){}
	
	public void start(boolean force) {
		boolean binaryExists = new File(RWG_BINARY_INSTALL_PATH).exists();
		
		Log.i(TAG, "rwgexec exists? = "+binaryExists);
		
		if(!binaryExists || force)
			installBinary();
	}
	
	private void installBinary() {
		try {
			ZipFile zip = new ZipFile(APK_PATH);
			
			ZipEntry rwg = zip.getEntry(RWG_BINARY_ZIP_KEY);
			streamToFile(zip.getInputStream(rwg),RWG_BINARY_INSTALL_PATH);
		}
		catch(IOException ioe) {
			Log.e(TAG, "unable to pull binary from zip", ioe);
		}
	}
	
	private static void streamToFile(InputStream is, String targetFilename) {
		FileOutputStream fos = null;
		byte[] buffer = new byte[FILE_WRITE_BUFFER_SIZE];
		int byteCount;
		
		File outFile = new File(targetFilename);
		
		try {
			outFile.createNewFile();
			fos = new FileOutputStream(outFile);
			while ((byteCount = is.read(buffer)) > 0) {
                fos.write(buffer, 0, byteCount);
            }
            fos.close();
		}
		catch(IOException ioe) {
			Log.e(TAG, "could not open or write to output file", ioe);
			return;
		}
	}
}
