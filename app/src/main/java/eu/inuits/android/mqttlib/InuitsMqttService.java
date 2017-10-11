package eu.inuits.android.mqttlib;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class InuitsMqttService extends IntentService {

    private static String TAG = InuitsMqttService.class.toString();

    /* VARIABLE DEFINITION */
    private static MqttAndroidClient mqttAndroidClient;
    
    private static Map<String, Integer> topics = new HashMap<>();

    private static final String serverUri = "wss://api-dev.mundosalsa.eu:443";
    private static final String clientId = "ExampleAndroidClient";

    private static final String subscriptionTopic = "testtopic/inuits";
    private static final String publishTopic = "testtopic/inuits";
    private static final String publishMessage = "Hello World!";

    /* CONSTRUCTORS */
    public InuitsMqttService() {
        this(InuitsMqttService.class.toString());
    }

    public InuitsMqttService(String name) {
        super(name);
    }

    /* OVERRIDDEN IntentService METHODS */
//    @Override
//    public void onCreate() {
//        super.onCreate();
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        super.onStartCommand(intent, flags, startId);
//
//        // Run service indefinitely until stopped explicitly
//        // This maybe not needed or working
//        return START_STICKY;
//    }

    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();

        Log.d(TAG, "Received intent: "+ dataString);

        // TODO here be dragons

        this.initClient(InuitsMqttService.serverUri, InuitsMqttService.clientId);
        this.connect();

        this.publish(InuitsMqttService.publishTopic, "Here be dragons!");

    }

    /* CUSTOM METHODS */

    /**
     * This methods inits the MQTT client if it was not initialized before
     *
     * TODO: the change in serverUri and clientId are not changed after the client is initialized.
     * @param serverUri Server URI for MQTT. Eg. "wss://example.com/mqtt"
     * @param clientId Client ID that is used in MQTT communications
     * @return returns instance of the client
     */
    public MqttAndroidClient initClient(String serverUri, String clientId) {
        if (InuitsMqttService.topics == null) {
            InuitsMqttService.topics = new HashMap<>();
        }
        if (InuitsMqttService.mqttAndroidClient == null) {
            InuitsMqttService.mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
            InuitsMqttService.topics.put("testtopic/inuits", 0);

            this.setupCallbacks();
        }
        return InuitsMqttService.mqttAndroidClient;
    }

    /**
     * This method will setup all required callbacks.
     * Mainly it is used for setup Message received callback.
     */
    public void setupCallbacks() {
        final LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
        
        InuitsMqttService.mqttAndroidClient.setCallback(new MqttCallbackExtended() {

            @Override
            public void connectComplete(boolean reconnect, String serverUri) {

                if (reconnect) {
                    Log.d(TAG, "Reconnected to: " + serverUri);
                    
                    // Because Clean Session is true, we need to re-subscribe
                    subscribeToTopics();
                } else {
                    Log.d(TAG, "Connected to: " + serverUri);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                Log.w(TAG, "Connection lost due to: " + cause.toString());
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String payload = new String(message.getPayload());
                Log.d(TAG, "Incomming message: " + payload);

                Intent localIntent =
                        new Intent(Constants.MESSAGE_RECEIVED)
                                // Puts the status into the Intent
                                .putExtra(Constants.MESSAGE_DATA, payload);
                
                // Broadcasts the Intent to receivers in this app.
                lbm.sendBroadcast(localIntent);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.d(TAG, "Delivered message: " + token.toString());
            }
        });
    }

    /**
     * This will subscribe to all topics stored in InuitsMqttService.topics Map.
     * Map key is topic and values is QOS
     */
    private void subscribeToTopics() {
        if (InuitsMqttService.topics != null) {
            for (Map.Entry<String, Integer> entry : InuitsMqttService.topics.entrySet()) {
                try {
                    InuitsMqttService.mqttAndroidClient.subscribe(entry.getKey(), entry.getValue());
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * TODO: This needs to be rewritten for proper options
     *
     * @return MqttConnectOptions object with reasonable default settings
     */
    private MqttConnectOptions getMqttConnectOptions() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(true);
        return mqttConnectOptions;
    }

    /**
     * This method will connect to MQTT and subscribe to topics
     */
    public void connect() {
        try {
            Log.d(TAG, "Connecting to: " + serverUri);
            
            InuitsMqttService.mqttAndroidClient.connect(this.getMqttConnectOptions(), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    InuitsMqttService.mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    
                    subscribeToTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failed to connect to: " + serverUri);
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    public void publish(String topic, String message) {
        if (topic != null && message != null){
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            this.publish(topic, mqttMessage);
        }
    }

    public void publish(String topic, MqttMessage mqttMessage) {
        if (topic != null && mqttMessage != null){
            try {
                InuitsMqttService.mqttAndroidClient.publish(topic, mqttMessage);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
