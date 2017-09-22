package org.radarcns.management.service.mapper.decorator;

import org.radarcns.management.domain.Source;
import org.radarcns.management.repository.SourceRepository;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.mapper.SourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Created by nivethika on 13-6-17.
 */

public abstract class SourceMapperDecorator implements SourceMapper {

    private static final String SEPARATOR = "_: ";

    @Autowired
    @Qualifier("delegate")
    private SourceMapper delegate;

    @Autowired
    private SourceRepository sourceRepository;

    @Override
    public MinimalSourceDetailsDTO sourceToMinimalSourceDetailsDTO(Source source) {
        MinimalSourceDetailsDTO dto = delegate.sourceToMinimalSourceDetailsDTO(source);
        dto.setDeviceTypeAndSourceId( source.getDeviceType().getDeviceModel() + SEPARATOR + source
            .getSourceName());
        return dto;
    }

    @Override
    public Source descriptiveDTOToSource(MinimalSourceDetailsDTO minimalSourceDetailsDTO) {
        Source source = sourceRepository.findOne(minimalSourceDetailsDTO.getId());
        source.setAssigned(minimalSourceDetailsDTO.isAssigned());
        return source;
    }
}
