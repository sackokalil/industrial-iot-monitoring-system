package bo.ms.informatik.mqttproject;

import bo.ms.informatik.mqttproject.entities.LampMessageDocument;
import bo.ms.informatik.mqttproject.entities.TemperatureMessageDocument;
import bo.ms.informatik.mqttproject.services.MqttListener;
import bo.ms.informatik.mqttproject.services.MqttService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


@Slf4j
@SpringBootApplication
public class MqttProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(MqttProjectApplication.class, args);

	}



	@Bean
	CommandLineRunner runner(MqttListener mqttListener) {

		return args -> {
			log.info("STARTING MQTT PROJECT APPLICATION");

			//The brocker do not work this night, so I am bound to manually test my changes... hahaha
			/*String payloadstat = "25";
			MqttMessage mqttMessage = new MqttMessage(payloadstat.getBytes());
			mqttListener.messageArrived("Wago750/Status", mqttMessage);
			System.out.println(mqttMessage.toString());

			String payloadtemp = "27";
			MqttMessage mqttMessage2 = new MqttMessage(payloadtemp.getBytes());
			mqttListener.messageArrived("S7_1500/Temperatur/Ist", mqttMessage2);*/


		};
	}
}
