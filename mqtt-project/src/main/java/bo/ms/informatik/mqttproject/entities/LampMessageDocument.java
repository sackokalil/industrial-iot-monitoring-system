package bo.ms.informatik.mqttproject.entities;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.TypeAlias;

@Data
@TypeAlias("lampe")

public class LampMessageDocument extends MessageDocument {
    private String status;
}
