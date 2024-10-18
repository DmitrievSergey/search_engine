package searchengine.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import searchengine.dto.search.SearchDto;
import searchengine.entity.SearchEntity;

@Mapper(componentModel = "spring")
public interface SearchMapper {
    SearchMapper INSTANCE = Mappers.getMapper(SearchMapper.class);
    SearchDto convertToDto(SearchEntity search);
}