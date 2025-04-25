package com.algaworks.algasensors.temperature.processing.api.contrroller;

import com.algaworks.algasensors.temperature.processing.api.model.TemperatureLog;
import com.algaworks.algasensors.temperature.processing.common.IdGenerator;
import io.hypersistence.tsid.TSID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@Slf4j
@RequestMapping("/api/sensors/{sensorId}/temperatures/data")
@RestController
public class TemperatureProcessingController {

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
    public void receive(@PathVariable TSID sensorId, @RequestBody String data) {

        if (data == null || data.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        double value;

        try {
            value = Double.parseDouble(data);
        } catch (NumberFormatException exc) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        TemperatureLog temperatureLog = TemperatureLog.builder()
                                                    .id(IdGenerator.generateTimeBasedUUID())
                                                    .sensorId(sensorId)
                                                    .registeredAt(OffsetDateTime.now())
                                                    .value(value)
                                                    .build();

        log.info(temperatureLog.toString());
    }
}
