package com.bukeu.moment.view.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.bukeu.moment.MomentApplication;
import com.bukeu.moment.R;
import com.bukeu.moment.model.Feedback;
import com.bukeu.moment.util.StringUtils;
import com.bukeu.moment.util.UIHelper;
import com.bukeu.moment.view.service.PublishIntentService;

public class FeedbackActivity extends BaseActivity implements View.OnClickListener {

    private EditText contentEditText;
    private ImageView sendButton;

    private String feedContent;
    private Feedback mFeedback = new Feedback();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        contentEditText = (EditText) findViewById(R.id.feedback_text);
        sendButton = (ImageView) findViewById(R.id.send_button);

        contentEditText.requestFocus();
        sendButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_button:
                feedContent = contentEditText.getText().toString();
                if (StringUtils.isEmpty(feedContent)) {
                    UIHelper.showToastMessage(FeedbackActivity.this, getString(R.string.error_feedback));
                    return;
                } else {
                    if (MomentApplication.getContext().getUser() != null) {
                        mFeedback.setUuid(MomentApplication.getContext().getUser().getUuid());
                    }
                    mFeedback.setContent(StringUtils.filter(feedContent).trim());
                    PublishIntentService.startActionFeedback(FeedbackActivity.this, mFeedback);

                    UIHelper.showToastMessage(FeedbackActivity.this, getString(R.string.info_feedback));

                    FeedbackActivity.this.onBackPressed();
                }
                break;
        }
    }
}
