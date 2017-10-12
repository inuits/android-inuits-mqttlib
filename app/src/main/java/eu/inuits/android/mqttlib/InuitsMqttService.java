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
import java.util.UUID;
import java.util.zip.InflaterInputStream;


public class InuitsMqttService extends IntentService {

    private static String TAG = InuitsMqttService.class.toString();

    private static Integer DEFAULT_QOS = 1;

    /* VARIABLE DEFINITION */
    private static MqttAndroidClient mqttAndroidClient;
    private static Map<String, Integer> topics = new HashMap<>();

    /* CONSTRUCTORS */
    public InuitsMqttService() {
        this(InuitsMqttService.class.toString());
    }

    public InuitsMqttService(String name) {
        super(name);
    }

    /* OVERRIDDEN IntentService METHODS */
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

        String action = workIntent.getStringExtra(Constants.ACTION);
        Log.d(TAG, "Received intent of type: "+ action);

        String serverUri = null;
        String clientId = null;
        String topic = null;
        Integer qos = null;
        switch (action) {
            case Constants.ACTION_CONNECT:
                // Obtain more data
                serverUri = workIntent.getStringExtra(Constants.DATA_SERVER_URI);
                clientId = workIntent.getStringExtra(Constants.DATA_CLIENT_ID);

                if (serverUri == null) {
                    Log.e(TAG, "Server URI not specified, can't connect. Aborting!");
                } else {
                    if(clientId == null) {
                        clientId = UUID.randomUUID().toString();
                        Log.w(TAG, "Client ID not specified, generating random UUID: " + clientId);
                    }
                    this.initClient(serverUri, clientId);
                    this.connect();
                }
                break;

            case Constants.ACTION_DISCONNECT:
                this.disconnect();
                break;

            case Constants.ACTION_SUBSCRIBE:
                // Obtain more data
                topic = workIntent.getStringExtra(Constants.DATA_TOPIC);
                qos = workIntent.getIntExtra(Constants.DATA_QOS, InuitsMqttService.DEFAULT_QOS);

                if (topic == null) {
                    Log.e(TAG, "Topic not specified, can't subscribe to unknown topic. Aborting!");
                } else {
                    this.subscribe(topic, qos);
                }
                break;

            case Constants.ACTION_UNSUBSCRIBE:
                topic = workIntent.getStringExtra(Constants.DATA_TOPIC);

                if (topic == null) {
                    Log.e(TAG, "Topic not specified, can't unsubscribe from unknown topic. Aborting!");
                } else {
                    this.unsubscribe(topic);
                }
                break;

            case Constants.ACTION_PUBLISH:
                topic = workIntent.getStringExtra(Constants.DATA_TOPIC);
                if (topic == null) {
                    Log.e(TAG, "Topic not specified, can't publish to unknown topic. Aborting!");
                } else {
                    this.publish("testtopic/inuits", workIntent.getDataString());
                }
                break;
        }
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
        } else {
            Log.w(TAG, "mqttAndroidClient not initialized!");
        }
    }

    private void subscribe(String topic) {
        this.subscribe(topic, InuitsMqttService.DEFAULT_QOS);
    }
    private void subscribe(String topic, Integer qos) {
        if (InuitsMqttService.mqttAndroidClient == null) {
            Log.w(TAG, "mqttAndroidClient not initialized!");
            return;
        }

        if (topic == null) {
            Log.e(TAG, "Topic not specified, can't publish to unknown topic. Aborting!");
            return;
        }

        if (qos == null) {
            qos = InuitsMqttService.DEFAULT_QOS;
            Log.w(TAG, "Qos is not set. Defaulting to: " + qos);
        }
        try {
            InuitsMqttService.topics.put(topic, qos);
            if (InuitsMqttService.mqttAndroidClient.isConnected()) {
                InuitsMqttService.mqttAndroidClient.subscribe(topic, qos, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "Successfully subscribed to topic.");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d(TAG, "Failed to subscribed to topic.");
                    }
                });
            }

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    private void unsubscribe(String topic) {
        if (InuitsMqttService.mqttAndroidClient == null) {
            Log.w(TAG, "mqttAndroidClient not initialized!");
            return;
        }

        if (topic == null) {
            Log.w(TAG, "Topic variable is null. Can't unsubscribe to that!");
            return;
        }

        try {
            InuitsMqttService.topics.remove(topic);
            if (InuitsMqttService.mqttAndroidClient.isConnected()) {
                InuitsMqttService.mqttAndroidClient.unsubscribe(topic);
            }
        } catch (MqttException e) {
            e.printStackTrace();
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
        if (InuitsMqttService.mqttAndroidClient == null) {
            Log.w(TAG, "mqttAndroidClient not initialized!");
            return;
        }

        try {
            Log.d(TAG, "Connecting to: " + InuitsMqttService.mqttAndroidClient.getServerURI());
            
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
                    Log.d(TAG, "Failed to connect to: " + InuitsMqttService.mqttAndroidClient.getServerURI());
                }
            });
        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    public void disconnect(){
        if (InuitsMqttService.mqttAndroidClient == null) {
            Log.w(TAG, "mqttAndroidClient not initialized!");
            return;
        }
        try {
            if (InuitsMqttService.mqttAndroidClient.isConnected()) {
                InuitsMqttService.mqttAndroidClient.disconnect();
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void publish(String topic, String message) {
        if (topic != null && message != null){
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            this.publish(topic, mqttMessage);
        }
    }

    public void publish(String topic, MqttMessage mqttMessage) {
        if (InuitsMqttService.mqttAndroidClient == null) {
            Log.w(TAG, "mqttAndroidClient not initialized!");
            return;
        }

        if (topic == null || mqttMessage == null) {
            Log.e(TAG, "Can't publish message because topic or content of message is not set!");
            return;
        }
        try {
            if (InuitsMqttService.mqttAndroidClient.isConnected()) {
                InuitsMqttService.mqttAndroidClient.publish(topic, mqttMessage, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        Log.d(TAG, "Successfully published to topic.");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d(TAG, "Failed to publish to topic.");
                    }
                });
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
