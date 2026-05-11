package br.com.escalas.api.ministry;

public record MinistryResponse(
    Long id,
    String name,
    String description,
    boolean active
) {
}
