package com.ddm.ddm_backend.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.util.Set;
import java.util.stream.Collectors;

public class DtoUtils {

    public DtoEntity convertToDto(Object obj , DtoEntity mapper){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(obj, mapper.getClass());
    }

    public Object convertToEntity(Object obj, DtoEntity mapper){
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return modelMapper.map(mapper, obj.getClass());
    }

    public <DTOEntity, Object> Set<DTOEntity> convertToDtos(Set<Object> obj, DTOEntity mapper){

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return (Set<DTOEntity>) obj.stream().map(o -> modelMapper.map(o, mapper.getClass())).collect(Collectors.toSet());

    }

    public <DTOEntity, Object> Set<Object> convertToEntities(Object obj, Set<DTOEntity> mapper){

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        return (Set<Object>) mapper.stream().map(m -> modelMapper.map(m, obj.getClass())).collect(Collectors.toSet());

    }

}