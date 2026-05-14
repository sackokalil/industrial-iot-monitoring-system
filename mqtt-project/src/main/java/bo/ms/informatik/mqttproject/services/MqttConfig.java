package bo.ms.informatik.mqttproject.services;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class MqttConfig {
    @Value("${mqtt.broker}")
    private String broker;
    @Value("${mqtt.port}")
    private int port;
    @Value("${mqtt.clientId}")
    private String clientId;
    private static final Logger logger = LoggerFactory.getLogger(MqttConfig.class);

    @Bean
    public MqttClient mqttClient() {
        try {
            String brokerUrl = String.format("tcp://%s:%d", broker, port);

            MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

            return client;

        } catch (Exception e) {
            logger.error("Erro : couldn't create MQTT Client in MqttConfig Class", e);
            return null;
        }
    }
}
