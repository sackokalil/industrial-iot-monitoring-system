package bo.ms.informatik.mqttproject.service;

import bo.ms.informatik.mqttproject.entities.LampMessageDocument;
import bo.ms.informatik.mqttproject.entities.MessageDocument;
import bo.ms.informatik.mqttproject.repositories.MqttMessageRepository;
import bo.ms.informatik.mqttproject.services.MqttListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@AutoConfigureMockMvc // to inject MockMvc and in order to test REST Endpoints
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test") //will use in memory test DB define in application-test.yml in repository 'resources'  created in repository test
public class MessageReceptionAndRetrievalIntegrationsTest {

    @Autowired
    private MqttListener mqttListener;

    @Autowired
    private MqttMessageRepository messageRepository;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
    }

    @Test
    void testMessageReceivedAndRetrievedViaRest() throws Exception {
        // 1. Simulate  reception
        String topic = "Wago750/Status";
        String payload = "1"; // Binary value "000...0001"
        MqttMessage mqttMessage = new MqttMessage(payload.getBytes());

        mqttListener.messageArrived(topic, mqttMessage);

        // 2. Check  DB
        List<MessageDocument> savedDocuments = messageRepository.findAll();
        //convert the list into a LampMessageDocument list
        List<LampMessageDocument> savedLampDocument = savedDocuments.stream()
                .filter(doc -> doc instanceof LampMessageDocument)
                .map(doc -> (LampMessageDocument) doc)
                .collect(Collectors.toList());
        assertEquals(1, savedLampDocument.size());
        assertEquals("0000000000000001", savedLampDocument.get(0).getStatus());

        // 3. Check REST
        mockMvc.perform(get("/mqttApi/status")) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("0000000000000001"));
    }
}
