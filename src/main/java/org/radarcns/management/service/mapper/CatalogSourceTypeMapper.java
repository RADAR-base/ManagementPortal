package org.radarcns.management.service.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.radarcns.management.domain.SourceType;
import org.radarcns.management.service.catalog.CatalogSourceType;

@Mapper(componentModel = "spring"  )
public interface CatalogSourceTypeMapper {

    @Mapping(source = "vendor", target = "producer")
    @Mapping(source = "version", target = "catalogVersion")
    @Mapping(source = "doc", target = "description")
    @Mapping(source = "scope", target = "sourceTypeScope")
    @Mapping(target = "sourceData" , ignore = true)
    @Mapping(target = "id" , ignore = true)
    @Mapping(target = "projects" , ignore = true)
    @Mapping(target = "canRegisterDynamically" , ignore = true)
    SourceType catalogSourceTypeToSourceType(CatalogSourceType catalogSourceType);

    List<SourceType> catalogSourceTypesToSourceTypes(List<CatalogSourceType> catalogSourceType);
}
