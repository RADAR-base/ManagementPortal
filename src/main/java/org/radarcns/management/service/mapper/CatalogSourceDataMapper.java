package org.radarcns.management.service.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.radarcns.management.domain.SourceData;
import org.radarcns.management.service.catalog.CatalogSourceData;

@Mapper(componentModel = "spring")
public interface CatalogSourceDataMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "type", target = "sourceDataType")
    @Mapping(target = "sourceDataName", ignore = true)
    @Mapping(source = "sampleRate.frequency", target = "frequency")
    @Mapping(target = "dataClass", ignore = true)
    @Mapping(source = "appProvider", target = "provider")
    @Mapping(target = "enabled", expression = "java(true)")
    @Mapping(target = "sourceType", ignore = true)
    SourceData catalogSourceDataToSourceData(CatalogSourceData catalogSourceData);

    List<SourceData> catalogSourceDataListToSourceDataList(
            List<CatalogSourceData> catalogSourceType);
}
