package org.radarcns.management.service.mapper.decorator;

import org.radarcns.management.domain.Source;
import org.radarcns.management.repository.SourceRepository;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.mapper.SourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Optional;

/**
 * Created by nivethika on 13-6-17.
 */

public abstract class SourceMapperDecorator implements SourceMapper {
    @Autowired
    @Qualifier("delegate")
    private SourceMapper delegate;

    @Autowired
    private SourceRepository sourceRepository;

    @Override
    public MinimalSourceDetailsDTO sourceToMinimalSourceDetailsDTO(Source source) {
        MinimalSourceDetailsDTO dto = delegate.sourceToMinimalSourceDetailsDTO(source);
        dto.setDeviceTypeName(source.getDeviceType().getDeviceProducer()
            + " " + source.getDeviceType().getDeviceModel());
        return dto;
    }

    @Override
    public Source descriptiveDTOToSource(MinimalSourceDetailsDTO minimalSource) {
        Optional<Source> sourceOpt = sourceRepository.findOneBySourceId(minimalSource.getSourceId());
        if (!sourceOpt.isPresent()) {
            throw new IllegalArgumentException("Source ID " + minimalSource.getSourceId()
                + " not found");
        }
        Source source = sourceOpt.get();
        source.setAssigned(minimalSource.isAssigned());
        return source;
    }
}
