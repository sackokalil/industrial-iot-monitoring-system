package bo.ms.informatik.mqttproject.service;

import bo.ms.informatik.mqttproject.entities.MessageDocument;
import bo.ms.informatik.mqttproject.repositories.MqttMessageRepository;
import bo.ms.informatik.mqttproject.services.MqttListener;
import bo.ms.informatik.mqttproject.web.MqttMessageController;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

//@SpringBootTest
//@TestPropertySource("classpath:application.properties")
// so that those defined values in applicaiton.properties can be loaded, because connectAndSubscribe() use them
public class MqttServiceUnitTest {

    @Mock
    private MqttClient mqttClient;

    @Mock
    private MqttMessageRepository messageRepository;

    //@Mock
    //private MqttMessageRepository mqttTemperatureRepository;

    @InjectMocks
    private MqttListener mqttListener;

    private MqttMessageController controller;

    @BeforeEach //to be executed before any test
    void setUp() {
        MockitoAnnotations.openMocks(this); //initialise all the @Mock and @InjectMocks
        controller = new MqttMessageController(mqttListener, messageRepository);

        mqttListener.setMqttClient(mqttClient);

        // manual injection of topics values
        ReflectionTestUtils.setField(mqttListener, "username", "test-user");
        ReflectionTestUtils.setField(mqttListener, "password", "test-pass");
        ReflectionTestUtils.setField(mqttListener, "statusTopic", "status");
        ReflectionTestUtils.setField(mqttListener, "temperaturIsttopic", "ist");
        ReflectionTestUtils.setField(mqttListener, "temperaturSolltopic", "soll");
        ReflectionTestUtils.setField(mqttListener, "temperaturDifftopic", "diff");
    }

    @Test
    void testConnectAndSubscribeToTopics() throws Exception {
        when(mqttClient.isConnected()).thenReturn(false);

        doNothing().when(mqttClient).connect(any(MqttConnectOptions.class));
        doNothing().when(mqttClient).setCallback(any(MqttCallback.class));
        doNothing().when(mqttClient).subscribe(anyString());

        mqttListener.connectAndSubscribe();

        verify(mqttClient).connect(any(MqttConnectOptions.class));
        verify(mqttClient).setCallback(any(MqttCallback.class));
        verify(mqttClient).subscribe("status");
        verify(mqttClient).subscribe("ist");
        verify(mqttClient).subscribe("soll");
        verify(mqttClient).subscribe("diff");
    }

    @Test
    void testPublish_Successful() throws Exception {
        String topic = "test/topic";
        String message = "Hello";

        when(mqttClient.isConnected()).thenReturn(true);//simulation of connected client

        // Simulate a publish
        doNothing().when(mqttClient).publish(eq(topic), any(MqttMessage.class));

        String result = mqttListener.publish(topic, message);

        assertEquals("Published to the topic " + topic, result);
        verify(mqttClient).publish(eq(topic), any(MqttMessage.class));
    }

    @Test
    void testPublish_ClientNotConnected() throws Exception {
        when(mqttClient.isConnected()).thenReturn(false); //Simulate an mqttClient non-connected

        String result = mqttListener.publish("any", "message");

        assertEquals("Client not connected", result);
        verify(mqttClient, never()).publish(any(), any());
    }

    @Test
    void testGetRecentStatus() {
        MessageDocument expected = new MessageDocument();
        when(messageRepository.findTopByTopicOrderByReceivedAtDesc("Wago750/Status")).thenReturn(expected);

        MessageDocument result = controller.getRecentStatus();
        assertSame(expected, result);
    }

    @Test
    void testDisconnect() throws Exception {
        when(mqttClient.isConnected()).thenReturn(true); // simulate client connected
        doNothing().when(mqttClient).disconnect();

        mqttListener.disconnect();

        verify(mqttClient).disconnect();
    }
}
