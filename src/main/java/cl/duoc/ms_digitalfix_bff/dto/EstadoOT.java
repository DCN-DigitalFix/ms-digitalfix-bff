package cl.duoc.ms_digitalfix_bff.dto;

/** Ciclo de vida de una orden de trabajo segun el caso DigitalFix. */
public enum EstadoOT {
    INGRESADA,
    ASIGNADA,
    EN_EJECUCION,
    CERRADA,
    ANULADA
}