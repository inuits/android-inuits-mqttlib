# Android MQTT lib wrapper

This repository should wrap PAHO ECLIPSE ANDROID SERVICE library.
Its main intended use is to expose librari api via intent calls.

## InuitsMqttService
is subclass of IntentService and translates intent calls into PAHO Service library.

The services returns all messages also via intents:
```
Intent intent = new Intent(Constants.MESSAGE_RECEIVED);
intent.putExtra(Constants.MESSAGE_TOPIC, topic);
intent.putExtra(Constants.MESSAGE_DATA, payload);
sendBroadcast(intent);
```

## InuitsMqttControler
is simple wrapper to call intents. But you can also call the Intents by yourself.

### Connect

```
Intent mqttServiceIntent = null;
mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
mqttServiceIntent.putExtra(Constants.ACTION,Constants.ACTION_CONNECT);
mqttServiceIntent.putExtra(Constants.DATA_SERVER_URI,uri);
mqttServiceIntent.putExtra(Constants.DATA_CLIENT_ID,clientId);
this.context.startService(mqttServiceIntent);
```

### Disconnect

```
Intent mqttServiceIntent = null;
mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
mqttServiceIntent.putExtra(Constants.ACTION,Constants.ACTION_DISCONNECT);
this.context.startService(mqttServiceIntent);
```

### Subscribe

```
Intent mqttServiceIntent = null;
mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
mqttServiceIntent.putExtra(Constants.ACTION, Constants.ACTION_SUBSCRIBE);
mqttServiceIntent.putExtra(Constants.DATA_TOPIC, topic);
mqttServiceIntent.putExtra(Constants.DATA_QOS, qos);
this.context.startService(mqttServiceIntent);
```

### Unsubscribe

```
Intent mqttServiceIntent = null;
mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
mqttServiceIntent.putExtra(Constants.ACTION, Constants.ACTION_UNSUBSCRIBE);
mqttServiceIntent.putExtra(Constants.DATA_TOPIC, topic);
this.context.startService(mqttServiceIntent);
```

### Publish
```
Intent mqttServiceIntent = null;
mqttServiceIntent = new Intent(this.context, InuitsMqttService.class);
mqttServiceIntent.putExtra(Constants.ACTION, Constants.ACTION_PUBLISH);
mqttServiceIntent.putExtra(Constants.DATA_TOPIC, topic);
mqttServiceIntent.setData(Uri.parse(message));
this.context.startService(mqttServiceIntent);
```