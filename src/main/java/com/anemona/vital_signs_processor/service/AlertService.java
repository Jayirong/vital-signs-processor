package com.anemona.vital_signs_processor.service;

import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.anemona.vital_signs_processor.dto.AlertaDTO;
import com.anemona.vital_signs_processor.dto.EstadoVitalDTO;

// Maneja alertas y publica en kafka
@Service
public class AlertService {
    
    @Autowired
    private KafkaTemplate<String, AlertaDTO> kafkaTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private static final String ANEBACK_URL = "http://aneback:8080/api/estadoVitales/ingreso/";

    private static final String ALERTAS_TOPIC = "alertas";

    public void createAlert(String descripcion, int nivel, String parametro, EstadoVitalDTO estadoVital) {
        if (estadoVital.getId_estado() == null) {
            System.err.println("ERROR: No se puede enviar alerta sin ID de estado vital asignado.");
            return;
        }
    
        AlertaDTO alerta = new AlertaDTO();
        alerta.setDescripcion_alerta(descripcion);
        alerta.setNivel_alerta(nivel);
        alerta.setFecha_alerta(LocalDate.now());
        alerta.setHora_alerta(LocalTime.now());
        alerta.setId_paciente(estadoVital.getId_paciente());
        alerta.setParametro_alterado(parametro);
        alerta.setVisto(false);
        
        // **Aseguramos que la alerta tenga el estado vital asociado**
        alerta.setId_estado_vital(estadoVital.getId_estado());
    
        // Enviar alerta a Kafka
        kafkaTemplate.send(ALERTAS_TOPIC, alerta);      
    }

    //metodo para procesar el estado vital a OCI, de paso habilitamos un retry para que intente 3 veces si falla
    @SuppressWarnings("null")
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 5000))
    public EstadoVitalDTO procesarAlerta(EstadoVitalDTO estadoVital, Long pacienteId) {

        String url = ANEBACK_URL + pacienteId;

        try {
            ResponseEntity<EstadoVitalDTO> response = restTemplate.postForEntity(url, estadoVital, EstadoVitalDTO.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                System.out.println("Estado vital registrado correctamente en aneback con ID: " + response.getBody().getId_estado());
                return response.getBody();
            } else {
                System.err.println("Error al registrar el estado vital en Aneback. Cod: " + response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error al comunicarse con aneback: " + e.getMessage());
            throw e;
        }

    }

}
