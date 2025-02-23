package com.anemona.vital_signs_processor.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.anemona.vital_signs_processor.dto.EstadoVitalDTO;
import com.anemona.vital_signs_processor.dto.ParametrosVitalesDTO;

// Escucha kafka y procesa datos
@Service
public class VitalSignsListener {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AlertService alertService;

    private static final String PARAMETROS_VITALES_URL = "http://aneback:8080/api/parametros-vitales/activos";
    
    @SuppressWarnings("null")
    @KafkaListener(topics = "senales_vitales", groupId = "anemona_group")
    public void listen(EstadoVitalDTO estadoVital) {
    
        // Consultamos los parámetros desde aneback
        ParametrosVitalesDTO parametrosActivos = restTemplate.getForObject(PARAMETROS_VITALES_URL, ParametrosVitalesDTO.class);
        Long pacienteId = estadoVital.getId_paciente();
    
        boolean alertaGenerada = false;
    
        // Validamos los signos vitales
        if (estadoVital.getFrecuencia_cardiaca() < parametrosActivos.getFrecuencia_cardiaca_min() ||
            estadoVital.getFrecuencia_cardiaca() > parametrosActivos.getFrecuencia_cardiaca_max()) {
            alertaGenerada = true;
        }
    
        if (estadoVital.getPresion_arterial_sis() < parametrosActivos.getPresion_arterial_sis_min() ||
            estadoVital.getPresion_arterial_sis() > parametrosActivos.getPresion_arterial_sis_max()) {
            alertaGenerada = true;
        }
    
        if (estadoVital.getPresion_arterial_dias() < parametrosActivos.getPresion_arterial_dias_min() ||
            estadoVital.getPresion_arterial_dias() > parametrosActivos.getPresion_arterial_dias_max()) {
            alertaGenerada = true;
        }
    
        if (estadoVital.getSaturacion_oxigeno() < parametrosActivos.getSaturacion_oxigeno_min()) {
            alertaGenerada = true;
        }
    
        // **Si se genera una alerta, primero registramos el estado vital**
        if (alertaGenerada) {
            EstadoVitalDTO estadoVitalGuardado = alertService.procesarAlerta(estadoVital, pacienteId);
    
            if (estadoVitalGuardado != null) {
                // Asignamos el ID correcto al estado vital
                estadoVital.setId_estado(estadoVitalGuardado.getId_estado());
                System.err.println("ESTADO VITAL REGISTRADO CORRECTAMENTE EN ANEBACK CON ID: " + estadoVital.getId_estado());
    
                // Ahora sí generamos la alerta con el ID correcto
                if (estadoVital.getFrecuencia_cardiaca() < parametrosActivos.getFrecuencia_cardiaca_min() ||
                    estadoVital.getFrecuencia_cardiaca() > parametrosActivos.getFrecuencia_cardiaca_max()) {
                    alertService.createAlert("Frecuencia Cardiaca fuera de rango",
                        calcularNivelAlerta(estadoVital.getFrecuencia_cardiaca(),
                                            parametrosActivos.getFrecuencia_cardiaca_min(),
                                            parametrosActivos.getFrecuencia_cardiaca_max()),
                        "Frecuencia Cardiaca", estadoVital);
                    System.out.println("ALERTA: Frecuencia cardíaca fuera de rango.");
                }
    
                if (estadoVital.getPresion_arterial_sis() < parametrosActivos.getPresion_arterial_sis_min() ||
                    estadoVital.getPresion_arterial_sis() > parametrosActivos.getPresion_arterial_sis_max()) {
                    alertService.createAlert("Presion Arterial Sistolica fuera de rango",
                        calcularNivelAlerta(estadoVital.getPresion_arterial_sis(),
                                            parametrosActivos.getPresion_arterial_sis_min(),
                                            parametrosActivos.getPresion_arterial_sis_max()),
                        "Presion Arterial Sistolica", estadoVital);
                    System.out.println("ALERTA: Presion arterial sistolica fuera de rango.");
                }
    
                if (estadoVital.getPresion_arterial_dias() < parametrosActivos.getPresion_arterial_dias_min() ||
                    estadoVital.getPresion_arterial_dias() > parametrosActivos.getPresion_arterial_dias_max()) {
                    alertService.createAlert("Presion Arterial Diastolica fuera de rango",
                        calcularNivelAlerta(estadoVital.getPresion_arterial_dias(),
                                            parametrosActivos.getPresion_arterial_dias_min(),
                                            parametrosActivos.getPresion_arterial_dias_max()),
                        "Presion Arterial Diastolica", estadoVital);
                    System.out.println("ALERTA: Presion arterial diastolica fuera de rango.");
                }
    
                if (estadoVital.getSaturacion_oxigeno() < parametrosActivos.getSaturacion_oxigeno_min()) {
                    alertService.createAlert("Saturacion de oxigeno baja",
                        calcularNivelAlerta(estadoVital.getSaturacion_oxigeno(),
                                            parametrosActivos.getSaturacion_oxigeno_min(),
                                            null),
                        "Saturación de oxígeno", estadoVital);
                    System.out.println("ALERTA: Saturación de oxígeno fuera de rango.");
                }
    
            } else {
                System.err.println("ERROR: NO SE PUEDE OBTENER EL ESTADO VITAL REGISTRADO");
                return;
            }
        }
    
        // Si no hubo alerta, no enviamos el estado vital
        System.out.println("Estado Vital normal recibido pero no enviado a aneback: " + estadoVital);
    }

    private int calcularNivelAlerta(double valor, Number min, Number max) {
        if (min != null && valor < min.doubleValue() * 0.9) return 3; //critic
        if (max != null && valor > max.doubleValue() * 1.1) return 3; //nvl critic
        if (min != null && valor < min.doubleValue()) return 2; //moderado
        if (max != null && valor > max.doubleValue()) return 2; //moderado
        return 1; //bajo
    }

}
