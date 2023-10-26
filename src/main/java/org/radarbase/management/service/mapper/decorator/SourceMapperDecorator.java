package org.radarbase.management.service.mapper.decorator;

import org.radarbase.management.domain.Source;
import org.radarbase.management.repository.SourceRepository;
import org.radarbase.management.repository.SubjectRepository;
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO;
import org.radarbase.management.service.dto.SourceDTO;
import org.radarbase.management.service.mapper.SourceMapper;
import org.radarbase.management.web.rest.errors.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.radarbase.management.web.rest.errors.EntityName.SOURCE;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_SOURCE_NOT_FOUND;

/**
 * Created by nivethika on 13-6-17.
 */

public abstract class SourceMapperDecorator implements SourceMapper {

    @Autowired
    @Qualifier("delegate")
    private SourceMapper delegate;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Override
    public Source minimalSourceDTOToSource(MinimalSourceDetailsDTO minimalSource) {
        Source source = sourceRepository
                .findOneBySourceId(minimalSource.getSourceId())
                .orElseThrow(() -> new NotFoundException(
                        "Source ID " + minimalSource.getSourceId() + " not found",
                        SOURCE, ERR_SOURCE_NOT_FOUND,
                        Map.of("sourceId", minimalSource.getSourceId().toString())));
        source.setAssigned(minimalSource.isAssigned());
        return source;
    }

    @Override
    public Source sourceDTOToSource(SourceDTO sourceDto) {
        Source source = delegate.sourceDTOToSource(sourceDto);
        if (sourceDto.getId() != null) {
            Source existingSource = sourceRepository.findById(sourceDto.getId())
                    .orElseThrow(() -> new NotFoundException(
                            "Source ID " + sourceDto.getId() + " not found",
                            SOURCE, ERR_SOURCE_NOT_FOUND,
                            Map.of("sourceId", sourceDto.getId().toString())));
            if (sourceDto.getSubjectLogin() == null) {
                source.subject = existingSource.subject;
            } else {
                source.subject = subjectRepository
                        .findOneWithEagerBySubjectLogin(sourceDto.getSubjectLogin())
                        .orElseThrow(NoSuchElementException::new);
            }
        }
        return source;
    }
}
