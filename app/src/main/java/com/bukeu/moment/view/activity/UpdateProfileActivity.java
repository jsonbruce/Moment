package com.bukeu.moment.view.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.model.User;
import com.bukeu.moment.util.FileUtils;
import com.bukeu.moment.util.ImageUtils;
import com.bukeu.moment.util.StringUtils;
import com.bukeu.moment.util.UIHelper;
import com.bukeu.moment.view.widget.LoadingDialog;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;

public class UpdateProfileActivity extends BaseActivity {

    public static final String ACTION_FROM_SIGNUP = "action_from_signup";
    public static final String ACTION_FROM_PROFILE = "action_from_profile";
    private boolean fromSignUp = false;

    private ImageView mAvaterView;
    private EditText mNicknameView;
    private LoadingDialog loading;

    private final static int CROP = 200;
    private final static String FILE_SAVEPATH = Environment
            .getExternalStorageDirectory().getAbsolutePath()
            + "/Moment/user/avater/";
    private Uri origUri;
    private Uri cropUri;
    private File protraitFile;
    private Bitmap protraitBitmap;
    private String protraitPath;

    private String nickname;

    public static void startUpdateProfile(Context context, String action) {
        Intent intent = new Intent(context, UpdateProfileActivity.class);
        intent.setAction(action);
        context.startActivity(intent);
    }

