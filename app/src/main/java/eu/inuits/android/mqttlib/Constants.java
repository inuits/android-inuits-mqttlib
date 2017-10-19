package eu.inuits.android.mqttlib;

public final class Constants {

    public static final String ACTION   = "eu.inuits.android.mqttlib.ACTION";
    public static final String MESSAGE  = "eu.inuits.android.mqttlib.MESSAGE";
    public static final String RESPONSE = "eu.inuits.android.mqttlib.RESPONSE";


    public static final String ACTION_CONNECT     = "eu.inuits.android.mqttlib.ACTION_CONNECT";
    public static final String ACTION_DISCONNECT  = "eu.inuits.android.mqttlib.ACTION_DISCONNECT";
    public static final String ACTION_SUBSCRIBE   = "eu.inuits.android.mqttlib.ACTION_SUBSCRIBE";
    public static final String ACTION_UNSUBSCRIBE = "eu.inuits.android.mqttlib.ACTION_UNSUBSCRIBE";
    public static final String ACTION_PUBLISH     = "eu.inuits.android.mqttlib.ACTION_PUBLISH";

    public static final String DATA_SERVER_URI = "eu.inuits.android.mqttlib.DATA_SERVER_URI";
    public static final String DATA_CLIENT_ID  = "eu.inuits.android.mqttlib.DATA_CLIENT_ID";

    public static final String DATA_TOPIC = "eu.inuits.android.mqttlib.DATA_TOPIC";
    public static final String DATA_QOS   = "eu.inuits.android.mqttlib.DATA_QOS";

    public static final String MESSAGE_TOPIC = "eu.inuits.android.mqttlib.MESSAGE_TOPIC";
    public static final String MESSAGE_DATA = "eu.inuits.android.mqttlib.MESSAGE_DATA";

    public static final String RESPONSE_ERROR = "eu.inuits.android.mqttlib.RESPONSE_CONNECTED_ERROR";

    public static final String RESPONSE_CONNECTION_LOST  = "eu.inuits.android.mqttlib.RESPONSE_CONNECTION_LOST";
    public static final String RESPONSE_CONNECTION_ERROR = "eu.inuits.android.mqttlib.RESPONSE_CONNECTION_ERROR";

    public static final String RESPONSE_SUBSCRIBE_ERROR = "eu.inuits.android.mqttlib.RESPONSE_CONNECTION_ERROR";

    /* QOS */
    public static final int QOS_AT_MOST_ONCE  = 0;
    public static final int QOS_AT_LEAST_ONCE = 1;
    public static final int QOS_EXACTLY_ONCE  = 2;
    public static final int QOS_DEFAULT_VALUE = QOS_AT_LEAST_ONCE;

}
