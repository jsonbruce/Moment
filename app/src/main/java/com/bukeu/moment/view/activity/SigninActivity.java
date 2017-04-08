package com.bukeu.moment.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.graphics.Palette;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSON;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.controller.ApiController;
import com.bukeu.moment.model.User;
import com.bukeu.moment.util.StringUtils;
import com.bukeu.moment.util.UIHelper;
import com.bukeu.moment.view.service.MomentService;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import java.nio.charset.Charset;

public class SigninActivity extends BaseActivity {

    private EditText emailField;
    private EditText passField;
    private EditText repeatPassField;
    private Button signinButton;
    private Button signupButton;
    private ProgressBar progressbar;
    private LinearLayout repeatPasswordLayout;

    private String email;
    private String password;
    private String passwordRepeat;
    private boolean isSignUp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
        initData();
    }

    private void initView() {
        setContentView(R.layout.activity_signin);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_log_bg2);
        Palette palette = Palette.from(bitmap).generate();

        emailField = (EditText) findViewById(R.id.et_signin_email);
        passField = (EditText) findViewById(R.id.et_signin_password);
        signinButton = (Button) findViewById(R.id.btn_signin);
        signupButton = (Button) findViewById(R.id.btn_signup);
        progressbar = (ProgressBar) findViewById(R.id.prg_signin);
        repeatPasswordLayout = (LinearLayout) findViewById(R.id.layout_signin_repeatpassword);
        repeatPassField = (EditText) findViewById(R.id.et_signin_password_repeat);

        //Load our font
        Typeface openSansFont = Typeface.createFromAsset(getAssets(), "fonts/OpenSans-Regular.ttf");
        //set our fields and buttons to our font
        emailField.setTypeface(openSansFont);
        passField.setTypeface(openSansFont);
        signinButton.setTypeface(openSansFont);
        signupButton.setTypeface(openSansFont);

        emailField.requestFocus();
    }

    private void initData() {
        emailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                changeButtonTheme(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                changeButtonTheme(s.toString());
            }
        });

        passField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                changeButtonTheme(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                changeButtonTheme(s.toString());
            }
        });

        signinButton.setOnClickListener(onSigninClickListener);
        signupButton.setOnClickListener(onSignupClickListener);
    }

    private void changeButtonTheme(String s) {
        if (!TextUtils.isEmpty(s)) {
            signinButton.setTextColor(Color.BLACK);
            signinButton.setEnabled(true);
        } else {
            signinButton.setTextColor(Color.GRAY);
        }
    }

    private View.OnClickListener onSigninClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            email = emailField.getText().toString();
            password = passField.getText().toString();
            if (isSignUp) {
                passwordRepeat = repeatPassField.getText().toString();
            }

            if (StringUtils.isEmpty(email)
                    || StringUtils.isEmpty(password)
                    || !StringUtils.isEmail(email)
                    ) {
                UIHelper.showToastMessage(SigninActivity.this, getResources().getString(R.string.error_signin_blank));
                return;
            }

            if (isSignUp && !password.equals(passwordRepeat)) {
                UIHelper.showToastMessage(SigninActivity.this, getResources().getString(R.string.error_password_repeat));
                passField.setText(null);
                repeatPassField.setText(null);
                return;
            }

            progressbar.setVisibility(View.VISIBLE);

            if (!isSignUp) {
                ApiController.signin(email, password, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String response = new String(responseBody, Charset.forName("utf-8"));
                        User user = JSON.parseObject(response, User.class);
                        MomentApplication.getContext().setUser(user);

                        startService(new Intent(SigninActivity.this, MomentService.class));

                        progressbar.setVisibility(View.GONE);
                        UIHelper.startMainActivity(SigninActivity.this, MainActivity.ACTION_FROM_SIGNIN);
                        SigninActivity.this.finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        progressbar.setVisibility(View.GONE);
                        if (statusCode == 404) {
                            UIHelper.showToastMessage(SigninActivity.this, "user not exist");
                        }
                    }
                });
            } else {
                ApiController.signup(email, password, new Response.Listener() {
                            @Override
                            public void onResponse(Object response) {
                                User user = JSON.parseObject(response.toString(), User.class);
                                MomentApplication.getContext().setUser(user);
                                MomentApplication.getContext().getUserMomentList().setMoments(null);

                                startService(new Intent(SigninActivity.this, MomentService.class));

                                progressbar.setVisibility(View.GONE);
                                UpdateProfileActivity.startUpdateProfile(SigninActivity.this, UpdateProfileActivity.ACTION_FROM_SIGNUP);
                                SigninActivity.this.finish();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (error.networkResponse.statusCode == 409) {
                                    UIHelper.showToastMessage(SigninActivity.this, "user already exist!");
                                }
                                UIHelper.showToastMessage(SigninActivity.this, error.getMessage());
                                progressbar.setVisibility(View.GONE);
                            }
                        }
                );

            }
        }
    };

    private View.OnClickListener onSignupClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            emailField.setFocusable(true);
            passField.setText(null);

            if (!isSignUp) {
                signinButton.setText(getResources().getString(R.string.signup));
                signupButton.setText(getResources().getString(R.string.signin));
                repeatPasswordLayout.setVisibility(View.VISIBLE);

                isSignUp = true;
            } else {
                signinButton.setText(getResources().getString(R.string.signin));
                signupButton.setText(getResources().getString(R.string.signup));
                repeatPasswordLayout.setVisibility(View.GONE);

                isSignUp = false;
            }
        }
    };

}
