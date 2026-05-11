package br.com.escalas.service;

import br.com.escalas.api.exception.BusinessException;
import br.com.escalas.api.exception.NotFoundException;
import br.com.escalas.api.ministry.MinistryRequest;
import br.com.escalas.api.ministry.MinistryResponse;
import br.com.escalas.domain.ministry.Ministry;
import br.com.escalas.repository.MinistryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MinistryService {

    private final MinistryRepository ministryRepository;

    @Transactional(readOnly = true)
    public List<MinistryResponse> findAll() {
        return ministryRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public Ministry findEntityById(Long id) {
        return ministryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Ministério não encontrado."));
    }

    @Transactional
    public MinistryResponse create(MinistryRequest request) {
        validateDuplicate(null, request.name());
        Ministry ministry = new Ministry();
        apply(ministry, request);
        return toResponse(ministryRepository.save(ministry));
    }

    @Transactional
    public MinistryResponse update(Long id, MinistryRequest request) {
        Ministry ministry = findEntityById(id);
        validateDuplicate(id, request.name());
        apply(ministry, request);
        return toResponse(ministryRepository.save(ministry));
    }

    @Transactional
    public void delete(Long id) {
        Ministry ministry = findEntityById(id);
        ministryRepository.delete(ministry);
    }

    public MinistryResponse toResponse(Ministry ministry) {
        return new MinistryResponse(
            ministry.getId(),
            ministry.getName(),
            ministry.getDescription(),
            ministry.isActive()
        );
    }

    private void apply(Ministry ministry, MinistryRequest request) {
        ministry.setName(request.name().trim());
        ministry.setDescription(request.description());
        ministry.setActive(request.active());
    }

    private void validateDuplicate(Long currentId, String name) {
        ministryRepository.findByNameIgnoreCase(name.trim())
            .filter(found -> !found.getId().equals(currentId))
            .ifPresent(found -> {
                throw new BusinessException("Já existe um ministério com esse nome.");
            });
    }
}
