package eu.inuits.android.mqttlib;

public final class Constants {

    /* INTENT - PUT EXTRA KEY */
    public static final String ACTION = "eu.inuits.android.mqttlib.ACTION";
    public static final String MESSAGE = "eu.inuits.android.mqttlib.MESSAGE";

    /* INTENT ACTION TYPES - PUT EXTRA VALUES */
    public static final String ACTION_CONNECT = "eu.inuits.android.mqttlib.ACTION_CONNECT";
    public static final String ACTION_DISCONNECT = "eu.inuits.android.mqttlib.ACTION_DISCONNECT";
    public static final String ACTION_SUBSCRIBE = "eu.inuits.android.mqttlib.ACTION_SUBSCRIBE";
    public static final String ACTION_UNSUBSCRIBE = "eu.inuits.android.mqttlib.ACTION_UNSUBSCRIBE";
    public static final String ACTION_PUBLISH = "eu.inuits.android.mqttlib.ACTION_PUBLISH";

    /* INTENT DATA TYPES */

    // ACTION_CONNECT
    public static final String DATA_SERVER_URI = "eu.inuits.android.mqttlib.DATA_SERVER_URI";
    public static final String DATA_CLIENT_ID = "eu.inuits.android.mqttlib.DATA_CLIENT_ID";

    // ACTION_SUBSCRIBE
    public static final String DATA_TOPIC = "eu.inuits.android.mqttlib.DATA_TOPIC";
    public static final String DATA_QOS = "eu.inuits.android.mqttlib.DATA_QOS";

    // ACTION_UNSUBSCRIBE
    // public static final String DATA_TOPIC = "eu.inuits.android.mqttlib.DATA_TOPIC";

    // ACTION_PUBLISH
    // set data


    /* INTENT BROADCASTS */
    public static final String MESSAGE_RECEIVED = "android.intent.action.MQTT_MESSAGE_RECEIVED";

    /* INTENT DATA */
    public static final String MESSAGE_TOPIC = "eu.inuits.android.mqttlib.MESSAGE_TOPIC";
    public static final String MESSAGE_DATA = "eu.inuits.android.mqttlib.MESSAGE_DATA";

}
