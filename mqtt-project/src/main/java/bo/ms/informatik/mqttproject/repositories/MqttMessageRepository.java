package bo.ms.informatik.mqttproject.repositories;

import bo.ms.informatik.mqttproject.entities.MessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MqttMessageRepository extends MongoRepository<MessageDocument,String> {
    MessageDocument findTopByTopicOrderByReceivedAtDesc(String topic);
}
