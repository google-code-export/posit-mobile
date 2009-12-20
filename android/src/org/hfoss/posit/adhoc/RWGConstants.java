package org.hfoss.posit.adhoc;

public interface RWGConstants {

	//home directory of Android application
	public final static String POSIT_HOME = "/data/data/org.hfoss.posit/";

	//name of the tor C binary
	public final static String RWG_BINARY_ASSET_KEY = "rwgexec";	
	
	//path to install the Tor binary too
	public final static String RWG_BINARY_INSTALL_PATH = POSIT_HOME + "files/" + RWG_BINARY_ASSET_KEY;
	
	//key of the tor binary in the Zip file
	public final static String RWG_BINARY_ZIP_KEY = "assets/" + RWG_BINARY_ASSET_KEY;
	

	//where to send the notices log
	public final static String RWG_LOG_PATH = POSIT_HOME + "notices.log";
	
	//various console cmds
	public final static String SHELL_CMD_CHMOD = "/system/bin/chmod";
	public final static String SHELL_CMD_KILLALL = "/system/bin/kill";
	public final static String SHELL_CMD_RM = "/system/bin/rm";
	public final static String SHELL_CMD_PS = "ps";
	public final static String CHMOD_EXE_VALUE = "777";
	
	//path of the installed APK file
	public final static String APK_PATH = "/data/app/org.hfoss.posit.apk";
	
	
	public final static int FILE_WRITE_BUFFER_SIZE = 2048;
}
