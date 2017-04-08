package com.bukeu.moment.view.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bukeu.moment.MomentConfig;
import com.bukeu.moment.R;
import com.bukeu.moment.util.ImageUtils;
import com.bukeu.moment.util.UIHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * camera activity, return image URI
 */
public class CameraActivity extends Activity implements Camera.PictureCallback,
		OnClickListener {

	public static final int REQUEST_CODE = 0x8;
	public static final String RESULT_KEY_PATHS = "paths";

	/** 相机实例 */
	private Camera mCamera;
	/** 继承surfaceView的自定义view 用于存放照相的图片 */
	private CameraView mCameraView = null;
	/** 照相机预览界面 */
	private FrameLayout mCameraPreview;
	/** 拍照按钮 */
	private Button mCaptureButton;
	/** 本地相册按钮 */
	private ImageButton mAlbumButton;// 文件夹
	/** 确认按钮 */
	private ImageButton mConfirmButton;

	private String mImagePath;  // 存放图片路径
//	private Uri mImageUri;     // 存放图片URI

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		initView();
		initData();

	}

	private void initView() {
		mCameraPreview = (FrameLayout) findViewById(R.id.layout_camera_preview);
		mAlbumButton = (ImageButton) findViewById(R.id.btn_camera_album);
		mCaptureButton = (Button) findViewById(R.id.btn_camera_capture);
		mConfirmButton = (ImageButton) findViewById(R.id.btn_camera_confirm);
	}

	private void initData() {
		initCameraPreview();

		mAlbumButton.setOnClickListener(this);
		mConfirmButton.setOnClickListener(this);
		mCaptureButton.setOnClickListener(this);
	}

	/**
	 * 初始化相机预览界面
	 */
	private void initCameraPreview() {
		mCameraView = new CameraView(CameraActivity.this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		mCameraPreview.addView(mCameraView, params);
	}

	/**
	 * 设置照片uri
	 *
	 * @param path
	 */
	private void startPublishActivity(String path) {
		mImagePath = path;
		PublishActivity.openWithPhotoPath(CameraActivity.this, mImagePath);
	}

//	private void startPublishActivity(Uri uri) {
//		mImageUri = uri;
//		PublishActivity.openWithPhotoPath(CameraActivity.this, uri);
//	}

	@Override
	public void onClick(View v) {
		if (v.equals(mCaptureButton)) {
			mCamera.takePicture(null, null, CameraActivity.this);
		} else if (v.equals(mAlbumButton)) {
			// 打开系统图片浏览器
			Intent intent = new Intent(Intent.ACTION_PICK,
					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent, 1);
		} else if (v.equals(mConfirmButton)) {
			if (mImagePath != null) {
				startPublishActivity(mImagePath);
			} else {
				UIHelper.showToastMessage(CameraActivity.this, "Please take one photo !");
			}
		}
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		File picture = new File(MomentConfig.PATH_TMP_IMG, timeStamp + ".jpg");
		String imagePath = null;
		try {
			imagePath = picture.getPath();
			FileOutputStream fos = new FileOutputStream(imagePath);// 获得文件输出流
			fos.write(data);// 写入文件
			fos.close();// 关闭文件流
			camera.startPreview();

			startPublishActivity(imagePath);
		} catch (Exception e) {
			e.printStackTrace();
			UIHelper.showToastMessage(CameraActivity.this, "拍照失败,请重试!");
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			TextView content = (TextView) getLayoutInflater().inflate(R.layout.about_view, null);
			content.setMovementMethod(LinkMovementMethod.getInstance());
			content.setText(Html.fromHtml(getString(R.string.ask_close)));
			new AlertDialog.Builder(CameraActivity.this)
					.setTitle(null)
					.setView(content)
					.setInverseBackgroundForced(true)
					.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							CameraActivity.this.finish();
						}
					}).create().show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == 1) {
				startPublishActivity(ImageUtils.getAbsoluteImagePath(CameraActivity.this, data.getData()));
			}
		}
	}

	/**
	 * 主要的surfaceView，负责展示预览图片，camera的开关
	 *
	 * @author Max Xu
	 */
	class CameraView extends SurfaceView implements SurfaceHolder.Callback {

		private SurfaceHolder holder = null;

		public CameraView(Context context) {
			super(context);
			holder = this.getHolder();
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
			holder.addCallback(this);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			try {
				mCamera = Camera.open();
				// 设置camera预览的角度，因为默认图片是倾斜90度的
				mCamera.setDisplayOrientation(90);
				// 设置holder主要是用于surfaceView的图片的实时预览，以及获取图片等功能
				mCamera.setPreviewDisplay(holder);
			} catch (IOException e) {
				mCamera.release();
				mCamera = null;
				e.printStackTrace();
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
								   int height) {
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setRotation(90);

			if (CameraActivity.this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
				// 如果是竖屏
				parameters.set("orientation", "portrait");
				// 在2.2以上可以使用
				// camera.setDisplayOrientation(90);
			} else {
				parameters.set("orientation", "landscape");
				// 在2.2以上可以使用
				// camera.setDisplayOrientation(0);
			}

			mCamera.setParameters(parameters);
			mCamera.startPreview();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		}
	}

}