package org.radarbase.management.service.mapper.decorator;

import java.util.NoSuchElementException;
import org.radarbase.management.domain.Source;
import org.radarbase.management.repository.SourceRepository;
import org.radarbase.management.repository.SubjectRepository;
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO;
import org.radarbase.management.service.dto.SourceDTO;
import org.radarbase.management.service.mapper.SourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

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
    public Source descriptiveDTOToSource(MinimalSourceDetailsDTO minimalSource) {
        Source source = sourceRepository
                .findOneBySourceId(minimalSource.getSourceId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Source ID " + minimalSource.getSourceId() + " not found"));
        source.setAssigned(minimalSource.isAssigned());
        return source;
    }

    @Override
    public Source sourceDTOToSource(SourceDTO sourceDto) {
        Source source = delegate.sourceDTOToSource(sourceDto);
        if (sourceDto.getId() != null) {
            Source existingSource = sourceRepository.findById(sourceDto.getId()).get();
            if (sourceDto.getSubjectLogin() == null) {
                source.setSubject(existingSource.getSubject());
            } else {
                source.setSubject(subjectRepository
                        .findOneWithEagerBySubjectLogin(sourceDto.getSubjectLogin())
                        .orElseThrow(NoSuchElementException::new));
            }
        }
        return source;
    }
}
