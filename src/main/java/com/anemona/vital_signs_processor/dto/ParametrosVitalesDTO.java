package com.anemona.vital_signs_processor.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ParametrosVitalesDTO {
    private Long id_parametro;
    private int frecuencia_cardiaca_min;
    private int frecuencia_cardiaca_max;
    private int presion_arterial_sis_min;
    private int presion_arterial_sis_max;
    private int presion_arterial_dias_min;
    private int presion_arterial_dias_max;
    private float saturacion_oxigeno_min;
    private boolean activo;
    private LocalDateTime fecha_actualizacion;
}

