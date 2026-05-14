package bo.ms.informatik.mqttproject.service;

import bo.ms.informatik.mqttproject.services.MqttListener;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc // to inject MockMvc and in order to test REST Endpoints
@Import(ControlMessageIntegrationTest.MqttTestConfig.class) // Import internal config class
public class ControlMessageIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MqttListener mqttListener; // injected from  the mocked config.

    @Test
    void testSendModePublishesMessageToMqtt() throws Exception {
        String mode = "2";

        // Simulate a response of publish (to avoid a NullPointerException)
        Mockito.when(mqttListener.publish("Wago750/Control", mode))
                .thenReturn("Published");

        // perform a POST Request  to the  REST Endpoint
        mockMvc.perform(post("/mqttApi/sendMode/" + mode))
                .andExpect(status().isOk());

        // Checking that mqttListener.publish has been once called with good parameters
        Mockito.verify(mqttListener, times(1)).publish("Wago750/Control", mode);
    }


    // Config class for simulating the MQTT bean
    @TestConfiguration
    static class MqttTestConfig {
        @Bean
        public MqttListener mqttListener() {
            return Mockito.mock(MqttListener.class);
        }
    }
}