    private final int AVATER_FINISH = 10;
    private final int UPDATE_FINISH = 11;
    private final int UPDATE_ERROR = -1;
    private final int IMAGE_NOT_EXIST = -2;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (loading != null) {
                loading.dismiss();
            }
            if (msg.what == AVATER_FINISH) {
                mAvaterView.setImageDrawable(Drawable.createFromPath(protraitPath));
            } else if (msg.what == UPDATE_FINISH && msg.obj != null) {
                User user = (User) msg.obj;
                MomentApplication.getContext().setUser(user);

                if (fromSignUp) {
                    UIHelper.startMainActivity(UpdateProfileActivity.this, MainActivity.ACTION_FROM_SIGNIN);
                    UpdateProfileActivity.this.finish();
                } else {
                    int[] startingLocation = new int[2];
                    UserProfileActivity.startUserProfileFromLocation(startingLocation, UpdateProfileActivity.this);
                    UpdateProfileActivity.this.finish();
                }

            } else if (msg.what == UPDATE_ERROR && msg.obj != null) {
                loading.setLoadText("上传出错·");
                loading.hide();
            } else if (msg.what == IMAGE_NOT_EXIST) {
                loading.setLoadText("图像不存在，上传失败·");
                loading.hide();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });

        fromSignUp = getIntent().getAction().equals(ACTION_FROM_SIGNUP);

        mAvaterView = (ImageView) findViewById(R.id.iv_user_avater);
        mNicknameView = (EditText) findViewById(R.id.et_user_nickname);

        mNicknameView.setText(MomentApplication.getContext().getUser().getNickname());
        Glide.with(this)
                .load(MomentApplication.getContext().getUser().getAvater())
                .placeholder(R.drawable.default_avater)
                .into(mAvaterView);
        mAvaterView.setOnClickListener(editerClickListener);
        mToolbar.setOnMenuItemClickListener(onMenuItemClickListener);
    }

    private View.OnClickListener editerClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            CharSequence[] items = {getString(R.string.img_from_album),
                    getString(R.string.img_from_camera)};
            imageChooseItem(items);
        }
    };

    public void imageChooseItem(CharSequence[] items) {
        AlertDialog imageDialog = new AlertDialog.Builder(this)
                .setTitle("上传头像").setIcon(android.R.drawable.btn_plus)
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        // 相册选图
                        if (item == 0) {
                            startImagePick();
                        }
                        // 手机拍照
                        else if (item == 1) {
                            startActionCamera();
                        }
                    }
                }).create();

        imageDialog.show();
    }

    private void startImagePick() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择图片"),
                ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP);
    }

    private void startActionCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, this.getCameraTempFile());
        startActivityForResult(intent,
                ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA);
    }

    // 裁剪头像的绝对路径
    private Uri getUploadTempFile(Uri uri) {
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            File savedir = new File(FILE_SAVEPATH);
            if (!savedir.exists()) {
                savedir.mkdirs();
            }
        } else {
            UIHelper.showToastMessage(UpdateProfileActivity.this, "无法保存上传的头像，请检查SD卡是否挂载");
            return null;
        }
        String timeStamp = MomentApplication.getContext().getUser().getUuid();
        String thePath = ImageUtils.getAbsolutePathFromNoStandardUri(uri);

        // 如果是标准Uri
        if (StringUtils.isEmpty(thePath)) {
            thePath = ImageUtils.getAbsoluteImagePath(UpdateProfileActivity.this, uri);
        }
        String ext = FileUtils.getFileFormat(thePath);
        ext = StringUtils.isEmpty(ext) ? "jpg" : ext;
        // 照片命名
        String cropFileName = "crop_" + timeStamp + "." + ext;
        // 裁剪头像的绝对路径
        protraitPath = FILE_SAVEPATH + cropFileName;
        protraitFile = new File(protraitPath);

        cropUri = Uri.fromFile(protraitFile);
        return this.cropUri;
    }

    // 拍照保存的绝对路径
    private Uri getCameraTempFile() {
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            File savedir = new File(FILE_SAVEPATH);
            if (!savedir.exists()) {
                savedir.mkdirs();
            }
        } else {
            UIHelper.showToastMessage(UpdateProfileActivity.this, "无法保存上传的头像，请检查SD卡是否挂载");
            return null;
        }
        String timeStamp = MomentApplication.getContext().getUser().getUuid();
        // 照片命名
        String cropFileName = timeStamp + ".jpg";
        // 裁剪头像的绝对路径
        protraitPath = FILE_SAVEPATH + cropFileName;
        protraitFile = new File(protraitPath);
        cropUri = Uri.fromFile(protraitFile);
        this.origUri = this.cropUri;
        return this.cropUri;
    }

    private void startActionCrop(Uri data) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(data, "image/*");
        intent.putExtra("output", this.getUploadTempFile(data));
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);// 裁剪框比例
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", CROP);// 输出图片大小
        intent.putExtra("outputY", CROP);
        intent.putExtra("scale", true);// 去黑边
        intent.putExtra("scaleUpIfNeeded", true);// 去黑边
        startActivityForResult(intent, ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD);
    }

    private void uploadNewPhoto() {

        nickname = mNicknameView.getText().toString();
        nickname = StringUtils.filter(nickname).trim();
        if (TextUtils.isEmpty(nickname)) {
            UIHelper.showToastMessage(UpdateProfileActivity.this, "nickname is a must!");
            return;
        }
        if (protraitFile == null
                ||!protraitFile.exists()) {
            UIHelper.showToastMessage(UpdateProfileActivity.this, "avater is a must!");
            return;
        }

        loading = new LoadingDialog(this);
        if (loading != null) {
            loading.setLoadText("正在上传头像···");
            loading.show();
        }

        new Thread() {
            public void run() {
                // 获取头像缩略图
                if (!StringUtils.isEmpty(protraitPath) && protraitFile.exists()) {
                    protraitBitmap = ImageUtils.getBitmapByPath(protraitPath);
//                    protraitBitmap = ImageUtils.loadImgThumbnail(protraitPath,
//                            200, 200);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loading.setLoadText("图像不存在，上传失败·");
                            loading.hide();
                        }
                    });
                }

                if (protraitBitmap != null) {
                    Message msg = new Message();
                    try {
                        User user = ApiController.updateAvater(nickname, protraitPath);
                        if (user != null) {
                            // 保存新头像到缓存
                            String filename = FileUtils.getFileName(user.getAvater());
                            ImageUtils.saveImage(UpdateProfileActivity.this, filename, protraitBitmap);
                        }
                        msg.what = UPDATE_FINISH;
                        msg.obj = user;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult(final int requestCode,
                                    final int resultCode, final Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case ImageUtils.REQUEST_CODE_GETIMAGE_BYCAMERA:
                startActionCrop(origUri);// 拍照后裁剪
                break;
            case ImageUtils.REQUEST_CODE_GETIMAGE_BYCROP:
                startActionCrop(data.getData());// 选图后裁剪
                break;
            case ImageUtils.REQUEST_CODE_GETIMAGE_BYSDCARD:
                mHandler.sendEmptyMessage(AVATER_FINISH);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update_profile, menu);
        return true;
    }

    private Toolbar.OnMenuItemClickListener onMenuItemClickListener = new Toolbar.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (menuItem.getItemId() == R.id.action_sure) {
                uploadNewPhoto();
            }
            return true;
        }
    };
}
