package com.artcom.y60;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ErrorPresentationActivity extends Activity {

    enum Mode {
        DEBUG, PRODUCTION
    }

    // TODO this will eventually need to be configurable, possibly via
    // preferences
    private static final Mode mMode = Mode.DEBUG;

    private TextView          mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.error_presentation);

        mTextView = (TextView) findViewById(R.id.error_text);
        Button okButton = (Button) findViewById(R.id.error_ok_button);

        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        ErrorHandling.cancelErrorNotification(this);
    }

    @Override
    protected void onPause() {
        // nothing else seems to work to avoid stacking when task affinity no
        // longer is an option due to the possibility of arbitrary web apps
        finish();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent intent = getIntent();
        if (mMode == Mode.DEBUG) {

            String logTag = "[unknown source]";
            if (intent.hasExtra(ErrorHandling.ID_LOGTAG)) {
                logTag = intent.getStringExtra(ErrorHandling.ID_LOGTAG);
            }

            String stacktrace = "<no stacktrace>";
            if (intent.hasExtra(ErrorHandling.ID_STACKTRACE)) {
                intent.getStringExtra(ErrorHandling.ID_STACKTRACE);
            }

            String message = "(unspecified error)";
            if (intent.hasExtra(ErrorHandling.ID_MESSAGE)) {
                message = intent.getStringExtra(ErrorHandling.ID_MESSAGE);

                // Sort by class here later, for now, we just log everything in
                // verbose mode
                Logger.e(logTag, message, stacktrace);
            }

            mTextView.setText("Oops! " + logTag + " says: " + message + "\n\n" + stacktrace);

        } else if (mMode == Mode.PRODUCTION) {

            mTextView.setText(R.string.err_user_general);
        }
    }

}
