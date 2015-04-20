package com.example.viewflipperactivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;

/**
 * @Title：FashionDIY
 * @Description：bitmap的处理函数，避免处理的照片过大出现OOM
 * @date 2014-11-5 下午3:06:00
 * @author chengang
 * @version 1.0
 */

public class BitmapUtil {

	private static String TAG = "BitmapUtil";
	//图片下载到本地的地址
	static String mLocalPicturePath = null;
	// 内存缓存
	private LruCache<String, Bitmap> mMemoryCache;
	// 显示�?大可用内存的大小
	final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

	// 限制�?大内存的八分之一为缓存内�?
	final int cacheSize = maxMemory / 8;
	
	private static Context mContext;

	/**
	 * 工具的构造函�?
	 * @param context 上下�?
	 * @param localFilePath  图片存储在本地的路径，为空的话表示不下载仅显�?
	 */
	public BitmapUtil(Context context , String localFilePath) {
		
		mContext = context;
		mLocalPicturePath = localFilePath;
		
		mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount() / 1024;
			}
		};
	}

	/**
	 * 解码对应的图片资�?
	 * 
	 * @author chengang
	 * @date 2014-11-5 下午3:17:09
	 * @param photoPath 要处理的照片的绝对路径（sd和drawable中的图片�?
	 * @param reqWidth 返回照片的宽�?
	 * @param reqHeight 返回照片的高�?
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromResource(Object photoPath,int reqWidth, int reqHeight) {
		// 通过设置inJustDecodeBounds=true解码，获取照片的尺寸
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		if (photoPath instanceof String) {
			BitmapFactory.decodeFile((String) photoPath, options);
		} else {
			BitmapFactory.decodeResource(mContext.getResources(),(Integer) photoPath, options);
		}
		
		if(reqHeight == 0 && reqWidth == 0){
			options.inSampleSize = 1;
		}else{
			options.inSampleSize = calculateInSampleSize(options, reqWidth,reqHeight);
		}

		// 通过inSampleSize的大小返回对应处理后的图片大�?
		options.inJustDecodeBounds = false;
		if (photoPath instanceof String) {
			return BitmapFactory.decodeFile((String) photoPath, options);
		} else {
			return BitmapFactory.decodeResource(mContext.getResources(),(Integer) photoPath, options);
		}
	}

	public static Bitmap decodeBitmapFromByteData(byte[] data, int resW,int resH) {
		// 通过设置inJustDecodeBounds=true解码，获取照片的尺寸
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		if (data != null && data.length > 0) {
			BitmapFactory.decodeByteArray(data, 0, data.length, options);
		}

		options.inSampleSize = calculateInSampleSize(options, resW, resH);

		// 通过inSampleSize的大小返回对应处理后的图片大�?
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(data, 0, data.length, options);
	}

	/**
	 * 计算InSampleSize大小
	 * 
	 * @author chengang
	 * @date 2014-11-5 下午3:21:24
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	private static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}

		return inSampleSize;
	}

	/**
	 * 解码对应的照片输入流
	 * 
	 * @author chengang
	 * @date 2014-11-5 下午3:20:41
	 * @param is 要处理的照片的流
	 * @param reqWidth 返回照片的宽�?
	 * @param reqHeight 返回照片的高�?
	 * @return
	 */
	public static Bitmap decodeSampledBitmapFromResource(InputStream inputStream, int reqWidth, int reqHeight) {
		byte[] byteArr = new byte[0];
		byte[] buffer = new byte[1024];
		int len;
		int count = 0;

		try {
			while ((len = inputStream.read(buffer)) > -1) {
				if (len != 0) {
					if (count + len > byteArr.length) {
						byte[] newbuf = new byte[(count + len) * 2];
						System.arraycopy(byteArr, 0, newbuf, 0, count);
						byteArr = newbuf;
					}

					System.arraycopy(buffer, 0, byteArr, count, len);
					count += len;
				}
			}

			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(byteArr, 0, count, options);

			options.inSampleSize = calculateInSampleSize(options, reqWidth,reqHeight);
			options.inJustDecodeBounds = false;
			options.inPreferredConfig = Bitmap.Config.RGB_565;
			options.inDither = true;

			return BitmapFactory.decodeByteArray(byteArr, 0, count, options);

		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	/**
	 * 加载图片,类型包括(网络图片，sd卡图片，res图片)
	 * 如果设置宽度和高度为0，那么按照原图片大小加载
	 * @author admin
	 * @date 2015-4-17 下午3:12:44
	 * @param resPath 图片的路径（网络下载地址，sd卡图片路径，本地res文件的id�?
	 * @param imageView 要显示的控件
	 * @param resWid 要显示图片的宽度
	 * @param resHei 要显示图片的高度
	 */
	public void loadBitmap(Object resPath, ImageView imageView, int resWid , int resHei) {
		Bitmap bitmap = null;
		if (resPath instanceof String) {
			bitmap = getBitmapFromMemCache((String) resPath);
			if (bitmap == null) {
				startAsyncTask(resPath, imageView, resWid, resHei);
			} else {
				loadCacheBitmap(resPath, imageView, bitmap);
			}
		} else {
			startAsyncTask(resPath, imageView, resWid, resHei);
		}
	}
	
	/**
	 * 下载并且显示网络图片
	 * 
	 * @author admin
	 * @date 2015-4-17 上午10:31:45
	 * @param imageUrl
	 * @param imageView
	 * @param width
	 * @param height
	 */
	public void showAndDownloadNetworkImage(String imageUrl , ImageView imageView , int width , int height){
		if(StringUtils.isEmpty(imageUrl)){
			return;
		}			
		
		Bitmap bitmap = null;
		
		bitmap = getBitmapFromMemCache(imageUrl);
		if (bitmap == null) {
			startAsyncTask(imageUrl, imageView, width, height);
		} else {
			loadCacheBitmap(imageUrl, imageView, bitmap);
		}
	}

	/**
	 * �?始异步任�?
	 * 
	 * @author admin
	 * @date 2015-4-17 下午3:24:26
	 * @param resPath
	 * @param imageView
	 * @param resWid
	 * @param resHei
	 */
	private void startAsyncTask(Object resPath, ImageView imageView, int resWid,int resHei) {
		if (cancelPotentialWork(resPath, imageView)) {
			final BitmapWorkerTask task = new BitmapWorkerTask(imageView,resWid, resHei);
			Resources resource = mContext.getResources();
			Bitmap defaultBimap = BitmapFactory.decodeStream(resource.openRawResource(R.drawable.ic_launcher));
			final AsyncDrawable asyncDrawable = new AsyncDrawable(resource,defaultBimap, task);
			imageView.setImageDrawable(asyncDrawable);
			imageView.setTag(resPath);
			task.execute(resPath);
		}
	}

	/**
	 * 缓存存在的话，加载缓存图�?
	 * 
	 * @author admin
	 * @date 2015-4-17 下午3:24:40
	 * @param resPath
	 * @param imageView
	 * @param cacheBitmap
	 */
	private void loadCacheBitmap(Object resPath, ImageView imageView,Bitmap cacheBitmap) {
		imageView.setVisibility(View.VISIBLE);
		cancelPotentialWork(resPath, imageView);
		imageView.setTag(resPath);
		imageView.setImageBitmap(cacheBitmap);
	}

	private static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap,BitmapWorkerTask bitmapWorkerTask) {
			super(res, bitmap);
			bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
		}

		public BitmapWorkerTask getBitmapWorkerTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	public static boolean cancelPotentialWork(Object resSource,ImageView imageView) {
		BitmapWorkerTask bitmapDownloaderTask = getBitmapWorkerTask(imageView);

		if (bitmapDownloaderTask != null) {
			if (resSource instanceof String) {
				String bitmapPath = bitmapDownloaderTask.resPath;
				if ((bitmapPath == null)|| (!bitmapPath.equals((String) resSource))) {
					bitmapDownloaderTask.cancel(true);
				} else {
					// The same URL is already being downloaded.
					return false;
				}
			} else if (resSource instanceof Integer) {
				final int bitmapData = bitmapDownloaderTask.drawableId;
				int drawableId = (Integer) resSource;
				if (bitmapData != drawableId) {
					// Cancel previous task
					bitmapDownloaderTask.cancel(true);
				} else {
					// The same work is already in progress
					return false;
				}
			}
		}
		return true;
	}

	private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
		if (imageView != null) {
			final Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
				return asyncDrawable.getBitmapWorkerTask();
			}
		}
		return null;
	}

	class BitmapWorkerTask extends AsyncTask<Object, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private String resPath;
		private int drawableId;
		private int mResWid = 0;
		private int mResHei = 0;

		public BitmapWorkerTask(ImageView imageView, int resWid, int resHei) {
			// Use a WeakReference to ensure the ImageView can be garbage collected
			imageViewReference = new WeakReference<ImageView>(imageView);
			mResWid = resWid;
			mResHei = resHei;
		}
		
		@Override
		protected Bitmap doInBackground(Object... arg0) {
			if (arg0 == null) {
				return null;
			}
			
			Bitmap currentBitmap = null;
			if (arg0[0] instanceof String) {
				resPath = (String) arg0[0];
				if(URLUtil.isNetworkUrl(resPath)){
					currentBitmap = downloadBitmap(resPath);
				}else{
					currentBitmap = decodeSampledBitmapFromResource(resPath,mResWid, mResHei);
				}
			} else {
				drawableId = (Integer) arg0[0];
				currentBitmap = decodeSampledBitmapFromResource(drawableId, mResWid, mResHei);
			}
			return currentBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled()) {
				bitmap = null;
				Log.e(TAG, "取消加载�?");
			}

			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
				if (this == bitmapWorkerTask && imageView != null) {
					if (imageView.getTag() instanceof String) {
						addBitmapToMemoryCache(resPath, bitmap);// 添加到缓�?
						if (resPath.equals((String) imageView.getTag())) {
							imageView.setImageBitmap(bitmap);
						}
					} else {
						if (drawableId == ((Integer) imageView.getTag())) {
							imageView.setImageBitmap(bitmap);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 下载网络上的图片
	 * 
	 * @author admin
	 * @date 2015-4-17 下午3:25:18
	 * @param url
	 * @return
	 */
	private static Bitmap downloadBitmap(String url) {	       
		Bitmap bitmapImage;
		URL imageUrl = null;
		try {
			imageUrl = new URL(url);

			HttpGet httpRequest = null;
			httpRequest = new HttpGet(imageUrl.toURI());

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse response = (HttpResponse) httpclient.execute(httpRequest);

			final HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = null;
				try {
					inputStream = entity.getContent();
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inSampleSize = 2;
					bitmapImage = BitmapFactory.decodeStream(inputStream,null,options);
					
					if(!StringUtils.isEmpty(mLocalPicturePath)){
						String fileName = url.substring(url.lastIndexOf("/") + 1,url.length());
						String localFilePath = mLocalPicturePath + fileName;
						File myCaptureFile = new File(localFilePath);
						BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
						bitmapImage.compress(Bitmap.CompressFormat.PNG, 80, bos);
						bos.flush();
						bos.close();
					}

					return bitmapImage;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (inputStream != null) {
						inputStream.close();
					}
					entity.consumeContent();
				}
			}

		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}

	public void clearImageCache() {
		if (mMemoryCache != null && mMemoryCache.size() > 0) {
			mMemoryCache = null;
		}
	}
	
	//添加照片的bitmap到缓�?
	private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
	    if (getBitmapFromMemCache(key) == null) {
	        mMemoryCache.put(key, bitmap);
	    }
	}

	//获取缓存
	private Bitmap getBitmapFromMemCache(String key) {
	    return mMemoryCache.get(key);
	}

	/**
	 * 将view转化为bitmap
	 * 
	 * @author Administrator
	 * @date 2014-12-5 上午8:56:05
	 * @param sourceView
	 * @return
	 */
	public static Bitmap getBitmapFromView(View sourceView) {
		if (sourceView == null) {
			return null;
		}
		Bitmap returnedBitmap = Bitmap.createBitmap(sourceView.getWidth(),sourceView.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(returnedBitmap);
		Drawable bgDrawable = sourceView.getBackground();
		if (bgDrawable != null) {
			bgDrawable.draw(canvas);
		} else {
			canvas.drawColor(Color.TRANSPARENT);
		}
		sourceView.draw(canvas);
		return returnedBitmap;
	}
	
	public static class StringUtils {

		public static boolean isEmpty(String str){
			if(str != null && !"".equals(str)){
				if(!str.isEmpty()){
					return false;
				}else{
					return true;
				}
			}else{
				return true;
			}
		}
	}
}
