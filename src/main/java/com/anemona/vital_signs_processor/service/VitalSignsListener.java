package com.anemona.vital_signs_processor.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.anemona.vital_signs_processor.dto.EstadoVitalDTO;

// Escucha kafka y procesa datos
@Service
public class VitalSignsListener {
    
    @KafkaListener(topics = "senales_vitales", groupId = "vital-signs-group")
    public void listen(EstadoVitalDTO estadoVital) {
        System.out.println("RESIVIDOCCCC: " + estadoVital);
    }

}
