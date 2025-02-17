package com.anemona.vital_signs_processor.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;

@Data
public class AlertaDTO {
    private Long id_alerta;
    private String descripcion_alerta;
    private int nivel_alerta;
    // private LocalDateTime fecha_alerta;
    private LocalDate fecha_alerta; //terminamos de dividir la fecha y hora en el service
    private LocalTime hora_alerta;
    private Long id_paciente;
    //extra
    private boolean visto;
    private Long id_estado_vital;
    private String parametro_alterado;


}

