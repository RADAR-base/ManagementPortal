package org.radarcns.management.service.mapper.decorator;

import java.util.NoSuchElementException;
import java.util.Optional;
import org.radarcns.management.domain.Source;
import org.radarcns.management.repository.SourceRepository;
import org.radarcns.management.repository.SubjectRepository;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.SourceDTO;
import org.radarcns.management.service.mapper.SourceMapper;
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
        Optional<Source> sourceOpt = sourceRepository
                .findOneBySourceId(minimalSource.getSourceId());
        if (!sourceOpt.isPresent()) {
            throw new IllegalArgumentException("Source ID " + minimalSource.getSourceId()
                    + " not found");
        }
        Source source = sourceOpt.get();
        source.setAssigned(minimalSource.isAssigned());
        return source;
    }

    @Override
    public Source sourceDTOToSource(SourceDTO sourceDto) {
        Source source = delegate.sourceDTOToSource(sourceDto);
        if (sourceDto.getId() != null) {
            Source existingSource = sourceRepository.findOne(sourceDto.getId());
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
