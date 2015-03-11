package za.jamie.androidffmpegcmdline.ffmpeg;
import java.io.IOException;
import java.io.InputStream;

import android.util.Log;


public class ProcessRunnable implements Runnable {

	private static final String TAG = "FfmpegJob";
	
	private final ProcessBuilder mProcess;
	
	private ProcessListener mListener;
	
	public ProcessRunnable(ProcessBuilder process) {
		mProcess = process;
		
		mListener = new ProcessListener() {
			
			private InputStream stdout;
			private InputStream stderr;
			
			@Override
			public void stdOut(InputStream stream) {
				Log.i(TAG, "stdOut()");
				stdout = stream;
			}
			
			@Override
			public void stdErr(InputStream stream) {
				Log.i(TAG, "stdErr()");
				stderr = stream;
			}
			
			@Override
			public void onExit(int exitCode) {
				Log.i(TAG, "stdout = " + convertStreamToString(stdout));
				Log.i(TAG, "stderr = " + convertStreamToString(stderr));
				Log.i(TAG, "exitCode = " + exitCode);
				
			}
		};
	}
	
	@Override
	public void run() {
		Process proc = null;
		try {
			proc = mProcess.start();
		} catch (IOException e) {
			Log.e(TAG, "IOException starting process", e);
			return;
		}
		
		// Consume the stdout and stderr
		if (mListener != null) {
			mListener.stdOut(proc.getInputStream());
			mListener.stdErr(proc.getErrorStream());
		}
		
		// Wait for process to exit
		int exitCode = 1; // Assume error
		try {
			exitCode = proc.waitFor();
		} catch (InterruptedException e) {
			Log.e(TAG, "Process interrupted!", e);
		}
		
		if (mListener != null) {
			mListener.onExit(exitCode);
		}
	}
	
	public void setProcessListener(ProcessListener listener) {
		mListener = listener;
	}
	
	public interface ProcessListener {
		public void stdOut(InputStream stream);
		public void stdErr(InputStream stream);
		public void onExit(int exitCode);
	}
	
	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
}
