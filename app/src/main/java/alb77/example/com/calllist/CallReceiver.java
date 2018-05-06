package alb77.example.com.calllist;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;


public class CallReceiver extends BroadcastReceiver {
    private String number;
    CustomDialog dialog;
    TelephonyManager telephonyManager;
    PhoneStateListener listener;
    Context context;
    String contactName = "";

    @Override
    public void onReceive(final Context context, Intent intent) {

        number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        getContactName(number, context);

        if (!intent.getAction().equals("android.intent.action.PHONE_STATE"))
            return;

        else {
            this.context = context;
            if (dialog == null) {
                dialog = new CustomDialog(context);
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.setTitle("Incoming Call for you");
                dialog.show();
            }

            telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            listener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    String stateString = "N/A";
                    switch (state) {
                        case TelephonyManager.CALL_STATE_IDLE:
                            stateString = "Idle";
                            dialog.dismiss();
                            break;
                        case TelephonyManager.CALL_STATE_OFFHOOK:
                            stateString = "Off Hook";
                            dialog.dismiss();
                            break;
                        case TelephonyManager.CALL_STATE_RINGING:
                            stateString = "Ringing";
                            dialog.show();
                            break;
                    }
                    Toast.makeText(context, stateString, Toast.LENGTH_LONG).show();
                }
            };

            // Register the listener with the telephony manager
            telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        }
    }

    class CustomDialog extends Dialog implements OnClickListener {

        public CustomDialog(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.dialog);
            Button btnOK = findViewById(R.id.ok_btn);
            TextView num = findViewById(R.id.incoming_call);
            num.setText("number is: " + number + "\n" + "contact name: " + contactName);

            btnOK.setVisibility(View.GONE);
            btnOK.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
        }
    }

    public String getContactName(final String phoneNumber, Context context) {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }

