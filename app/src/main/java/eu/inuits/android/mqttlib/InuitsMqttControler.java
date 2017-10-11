package eu.inuits.android.mqttlib;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class InuitsMqttControler {

    public void init(Context context) {

    }

    public boolean start (Context context) {
        Log.v("MQTTCONTROLER", String.valueOf(context));
        Log.v("MQTTCONTROLER", String.valueOf(context.getApplicationContext()));

        Intent mqttServiceIntent = new Intent(context, InuitsMqttService.class);
        mqttServiceIntent.setData(Uri.parse("test"));

        // Starts the IntentService
        context.startService(mqttServiceIntent);

        return true;
    }

    public boolean terminate(Context context) {
        return true;
    }

    public void show(Context context) {
        CharSequence text = "Hello NativeScript!";
        int duration = Toast.LENGTH_SHORT;

//        Toast toast = Toast.makeText(context, text, duration);
//        toast.show();
    }
}
