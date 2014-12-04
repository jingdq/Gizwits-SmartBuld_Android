package com.xpg.zxing;

import java.io.IOException;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.xpg.appbase.R;
import com.xpg.appbase.activity.BaseActivity;
import com.xpg.appbase.activity.device.DeviceListActivity;
import com.xpg.zxing.camera.CameraManager;
import com.xpg.zxing.decoding.CaptureActivityHandler;
import com.xpg.zxing.decoding.InactivityTimer;
import com.xpg.zxing.view.ViewfinderView;

public class CaptureActivity extends BaseActivity implements Callback {

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
//	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zxing_layout);
		CameraManager.init(getApplication());
		getActionBar().hide();
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		vibrate = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	public void handleDecode(Result obj, Bitmap barcode) {
		String text = obj.getText();
		Log.i("test", text);
		if(text.contains("product_key=")&text.contains("did=")&&text.contains("passcode=")){
			
			inactivityTimer.onActivity();
			viewfinderView.drawResultBitmap(barcode);
			String product_key = getParamFomeUrl(text,"product_key");
			String did= getParamFomeUrl(text, "did");
			String passcode = getParamFomeUrl(text, "passcode");
			Log.i("passcode product_key did", passcode + " " + product_key + " " + did);
			Intent it = new Intent();
//			it.setClass(this, DeviceListActivity.class);
//			it.putExtra("passcode", passcode);
//			it.putExtra("product_key", product_key);
//			it.putExtra("did", did);
//			startActivity(it);
			//TODO 执行绑定
			
			finish();
			
		}else{
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
		
		 
	}

	private String getParamFomeUrl(String url,String param) {
		String product_key = "";
		int startindex = url.indexOf(param+"=");
		startindex+=(param.length()+1);
		String subString = url.substring(startindex);
		int endindex = subString.indexOf("&");
		if(endindex==-1){
			product_key = subString;
		}else{
			product_key = subString.substring(0,endindex);
		}
		return product_key;
	}

}