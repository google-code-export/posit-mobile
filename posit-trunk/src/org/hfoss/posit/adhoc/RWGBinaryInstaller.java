package org.hfoss.posit.adhoc;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.util.Log;

public class RWGBinaryInstaller implements RWGConstants {

	private static final String TAG = "RWGBinaryInstaller";
	
	public RWGBinaryInstaller(){}
	
	public void start(boolean force) {
		boolean binaryExists = new File(RWG_BINARY_INSTALL_PATH).exists();
		
		Log.i(TAG, "rwgexec exists? = "+binaryExists);
		
		if(!binaryExists || force)
			installBinary();
	}
	
	private void createPipes() {
		Runtime r = Runtime.getRuntime();
		DataOutputStream os = null;
		DataInputStream is = null;
		/*
		 * We need to run rwgexec as root, so we get a su shell.
		 * The pipes need to be in the current working directory when rwgexec is
		 * started, so we cd to /data/rwg first.
		 */

        try {
            Process p = r.exec("su");
	        os = new DataOutputStream(p.getOutputStream());
	        is = new DataInputStream(p.getInputStream());
	        os.writeBytes("cd "+POSIT_HOME+"\n");
	        os.writeBytes("chmod 777 /data/rwg/busybox"+"\n");
	        os.writeBytes("./busybox --install\n");
	        os.writeBytes("./busybox mkfifo input\n");
	        os.writeBytes("./busybox mkfifo output\n");
	        os.close();
	        Log.i("INPUT", is.readLine());
	        p.destroy();
	        Log.i("RWGService", "tried to create pipes");
        } catch (Exception e) {
                Log.d("RWG", "Unexpected error - Here is what I know: "+e.getMessage());
        }
	}
	
	private void installBinary() {
		
		try {
			/*Process p = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
	        os.writeBytes("mkdir /data/rwg"+"\n");
	        os.close();
	        p.destroy();*/
	        
			ZipFile zip = new ZipFile(APK_PATH);
			
			ZipEntry rwg = zip.getEntry(RWG_BINARY_ZIP_KEY);
			streamToFile(zip.getInputStream(rwg),RWG_BINARY_INSTALL_PATH);
			
			//ZipEntry busybox = zip.getEntry("assets/busybox");
			//streamToFile(zip.getInputStream(busybox),POSIT_HOME+"files/busybox");
			
			//createPipes();
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
