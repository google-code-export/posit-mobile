<?php
$LOG_FILE = "../logs/posit_web.txt";
class Log {
	static private $instance;
	static $logfile;
	private function __construct(){
		
		// doesn't need to do anything
		// one logging object to avoid/control future/potential race conditions
	}
	public function getInstance(){
		if(!is_object($instance)) {
			$instance = new Log();
		}
			return $instance;
	}
	/*
	 * log the date and time of the string.
	 * use in moderation
	 */
	public function log($string){
		if (!LOG)
			return;
		$fh = fopen(LOG_FILE, "a+") OR die ("Cannot open log file LOG_FILE");
		$date = date("h-i-s, j-m-y ");
		fwrite($fh, $date." ". $string."\n");
		fflush($fh);
		fclose($fh);
	}
}
?>