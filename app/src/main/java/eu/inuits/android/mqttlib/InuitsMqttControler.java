package eu.inuits.android.mqttlib;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class InuitsMqttControler {

    private Context context = null;

    public void init(Context context) {
        this.context = context;
    }

    public void connect(String uri) {
        connect(uri, null);
    }

    public void connect(String uri, String clientId) {
        Intent mqttServiceIntent = null;

        Log.d("CONNECT URI:", uri);

        mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
        mqttServiceIntent.putExtra(Constants.ACTION,Constants.ACTION_CONNECT);
        mqttServiceIntent.putExtra(Constants.DATA_SERVER_URI,uri);
        mqttServiceIntent.putExtra(Constants.DATA_CLIENT_ID,clientId);
        this.context.startService(mqttServiceIntent);

        CharSequence text = "Connected!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this.context, text, duration);
        toast.show();
    }

    public void disconnect() {
        Intent mqttServiceIntent = null;

        mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
        mqttServiceIntent.putExtra(Constants.ACTION,Constants.ACTION_DISCONNECT);
        this.context.startService(mqttServiceIntent);

        CharSequence text = "Disconnected!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this.context, text, duration);
        toast.show();
    }

    public Intent subscribe(String topic) {
        return subscribe(topic, 1);
    }

    public Intent subscribe(String topic, int qos) {
        Intent mqttServiceIntent = null;

        mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
        mqttServiceIntent.putExtra(Constants.ACTION, Constants.ACTION_SUBSCRIBE);
        mqttServiceIntent.putExtra(Constants.DATA_TOPIC, topic);
        mqttServiceIntent.putExtra(Constants.DATA_QOS, qos);
        this.context.startService(mqttServiceIntent);

        CharSequence text = "Subscribed!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this.context, text, duration);
        toast.show();

        return mqttServiceIntent;
    }

    public void unsubscribe(String topic) {
        Intent mqttServiceIntent = null;

        mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
        mqttServiceIntent.putExtra(Constants.ACTION, Constants.ACTION_UNSUBSCRIBE);
        mqttServiceIntent.putExtra(Constants.DATA_TOPIC, topic);
        this.context.startService(mqttServiceIntent);

        CharSequence text = "Unsubscribed!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this.context, text, duration);
        toast.show();
    }

    public void publish(String topic, String message) {
        Intent mqttServiceIntent = null;

        mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
        mqttServiceIntent.putExtra(Constants.ACTION, Constants.ACTION_PUBLISH);
        mqttServiceIntent.putExtra(Constants.DATA_TOPIC, topic);
        mqttServiceIntent.setData(Uri.parse(message));
        this.context.startService(mqttServiceIntent);

        CharSequence text = "Published!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this.context, text, duration);
        toast.show();
    }

}
