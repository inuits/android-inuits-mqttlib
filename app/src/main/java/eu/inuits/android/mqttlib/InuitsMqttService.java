package eu.inuits.android.mqttlib;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.nfc.Tag;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


/**
 * Intent service that controls MQTT Paho Eclipse Android Service library
 */
public class InuitsMqttService extends IntentService {

    private static String TAG = InuitsMqttService.class.toString();

    //<editor-fold desc="Variable definitions" defaultstate="collapsed">
    private static MqttAndroidClient mqttAndroidClient;
    private static MqttConnectOptions mqttConnectOptions;
    private static Map<String, Integer> topics = new HashMap<>();
    //</editor-fold>

    //<editor-fold desc="Constructors" defaultstate="collapsed">
    public InuitsMqttService() {
        this(InuitsMqttService.class.toString());
    }

    public InuitsMqttService(String name) {
        super(name);
        if (InuitsMqttService.topics == null) {
            InuitsMqttService.topics = new HashMap<>();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Intent Handler method" defaultstate="collapsed">
    /**
     * This method is the main one that gets run in new thread by definition of IntentService
     * @param workIntent Inbound intent to react to.
     */
    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Check intent
        if (workIntent == null) {
            Log.w(TAG, "Intent can't be null, ignoring...");
            return;
        }

        // Get and check action from intent
        String action = workIntent.getStringExtra(Constants.ACTION);
        if (action == null) {
            Log.e(TAG, "Received intent without ACTION spcified, ignoring...");
            return;
        }

        Log.d(TAG, "Received intent of type: "+ action);

        // React on action
        switch (action) {

            case Constants.ACTION_CONNECT: {
                // Get rest of the data
                String serverUri = workIntent.getStringExtra(Constants.DATA_SERVER_URI);
                String clientId = workIntent.getStringExtra(Constants.DATA_CLIENT_ID);
                String username = workIntent.getStringExtra(Constants.DATA_USERNAME);
                String password = workIntent.getStringExtra(Constants.DATA_PASSWORD);

                if (serverUri == null) {
                    Log.e(TAG, "Server URI not specified, can't connect. Aborting!");
                    return;
                }

                if (clientId == null) {
                    clientId = UUID.randomUUID().toString();
                    Log.w(TAG, "Client ID not specified, generating random UUID: " + clientId);
                }

                if (username == null) {
                    Log.d(TAG, "No username");
                }

                if (password == null) {
                    Log.d(TAG, "No pasword");
                }

                this.initClient(serverUri, clientId, username, password);
                this.connect();
                break;
            }
            case Constants.ACTION_DISCONNECT: {
                this.disconnect();
                break;
            }
            case Constants.ACTION_SUBSCRIBE: {
                // Get rest of the data
                String topic = workIntent.getStringExtra(Constants.DATA_TOPIC);
                Integer qos = workIntent.getIntExtra(Constants.DATA_QOS, Constants.QOS_DEFAULT_VALUE);

                if (topic == null) {
                    Log.e(TAG, "Topic not specified, can't subscribe to unknown topic. Aborting!");
                    return;
                }
                this.subscribe(topic, qos);
                break;
            }
            case Constants.ACTION_UNSUBSCRIBE: {
                String topic = workIntent.getStringExtra(Constants.DATA_TOPIC);

                if (topic == null) {
                    Log.e(TAG, "Topic not specified, can't unsubscribe from unknown topic. Aborting!");
                    return;
                }
                this.unsubscribe(topic);
                break;
            }
            case Constants.ACTION_PUBLISH: {
                String topic = workIntent.getStringExtra(Constants.DATA_TOPIC);
                if (topic == null) {
                    Log.e(TAG, "Topic not specified, can't publish to unknown topic. Aborting!");
                    return;
                }
                this.publish(topic, workIntent.getDataString());
                break;
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="Main methods" defaultstate="collapsed">

    /**
     * Wrapper for initClient (String serverUri, String clientId, String username, String password)
     * This will not set username and password (will set to null)
     * See Doc for the other method for more information
     *
     * @param serverUri Server URI for MQTT. Eg. "wss://example.com/mqtt"
     * @param clientId Client ID that is used in MQTT communications
     * @return returns instance of the client
     */
    public MqttAndroidClient initClient(String serverUri, String clientId) {
        return this.initClient(serverUri, clientId, null, null);
    }

    /**
     * This methods inits the MQTT client if it was not initialized before.
     * !NOTE - this method does have effect only the first time called.
     * !NOTE - this method is here *knowingly*! It returns MqttAndroidClient which can be obtained
     *         from nativescript and tampered with the object in JavaScript! Which is intended
     *         purpose an/or usage of this library.
     *
     * TODO: the change in serverUri and clientId are not changed after the client is initialized.
     * @param serverUri Server URI for MQTT. Eg. "wss://example.com/mqtt"
     * @param clientId Client ID that is used in MQTT communications
     * @param username Username for MQTT connection
     * @param password Password for MQTT connection
     * @return returns instance of the client
     */
    public MqttAndroidClient initClient(String serverUri, String clientId, String username, String password) {
        if (InuitsMqttService.topics == null) {
            InuitsMqttService.topics = new HashMap<>();
        }
        if (InuitsMqttService.mqttConnectOptions == null) {
            InuitsMqttService.mqttConnectOptions = new MqttConnectOptions();
            if (username != null) {
                InuitsMqttService.mqttConnectOptions.setUserName(username);
            }
            if (password != null) {
                InuitsMqttService.mqttConnectOptions.setPassword(password.toCharArray());
            }
            // TODO: add more options as needed
            InuitsMqttService.mqttConnectOptions.setAutomaticReconnect(true);
            InuitsMqttService.mqttConnectOptions.setCleanSession(true);
        }
        if (InuitsMqttService.mqttAndroidClient == null) {
            InuitsMqttService.mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);

            this.setupCallbacks();
        }
        return InuitsMqttService.mqttAndroidClient;
    }

    /**
     * This method will setup all required callbacks.
     * Mainly it is used for setup Message-received callback.
     */
    public void setupCallbacks() {
        InuitsMqttService.mqttAndroidClient.setCallback(new MqttCallbackExtended() {

            @Override
            public void connectComplete(boolean reconnect, String serverUri) {
                if (serverUri == null) {
                    serverUri = "<? UNKNOWN URI ?>";
                }
                if (reconnect) {
                    Log.d(TAG, "Reconnected to: " + serverUri);
                    // If Clean Session is true, we need to re-subscribe
                    subscribeToTopics();
                    return;
                }

                Log.d(TAG, "Connected to: " + serverUri);
            }

            @Override
            public void connectionLost(Throwable cause) {
                if (cause == null) {
                    Log.i(TAG, "Connection lost due to unknown cause, possibly disconnect!");
                    broadcastResponseViaIntent(Constants.RESPONSE_CONNECTION_LOST,
                            "Connection lost due to unknown cause, possibly disconnect!");
                    return;
                }

                Log.w(TAG, "Connection lost due to: " + cause.toString());
                broadcastResponseViaIntent(Constants.RESPONSE_CONNECTION_LOST,
                        "Connection lost due to: " + cause.toString(),
                        cause);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                if (topic == null || message == null) {
                    return;
                }
                String payload = new String(message.getPayload());
                Log.d(TAG, "Incomming message: " + payload);
                broadcastMessageViaIntent(topic, payload);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                if (token == null) {
                    return;
                }
                Log.d(TAG, "Delivered message: " + token.toString());
            }
        });
    }


    /**
     * This method will connect to MQTT and subscribe to topics that are stored globally.
     */
    public void connect() {
//        this.disconnect();
        if (InuitsMqttService.mqttAndroidClient == null) {
            Log.w(TAG, "mqttAndroidClient not initialized!");
            return;
        }

        try {
            Log.d(TAG, "Connecting to: " + InuitsMqttService.mqttAndroidClient.getServerURI());
            InuitsMqttService.mqttAndroidClient.connect(InuitsMqttService.getMqttConnectOptions(), null, new IMqttActionListener() {
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
                public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
                    Log.w(TAG, "Failed to connect to: " + InuitsMqttService.mqttAndroidClient.getServerURI(), ex);
                    broadcastResponseViaIntent(Constants.RESPONSE_CONNECTION_ERROR, "Failed to connect to: " + InuitsMqttService.mqttAndroidClient.getServerURI(), ex);
                }
            });
        } catch (MqttException ex){
            broadcastResponseViaIntent(Constants.RESPONSE_CONNECTION_ERROR, "Catched Exception!", ex);
            ex.printStackTrace();
        }
    }

    /**
     * TODO: This needs to be rewritten for proper options
     *
     * @return MqttConnectOptions object with reasonable default settings
     */
    private static MqttConnectOptions getMqttConnectOptions() {
        if (InuitsMqttService.mqttConnectOptions == null) {
            Log.w(TAG, "mqttConnectOption is null and it shouldn't be! Returning empty options, which may not be what you wanted.");
            InuitsMqttService.mqttConnectOptions = new MqttConnectOptions();
        }
        return InuitsMqttService.mqttConnectOptions;
    }

    /**
     * This will subscribe to all topics stored in InuitsMqttService.topics Map.
     * Map key is topic and values is QOS
     */
    private void subscribeToTopics() {
        if (InuitsMqttService.topics == null) {
            Log.w(TAG, "Subscribed topics are null?!");
            return;
        }

        for (Map.Entry<String, Integer> entry : InuitsMqttService.topics.entrySet()) {
            Log.d(TAG, "Subscribing to topic: " + entry.getKey()+ " " + entry.getValue());
            try {
                InuitsMqttService.mqttAndroidClient.subscribe(entry.getKey(), entry.getValue());
            } catch (MqttException ex){
                broadcastResponseViaIntent(Constants.RESPONSE_CONNECTION_ERROR, "Catched Exception!", ex);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Disconnects from the connected server.
     */
    public void disconnect(){
        if (InuitsMqttService.mqttAndroidClient == null) {
            Log.w(TAG, "mqttAndroidClient not initialized!");
            return;
        }
        try {
            if (InuitsMqttService.mqttAndroidClient.isConnected()) {
                InuitsMqttService.mqttAndroidClient.disconnect();
            } else {
                Log.d(TAG, "Client is not connected - NO need to panic!");
            }
        } catch (MqttException ex){
            broadcastResponseViaIntent(Constants.RESPONSE_CONNECTION_ERROR, "Catched Exception!", ex);
            ex.printStackTrace();
        }
    }

    /**
     * Subscribes to the topic with default QOS defined by `Constants.QOS_DEFAULT_VALUE`
     * @param topic topic to subscribe to
     */
    private void subscribe(String topic) {
        this.subscribe(topic, null);
    }

    /**
     * Subscribes to the topic with passed QOS in argument,
     * when null defaults to QOS defined by `Constants.QOS_DEFAULT_VALUE`
     *
     * TODO: Underlying subscribe library has more options - explore them more!
     *
     * @param topic topic to subscribe to
     * @param qos required qos (see Constants.QOS_* values)
     */
    private void subscribe(String topic, Integer qos) {
        if (InuitsMqttService.mqttAndroidClient == null) {
            Log.w(TAG, "mqttAndroidClient not initialized!");
            return;
        }

        if (InuitsMqttService.topics == null) {
            Log.w(TAG, "Subscribed topics are null?!");
            return;
        }

        if (topic == null) {
            Log.w(TAG, "Topic is null, can't publish to unknown topic. Aborting!");
            return;
        }

        if (qos == null) {
            qos = Constants.QOS_DEFAULT_VALUE;
            Log.w(TAG, "QOS is null. Defaulting to: " + qos);
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
                    public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
                        Log.w(TAG, "Error subscribing go topic!", ex);
                        broadcastResponseViaIntent(Constants.RESPONSE_SUBSCRIBE_ERROR, "Error subscribing go topic!", ex);
                    }
                });
            } else {
                Log.d(TAG, "Client is not connected - NO need to panic!");
            }

        } catch (MqttException ex){
            broadcastResponseViaIntent(Constants.RESPONSE_CONNECTION_ERROR, "Catched Exception!", ex);
            ex.printStackTrace();
        }
    }

    /**
     * Unsubscribes from the topic.
     * @param topic topic to unsubscribe from
     */
    private void unsubscribe(String topic) {
        if (InuitsMqttService.mqttAndroidClient == null) {
            Log.w(TAG, "mqttAndroidClient not initialized!");
            return;
        }

        if (InuitsMqttService.topics == null) {
            Log.w(TAG, "Subscribed topics are null?!");
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
            } else {
                Log.d(TAG, "Client is not connected - NO need to panic!");
            }

        } catch (MqttException ex){
            broadcastResponseViaIntent(Constants.RESPONSE_CONNECTION_ERROR, "Catched Exception!", ex);
            ex.printStackTrace();
        }
    }


