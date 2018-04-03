package com.example.jacekmichalik.idomapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;


public class SMS_Czytacz extends BroadcastReceiver {

    private SmsHandler handler;
    private Context context;


    /* Constructor. Handler is the activity  *
     * which will show the messages to user. */
    public SMS_Czytacz(SmsHandler handler,Context context) {
        this.handler = handler;
        this.context=context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        /* Retrieve the sms message chunks from the intent */
        SmsMessage[] rawSmsChunks;

//        Toast.makeText(context, "SMSik", Toast.LENGTH_SHORT).show();
        return;

//        try {
//            rawSmsChunks = Telephony.Sms.Intents.getMessagesFromIntent(intent);
//        } catch (NullPointerException ignored) {
//            return;
//        }
//
//        /* Gather all sms chunks for each sender separately */
//        Map<String, StringBuilder> sendersMap = new HashMap<>();
//        for (SmsMessage rawSmsChunk : rawSmsChunks) {
//            if (rawSmsChunk != null) {
//                String sender = rawSmsChunk.getDisplayOriginatingAddress();
//                String smsChunk = rawSmsChunk.getDisplayMessageBody();
//                StringBuilder smsBuilder;
//                if (!sendersMap.containsKey(sender)) {
//                    /* For each new sender create a separate StringBuilder */
//                    smsBuilder = new StringBuilder();
//                    sendersMap.put(sender, smsBuilder);
//                } else {
//                    /* Sender already in map. Retrieve the StringBuilder */
//                    smsBuilder = sendersMap.get(sender);
//                }
//                /* Add the sms chunk to the string builder */
//                smsBuilder.append(smsChunk);
//            }
//        }
//
//        /* Loop over every sms thread and concatenate the sms chunks to one piece */
//        for (Map.Entry<String, StringBuilder> smsThread : sendersMap.entrySet()) {
//            String sender = smsThread.getKey();
//            StringBuilder smsBuilder = smsThread.getValue();
//            String message = smsBuilder.toString();
//            handler.handleSms(sender, message);
//        }
    }
}
