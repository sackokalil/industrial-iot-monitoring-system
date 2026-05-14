package bo.ms.informatik.mqttproject.services;

import bo.ms.informatik.mqttproject.entities.LampMessageDocument;
import bo.ms.informatik.mqttproject.entities.MessageDocument;

import bo.ms.informatik.mqttproject.entities.TemperatureMessageDocument;
import bo.ms.informatik.mqttproject.repositories.MqttMessageRepository;
import jakarta.annotation.Nonnull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
 @Slf4j//for the Logger.
@RequiredArgsConstructor
@Data
public class MqttListener implements MqttService, MqttCallback {

    @Value("${mqtt.username}")
    private String username;
    @Value("${mqtt.password}")
    private String password;
    @Value("${mqtt.statusTopic}")
    private String statusTopic;
    @Value("${mqtt.temperaturIsttopic}")
    private String temperaturIsttopic;
    @Value("${mqtt.temperaturSolltopic}")
    private String temperaturSolltopic;
    @Value("${mqtt.temperaturDifftopic}")
    private String temperaturDifftopic;
    @Nonnull //spring instanciate automatically and inject throught constructor(@RequiredArgsConstructor) to those @Nonnull marked attribut
    private MqttMessageRepository messageRepository;
    @Nonnull
    private MqttClient mqttClient; // these classes(MqttClient, MqttConnectOptions) come from org.eclipse.paho.client.mqttv3.*
    private MqttConnectOptions options ;

    //to log infomation and error messages
    @Override
    public void connectAndSubscribe() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                log.info("MQTT Client already connected. Skipping reconnection.");
                return;
            }
            //this second if block came into play because of the unit test, otherwise we will be content with the 2 commented
            // lines beneath.
            if (mqttClient == null) {
                log.info("Creating new MQTT client and setting connection options...");
                mqttClient = new MqttClient("tcp://sr-labor.ddns.net:1883", MqttClient.generateClientId());
            }
            /*log.info("Creating new MQTT client and setting connection options...");
            mqttClient = new MqttClient("tcp://sr-labor.ddns.net:1883", MqttClient.generateClientId());*/

            options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(this.username);
            options.setPassword(this.password.toCharArray());

            mqttClient.setCallback(this);

            log.info("Connecting to MQTT broker...");
            mqttClient.connect(options);
            log.info("Successfully connected to MQTT broker.");

            mqttClient.subscribe(statusTopic);
            mqttClient.subscribe(temperaturIsttopic);
            mqttClient.subscribe(temperaturSolltopic);
            mqttClient.subscribe(temperaturDifftopic);

            log.info("Subscribed to topics: {}, {}, {}, {}",
                    statusTopic, temperaturIsttopic, temperaturSolltopic, temperaturDifftopic);

            System.out.println(" Subscribed to topics: " + statusTopic + " " + temperaturIsttopic + " " + temperaturSolltopic + " " + temperaturDifftopic);


        } catch (Exception e) {
            log.error("Failed to connect and subscribe to MQTT broker"+ e.getMessage(), e);
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect() throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.unsubscribe(statusTopic);
            mqttClient.unsubscribe(temperaturIsttopic);
            mqttClient.unsubscribe(temperaturSolltopic);
            mqttClient.unsubscribe(temperaturDifftopic);
            mqttClient.disconnect();
            mqttClient.close();

            System.out.println("MQTT client fully disconnected and closed");
            log.info ("MQTT client fully disconnected and closed");

            mqttClient = null;
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.out.println("*******************Connection lost**********************");
        log.info("Connection lost");
        log.error(throwable.getMessage());

        new Thread(() -> {
            int maxAttempts = 15;
            int attempts = 0;
            while (!mqttClient.isConnected() && attempts < maxAttempts) {
                try {
                    //Thread.sleep(3000); // pause befor every connection atempt
                    mqttClient.connect(options);
                    mqttClient.subscribe(statusTopic);
                    mqttClient.subscribe(this.temperaturIsttopic);
                    mqttClient.subscribe(this.temperaturSolltopic);
                    mqttClient.subscribe(this.temperaturDifftopic);
                    log.info("Reconnected and resubscribed to the topics : ");
                } catch (Exception e) {
                    log.error("Reconnection attempt {} failed: {}", attempts+1, e.getMessage());
                }
                attempts++;
            }
            if (!mqttClient.isConnected()) {
                log.error("Connexion lost! Unable to reconnect after {} attempts", maxAttempts);
            }
        }).start();
    }



    //This will automatically be called as soon as SPS publish a message in the topic
    // and the methode receive, the name of topic and the message published in it
    @Override
    public void messageArrived(String topic, MqttMessage message)  {

            String payload = new String(message.getPayload());
            log.info("NEW MESSAGE RECEIVED | TOPIC: {} | PAYLOAD: {}", topic, payload);
            System.out.println("************NEW MESSAGE : "+payload+ "******************");

            // 🧽 we take of [] from the data
            payload = payload.replaceAll("[\\[\\]]", "");

        if (topic.equals(statusTopic)) {
            //create an instance of LampMessageDocument
            LampMessageDocument lampDocument = new LampMessageDocument();
            lampDocument.setTopic(topic);
            lampDocument.setReceivedAt(LocalDateTime.now());

            // for the lampe : en binary
            int statusValue = Integer.parseInt(payload);
            String binary = String.format("%16s", Integer.toBinaryString(statusValue)).replace(" ", "0");
            lampDocument.setStatus(binary);
            log.info("Wago750/Status detected | Value: {} | Binary: {}", statusValue, binary);

            System.out.println("💡 Status topic → Binary : " + binary);

            messageRepository.save(lampDocument);
            log.info("Status document saved in MongoDB | Topic: {}", topic);

        } else {

            //create an instance of TemperatureMessageDocument
            TemperatureMessageDocument tempDocument = new TemperatureMessageDocument();
            tempDocument.setTopic(topic);
            tempDocument.setReceivedAt(LocalDateTime.now());

            // for the temperatures : in float directly
            tempDocument.setValue(payload);
            log.info("Temperature topic detected | Topic: {} | Value: {}", topic, payload);

            System.out.println("Temp topic [" + topic + "] → " + payload);

            messageRepository.save(tempDocument);
            log.info("Temperature document saved in MongoDB | Topic: {}", topic);
        }



    }

    @Override
   public String publish(String topic, String payload) throws MqttException {
        log.info("PUBLISHING TO THE TOPIC: {} | PAYLOAD: {} .....", topic, payload);
        if (!mqttClient.isConnected()) {

            log.warn("Cannot publish to topic {} — MQTT client is not connected.", topic);
            System.out.println("Cannot publish. Client not connected.");
            return "Client not connected";
        }
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(0);
        mqttClient.publish(topic, message);

        log.info("Message Published  to topic {} | Payload: {}", topic, payload);
        return "Published to the topic " + topic;
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        System.out.println("Delivery Complete");
        log.info("Delivery Complete");

    }

    //this methode is only used for unit test purpose.it will be used to inject the mocked object.
    public void setMqttClient(MqttClient mqttClient) {
        this.mqttClient = mqttClient;
    }


}
