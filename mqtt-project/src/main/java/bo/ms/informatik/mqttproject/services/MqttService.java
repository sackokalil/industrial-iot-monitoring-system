package bo.ms.informatik.mqttproject.services;

import bo.ms.informatik.mqttproject.excepitons.ConnexionProblemException;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MqttService {
    abstract void connectAndSubscribe() throws ConnexionProblemException;
    abstract String publish(String topic, String message) throws MqttException;
    abstract void disconnect() throws MqttException;
    abstract void messageArrived(String topic, MqttMessage message) throws MqttException;
    abstract void connectionLost(Throwable cause);
    abstract void deliveryComplete(IMqttDeliveryToken token);



}
