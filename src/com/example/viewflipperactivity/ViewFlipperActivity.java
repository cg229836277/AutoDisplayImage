package com.example.viewflipperactivity;

import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ViewFlipper;

public class ViewFlipperActivity extends Activity{

	private GestureDetector gestureDetector = null;
	private ViewFlipper viewFlipper = null;
	
	private BitmapUtil loadBitmap;
	private final String LOCAL_PICTURE_PATH = Environment.getExternalStorageDirectory() +
			File.separator + "fipper_pictures" + File.separator;
	private final String TAG = "ViewFlipperActivity";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_flipper);
		
		buildImageDir();

		viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
		startFlipPictures();		
	}
	
	public void buildImageDir(){
		File file = new File(LOCAL_PICTURE_PATH);
		if(!file.exists()){
			file.mkdirs();
		}
		loadBitmap = new BitmapUtil(getApplicationContext(), LOCAL_PICTURE_PATH);
	}
	
	public void startFlipPictures(){
		String[] picturesArray = CommonData.PICTURE_URLS;

		for (int i = 0; i < picturesArray.length; i++) { 
			ImageView iv = new ImageView(this);
			
			String url = picturesArray[i];
			String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
			String filePath = LOCAL_PICTURE_PATH + fileName;
			File file = new File(filePath);
			if(file.exists() && file.length() > 0){
				loadBitmap.loadBitmap(filePath, iv, 0, 0);
			}else{			
				loadBitmap.loadBitmap(picturesArray[i], iv, 0, 0);
			}
			iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
			viewFlipper.addView(iv, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}

		viewFlipper.setAutoStart(true);	
		viewFlipper.setFlipInterval(3000);
		startLeftFlipAnimation();
		if(viewFlipper.isAutoStart() && !viewFlipper.isFlipping()){
			viewFlipper.startFlipping();
		}
	}

	boolean isFlipping = false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			isFlipping = viewFlipper.isFlipping();
			break;
			
		case MotionEvent.ACTION_UP:				
			if(isFlipping){
				viewFlipper.stopFlipping();			
				viewFlipper.setAutoStart(false);
			}else{
				viewFlipper.startFlipping();			
				viewFlipper.setAutoStart(true);
				viewFlipper.showNext();
			}
			
			break;
		}
		return false;
//		return gestureDetector.onTouchEvent(event); 
	}
	
	public void startRightFlipAnimation(){
		Animation rInAnim = AnimationUtils.loadAnimation(this, R.anim.push_right_in); 	
		Animation rOutAnim = AnimationUtils.loadAnimation(this, R.anim.push_right_out); 

		viewFlipper.setInAnimation(rInAnim);
		viewFlipper.setOutAnimation(rOutAnim);
		viewFlipper.showPrevious();
	}
	
	public void startLeftFlipAnimation(){
		Animation lInAnim = AnimationUtils.loadAnimation(this, R.anim.push_left_in);	
		Animation lOutAnim = AnimationUtils.loadAnimation(this, R.anim.push_left_out); 	

		viewFlipper.setInAnimation(lInAnim);
		viewFlipper.setOutAnimation(lOutAnim);
		viewFlipper.showNext();
	}
	
	@Override
	protected void onDestroy() {
		if(loadBitmap != null){
			loadBitmap.clearImageCache();
		}
		loadBitmap = null;
		super.onDestroy();
	}
}