    /**
     * Publish wrapper for publishing mqttMessage with just string.
     * @param topic topic to publish to
     * @param message message to publish
     */
    public void publish(String topic, String message) {
        if (topic != null && message != null){
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            this.publish(topic, mqttMessage);
        }
    }

    /**
     * Publish to topic with mqttMessage passed to undelying library.
     * @param topic topic to publish to
     * @param mqttMessage message to publish
     */
    public void publish(String topic, MqttMessage mqttMessage) {
        if (InuitsMqttService.mqttAndroidClient == null) {
            Log.w(TAG, "mqttAndroidClient not initialized!");
            return;
        }

        if (topic == null || mqttMessage == null) {
            Log.w(TAG, "Can't publish message because topic or content of message is not set!");
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
                    public void onFailure(IMqttToken asyncActionToken, Throwable ex) {
                        Log.w(TAG, "Failed to publish to topic.", ex);
                        broadcastResponseViaIntent(Constants.RESPONSE_CONNECTION_ERROR, "Failed to publish to topic.", ex);
                    }
                });
            } else {
                Log.w(TAG, "Client is not connected - NOW there is maybe a little need to panic!");
            }
        } catch (MqttException ex){
            broadcastResponseViaIntent(Constants.RESPONSE_CONNECTION_ERROR, "Catched Exception!", ex);
            ex.printStackTrace();
        }
    }

    //</editor-fold>

    //<editor-fold desc="Helpers" defaultstate="collapsed">

    /**
     * Private method for returning Messages as Intents
     * @param topic topic from which message originates
     * @param payload payload that message carries
     */
    private void broadcastMessageViaIntent(String topic, String payload) {
        Intent intent = new Intent(Constants.MESSAGE);
        intent.putExtra(Constants.MESSAGE_TOPIC, topic);
        intent.putExtra(Constants.MESSAGE_DATA, payload);
//        intent.setData(Uri.parse(payload));
        sendBroadcast(intent);
    }

    /**
     * Private method for returning any other information via intent.
     * @param responseType type of response (see Constants.RESPONSE_*)
     * @param responseMessage optional message for the response
     */
    private void broadcastResponseViaIntent(String responseType, String responseMessage) {
        this.broadcastResponseViaIntent(responseType, responseMessage, null);
    }

    /**
     * Private method for returning any other information via intent.
     * If there is throwable included it will post StackTrace also via Intent.
     * @param responseType type of response (see Constants.RESPONSE_*)
     * @param responseMessage optional message for the response
     */
    private void broadcastResponseViaIntent(String responseType, String responseMessage, Throwable error) {
        Intent intent = new Intent(Constants.RESPONSE);
        intent.putExtra(responseType, responseMessage);
        if (error != null) {
            intent.putExtra(Constants.RESPONSE_ERROR, error.getStackTrace());
        }
        sendBroadcast(intent);
    }
    //</editor-fold>
}
