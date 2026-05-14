package bo.ms.informatik.mqttproject.web;

import bo.ms.informatik.mqttproject.entities.MessageDocument;

import bo.ms.informatik.mqttproject.excepitons.ConnexionProblemException;
import bo.ms.informatik.mqttproject.repositories.MqttMessageRepository;
import bo.ms.informatik.mqttproject.services.MqttListener;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@CrossOrigin("*")
@RequestMapping("/mqttApi")
public class MqttMessageController {
    private MqttListener mqttListener;
    //private MqttService service;
    private MqttMessageRepository messageRepository;


    @PostMapping("/connect")
    public String connect() throws ConnexionProblemException {
        log.info("POST /connect -> Starting MQTT connection...");
        mqttListener.connectAndSubscribe();
        return "Connected";
    }

    @GetMapping("/status")
    public MessageDocument getRecentStatus(){
        log.info("GET /status -> Fetching last status from DB");
        return messageRepository.findTopByTopicOrderByReceivedAtDesc("Wago750/Status");
    }

    @PostMapping("/sendMode/{mode}")
    public String sendMode(@PathVariable String mode) throws MqttException {

        log.info("POST /sendMode/{} -> Publishing mode to MQTT broker", mode);
       return mqttListener.publish("Wago750/Control", mode);
    }

    @PostMapping("/disconnect")
    public String disconnect() throws MqttException {

        log.info("POST /disconnect -> Disconnecting MQTT client");
        mqttListener.disconnect();
        return "Disconnected";
    }
    @GetMapping("/ist")
    public MessageDocument getIst() throws MqttException {

        log.info("GET /ist -> Fetching current temperature");
        return messageRepository.findTopByTopicOrderByReceivedAtDesc("S7_1500/Temperatur/Ist");
    }
    @GetMapping("/soll")
    public MessageDocument getSoll() throws MqttException {

        log.info("GET /soll -> Fetching target temperature");
        return messageRepository.findTopByTopicOrderByReceivedAtDesc("S7_1500/Temperatur/Soll");
    }
    @GetMapping("/diff")
    public MessageDocument getDiff() throws MqttException {

        log.info("GET /diff -> Fetching temperature difference");
        return messageRepository.findTopByTopicOrderByReceivedAtDesc("S7_1500/Temperatur/Differenz");
    }

    @GetMapping("/all")
    public List<MessageDocument> getAllData(){

        log.info("GET /all -> Fetching all messages");
        return messageRepository.findAll();

    }

}
