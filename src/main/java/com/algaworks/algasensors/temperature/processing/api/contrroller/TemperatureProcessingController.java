package com.algaworks.algasensors.temperature.processing.api.contrroller;

import com.algaworks.algasensors.temperature.processing.api.model.TemperatureLog;
import com.algaworks.algasensors.temperature.processing.common.IdGenerator;
import com.algaworks.algasensors.temperature.processing.infrastructure.rabbitmq.RabbitMQConfiguration;
import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
@RequiredArgsConstructor
@RequestMapping("/api/sensors/{sensorId}/temperatures/data")
@RestController
public class TemperatureProcessingController {

    private final RabbitTemplate rabbitTemplate;

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

        MessagePostProcessor messagePostProcessor = message -> {
            message.getMessageProperties().setHeader("TSID", temperatureLog.getSensorId().toString());
            return message;
        };

        rabbitTemplate.convertAndSend(RabbitMQConfiguration.FANOUT_EXCHANGE, "", temperatureLog, messagePostProcessor);
    }
}
