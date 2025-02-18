package com.anemona.vital_signs_processor.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.anemona.vital_signs_processor.dto.AlertaDTO;
import com.anemona.vital_signs_processor.dto.EstadoVitalDTO;

// Maneja alertas y publica en kafka
@Service
public class AlertService {
    
    @Autowired
    private KafkaTemplate<String, AlertaDTO> kafkaTemplate;

    private static final String ALERTAS_TOPIC = "alertas";

    public void createAlert(String descripcion, int nivel, String parametro, EstadoVitalDTO estadoVital) {
        AlertaDTO alerta = new AlertaDTO();
        alerta.setDescripcion_alerta(descripcion);
        alerta.setNivel_alerta(nivel);
        alerta.setFecha_alerta(LocalDate.now());
        alerta.setHora_alerta(LocalTime.now());
        alerta.setId_paciente(estadoVital.getId_paciente());
        alerta.setParametro_alterado(parametro);
        alerta.setVisto(false);
        alerta.setId_estado_vital(null); //luego vemos

        //Enviar alerta a Kafka
        kafkaTemplate.send(ALERTAS_TOPIC, alerta);
    }

}
