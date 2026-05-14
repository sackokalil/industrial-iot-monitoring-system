package bo.ms.informatik.mqttproject.entities;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.TypeAlias;

@Data
@TypeAlias("temperature")

public class TemperatureMessageDocument extends MessageDocument {
    private String value;
}
