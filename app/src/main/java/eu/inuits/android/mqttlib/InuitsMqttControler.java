package eu.inuits.android.mqttlib;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 * Controller class for MQTT that sends proper intent(s). (And is able to show toasts if needed.)
 */
public class InuitsMqttControler {

    private static String TAG = InuitsMqttService.class.toString();

    private Context context = null;

    /**
     * Initialization method to pass context.
     * Can't be put into constructor since nativescript (intended to be used with) doesn't support
     * java classes with custom constructors.
     *
     * So init is needed to pass context variable from external source.
     * @param context Context that is used to send all intents
     */
    public void init(Context context) {
        this.context = context;
    }

    /**
     * This method will display toast on screen.
     * For more information see Toast.makeText() method.
     * @param text Text to be shown in the toast message.
     * @param duration Duration of the toast. Defaults to "Toast.LENGTH_SHORT"
     */
    public void toast(CharSequence text, Integer duration) {
        if (text == null) {
            text = "...";
        }
        if (duration == null) {
            duration = Toast.LENGTH_SHORT;
        }
        Toast toast = Toast.makeText(this.context, text, duration);
        toast.show();
    }

    /**
     * Connects to MQTT server with selected URI. ClientID is set to null which lib defaults
     * to random UUID.
     * @param uri Whole URI path of the MQTT server. Example: "wss://example.com:443/mqtt"
     */
    public void connect(String uri) {
        connect(uri, null, null, null);
    }

    /**
     * Connects to MQTT server with URI and as clientId.
     * @param uri Whole URI path of the MQTT server. Example: "wss://example.com:443/mqtt"
     * @param clientId ClientId that is used to connect to the server
     */
    public void connect(String uri, String clientId, String username, String password) {
        Intent mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
        mqttServiceIntent.putExtra(Constants.ACTION, Constants.ACTION_CONNECT);
        mqttServiceIntent.putExtra(Constants.DATA_SERVER_URI, uri);
        mqttServiceIntent.putExtra(Constants.DATA_CLIENT_ID, clientId);
        mqttServiceIntent.putExtra(Constants.DATA_USERNAME, username);
        mqttServiceIntent.putExtra(Constants.DATA_PASSWORD, password);
        this.context.startService(mqttServiceIntent);
    }

    /**
     * Disconnect from currently connected server.
     */
    public void disconnect() {
        Intent mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
        mqttServiceIntent.putExtra(Constants.ACTION,Constants.ACTION_DISCONNECT);
        this.context.startService(mqttServiceIntent);
    }

    /**
     * Subscribe to MQTT topic. QOS is set to null, which lib defaults to 1 (at least once)
     * @param topic Topic to subscribe to
     */
    public void subscribe(String topic) {
        this.subscribe(topic, null);
    }

    /**
     * Subscribe to MQTT topic with specified QOS.
     * @param topic Topic to subscribe to
     * @param qos QOS of the subscription (0 - at most once, 1 - at least once, 2 - exactly once)
     *            You can use Constants.QOS_* variables which
     */
    public void subscribe(String topic, Integer qos) {
        Intent mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
        mqttServiceIntent.putExtra(Constants.ACTION, Constants.ACTION_SUBSCRIBE);
        mqttServiceIntent.putExtra(Constants.DATA_TOPIC, topic);
        mqttServiceIntent.putExtra(Constants.DATA_QOS, qos);
        this.context.startService(mqttServiceIntent);
    }

    /**
     * Unsubscribe from the topic
     * @param topic Topic to unsubscribe from
     */
    public void unsubscribe(String topic) {
        Intent mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
        mqttServiceIntent.putExtra(Constants.ACTION, Constants.ACTION_UNSUBSCRIBE);
        mqttServiceIntent.putExtra(Constants.DATA_TOPIC, topic);
        this.context.startService(mqttServiceIntent);
    }

    /**
     * Publish message under some topic.
     * TODO: Add more arguments so it can create full MQTTMessage.
     * @param topic Topic to post message to
     * @param message Message that should be published
     */
    public void publish(String topic, String message) {
        Intent mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
        mqttServiceIntent.putExtra(Constants.ACTION, Constants.ACTION_PUBLISH);
        mqttServiceIntent.putExtra(Constants.DATA_TOPIC, topic);
        mqttServiceIntent.setData(Uri.parse(message));
        this.context.startService(mqttServiceIntent);
    }
}
