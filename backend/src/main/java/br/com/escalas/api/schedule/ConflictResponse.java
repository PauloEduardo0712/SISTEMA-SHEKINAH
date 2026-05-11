package br.com.escalas.api.schedule;

public record ConflictResponse(
    Long scheduleId,
    String volunteerName,
    String ministryName,
    String message
) {
}
