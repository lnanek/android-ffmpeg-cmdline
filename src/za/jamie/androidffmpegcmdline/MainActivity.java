package za.jamie.androidffmpegcmdline;

import java.io.File;
import java.io.IOException;

import za.jamie.androidffmpegcmdline.ffmpeg.FfmpegJob;
import za.jamie.androidffmpegcmdline.ffmpeg.Utils;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "MainActivity";
	
	private static final boolean REPLACE_EXISTING_FFMPEG = true;
	
	private EditText mInputFilepath;
	private EditText mOutputFilename;
	
	private CheckBox mEnableVideo;	
	private EditText mVideoCodec;
	private EditText mWidth;
	private EditText mHeight;
	private EditText mVideoBitrate;
	private EditText mFrameRate;
	private EditText mVideoFilter;
	private EditText mVideoBitStreamFilter;
	private ViewGroup mVideoFields;
	
	private CheckBox mEnableAudio;
	private EditText mAudioCodec;	
	private EditText mChannels;	
	private EditText mAudioBitrate;	
	private EditText mSampleRate;	
	private EditText mAudioFilter;	
	private EditText mAudioBitStreamFilter;
	private ViewGroup mAudioFields;
	
	private Button mStartButton;
	
	private String mFfmpegInstallPath;
	private SharedPreferences mSharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		setContentView(R.layout.activity_main);
		
		findViews();
		
		initCheckBoxes();
		
		installFfmpeg();
		
		mStartButton.setOnClickListener(this);
		
		mEnableAudio.setChecked(false);
	}
	
	private void findViews() {
		mInputFilepath = (EditText) findViewById(R.id.editText1);
		mInputFilepath.setText(mSharedPreferences.getString("mInputFilepath", ""));
		
		mOutputFilename = (EditText) findViewById(R.id.editText2);
		mOutputFilename.setText(mSharedPreferences.getString("mOutputFilename", ""));
				
		mVideoCodec = (EditText) findViewById(R.id.editText3);
		mVideoCodec.setText(mSharedPreferences.getString("mVideoCodec", ""));
		
		mWidth = (EditText) findViewById(R.id.editText5);
		mWidth.setText(mSharedPreferences.getString("mWidth", ""));
		
		mHeight = (EditText) findViewById(R.id.editText6);
		mHeight.setText(mSharedPreferences.getString("mHeight", ""));
		
		mVideoBitrate = (EditText) findViewById(R.id.editText8);
		mVideoBitrate.setText(mSharedPreferences.getString("mVideoBitrate", ""));
		mFrameRate = (EditText) findViewById(R.id.editText10);
		mFrameRate.setText(mSharedPreferences.getString("mFrameRate", ""));
		mVideoFilter = (EditText) findViewById(R.id.editText12);
		mVideoFilter.setText(mSharedPreferences.getString("mVideoFilter", ""));
		mVideoBitStreamFilter = (EditText) findViewById(R.id.editText14);
		mVideoBitStreamFilter.setText(mSharedPreferences.getString("mVideoBitStreamFilter", ""));
		
		mAudioCodec = (EditText) findViewById(R.id.editText4);	
		mAudioCodec.setText(mSharedPreferences.getString("mAudioCodec", ""));
		
		mChannels = (EditText) findViewById(R.id.editText7);	
		mChannels.setText(mSharedPreferences.getString("mChannels", ""));

		mAudioBitrate = (EditText) findViewById(R.id.editText9);	
		mAudioBitrate.setText(mSharedPreferences.getString("mAudioBitrate", ""));
		
		mSampleRate = (EditText) findViewById(R.id.editText11);
		mSampleRate.setText(mSharedPreferences.getString("mSampleRate", ""));
		
		mAudioFilter = (EditText) findViewById(R.id.editText13);
		mAudioFilter.setText(mSharedPreferences.getString("mAudioFilter", ""));
		
		mAudioBitStreamFilter = (EditText) findViewById(R.id.editText15);
		mAudioBitStreamFilter.setText(mSharedPreferences.getString("mAudioBitStreamFilter", ""));

		mEnableAudio = (CheckBox) findViewById(R.id.checkBox2);
		mEnableVideo = (CheckBox) findViewById(R.id.checkBox1);	
		
		mVideoFields = (ViewGroup) findViewById(R.id.videoFields);
		mAudioFields = (ViewGroup) findViewById(R.id.audioFields);
		
		mStartButton = (Button) findViewById(R.id.button1);
	}
	
	private void initCheckBoxes() {		
		mEnableVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int visibility = isChecked ? ViewGroup.VISIBLE : ViewGroup.INVISIBLE;
				mVideoFields.setVisibility(visibility);
				
			}
		});
		
		mEnableAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int visibility = isChecked ? ViewGroup.VISIBLE : ViewGroup.INVISIBLE;
				mAudioFields.setVisibility(visibility);
				
			}
		});
		
		mEnableVideo.setChecked(true);
		mEnableAudio.setChecked(true);
	}
	
	private void installFfmpeg() {
		File ffmpegFile = new File(getCacheDir(), "ffmpeg");
		mFfmpegInstallPath = ffmpegFile.toString();
		Log.d(TAG, "ffmpeg install path: " + mFfmpegInstallPath);
		
		if (ffmpegFile.exists() && REPLACE_EXISTING_FFMPEG) {
			Log.i(TAG, "Removing previous ffmpeg install");
			ffmpegFile.delete();
		}
		
		if (!ffmpegFile.exists()) {
			try {
				ffmpegFile.createNewFile();
			} catch (IOException e) {
				Log.e(TAG, "Failed to create new file!", e);
			}
			Utils.installBinaryFromRaw(this, 
					getFfmpegBinaryRawResourceId(), ffmpegFile);
		}
		
		ffmpegFile.setExecutable(true);
	}
	
	private int getFfmpegBinaryRawResourceId() {
		// Check preferred arch (e.g. armeabi-v7a)
		if ( Build.CPU_ABI.toUpperCase().startsWith("ARM") ) {
			Log.i(TAG, "using ARM ffmpeg");
			return R.raw.ffmpeg_arm;
		}		
		if ( Build.CPU_ABI.toUpperCase().startsWith("X86") || 
				Build.CPU_ABI.toUpperCase().startsWith("I686") ) {
			Log.i(TAG, "using x86 ffmpeg");
			return R.raw.ffmpeg_x86;
		}

		// Check supported arch (e.g. armeabi)
		if ( Build.CPU_ABI2.toUpperCase().startsWith("ARM") ) {
			Log.i(TAG, "using ARM ffmpeg");
			return R.raw.ffmpeg_arm;
		}		
		if ( Build.CPU_ABI2.toUpperCase().startsWith("X86") || 
				Build.CPU_ABI2.toUpperCase().startsWith("I686") ) {
			Log.i(TAG, "using x86 ffmpeg");
			return R.raw.ffmpeg_x86;
		}
		
		throw new RuntimeException("arch not supported: " + Build.CPU_ABI + "/" + Build.CPU_ABI2);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onClick(View v) {
		final FfmpegJob job = new FfmpegJob(mFfmpegInstallPath);
		loadJob(job);		
		
		final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading", "Please wait.", 
				true, false);
		
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... arg0) {
				job.create().run();
				return null;
			}
			
			@Override
			protected void onPostExecute(Void result) {
				progressDialog.dismiss();
				Toast.makeText(MainActivity.this, "Ffmpeg job complete.", Toast.LENGTH_SHORT).show();
			}
			
		}.execute();
	}
	
	private void loadJob(FfmpegJob job) {
		
		final Editor edit = mSharedPreferences.edit();
		
		job.inputPath = mInputFilepath.getText().toString();		
		edit.putString("mInputFilepath", mInputFilepath.getText().toString());
		
		job.outputPath = mOutputFilename.getText().toString();
		edit.putString("mOutputFilename", mOutputFilename.getText().toString());
		
		job.disableVideo = !mEnableVideo.isChecked();	
		
		job.videoCodec = mVideoCodec.getText().toString();
		edit.putString("mVideoCodec", mVideoCodec.getText().toString());
		
		if (!mWidth.getText().toString().isEmpty() && 
				!mHeight.getText().toString().isEmpty()) {
			
			job.videoWidth = Integer.parseInt(mWidth.getText().toString());
			edit.putString("mWidth", mWidth.getText().toString());
			
			job.videoHeight = Integer.parseInt(mHeight.getText().toString());
			edit.putString("mHeight", mHeight.getText().toString());
			
		} else {
			edit.remove("mWidth");		
			edit.remove("mHeight");			
		}
		
		if (!mVideoFilter.getText().toString().isEmpty()) {
			job.videoBitrate = Integer.parseInt(mVideoBitrate.getText().toString());
			edit.putString("mVideoBitrate", mVideoBitrate.getText().toString());
		} else {
			edit.remove("mVideoBitrate");			
		}
		
		if (!mFrameRate.getText().toString().isEmpty()) {
			job.videoFramerate = Float.parseFloat(mFrameRate.getText().toString());
			edit.putString("mFrameRate", mFrameRate.getText().toString());
		} else {
			edit.remove("mFrameRate");			
		}
		
		job.videoFilter = mVideoFilter.getText().toString();
		edit.putString("mVideoFilter", mVideoFilter.getText().toString());

		job.videoBitStreamFilter = mVideoBitStreamFilter.getText().toString();
		edit.putString("mVideoBitStreamFilter", mVideoBitStreamFilter.getText().toString());
		
		job.disableAudio = !mEnableAudio.isChecked();
		
		job.audioCodec = mAudioCodec.getText().toString();	
		edit.putString("mAudioCodec", mAudioCodec.getText().toString());
		
		if (!mChannels.getText().toString().isEmpty()) {
			job.audioChannels = Integer.parseInt(mChannels.getText().toString());
			edit.putString("mChannels", mChannels.getText().toString());
		} else {
			edit.remove("mChannels");			
		}
		
		if (!mAudioBitrate.getText().toString().isEmpty()) {
			job.audioBitrate = Integer.parseInt(mAudioBitrate.getText().toString());
			edit.putString("mAudioBitrate", mAudioBitrate.getText().toString());
		} else {
			edit.remove("mAudioBitrate");			
		}
		
		if (!mSampleRate.getText().toString().isEmpty()) {
			job.audioSampleRate = Integer.parseInt(mSampleRate.getText().toString());
			edit.putString("mSampleRate", mSampleRate.getText().toString());
		} else {
			edit.remove("mSampleRate");
		}
		
		job.audioFilter = mAudioFilter.getText().toString();
		edit.putString("mAudioFilter", mAudioFilter.getText().toString());
		
		job.audioBitStreamFilter = mAudioBitStreamFilter.getText().toString();
		edit.putString("mAudioBitStreamFilter", mAudioBitStreamFilter.getText().toString());
		
		edit.commit();
	}

}
