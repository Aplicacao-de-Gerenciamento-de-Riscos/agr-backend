package com.catolicasc.agrbackend.shared.mapper;

import lombok.Getter;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory;

@Getter
public class MapperBase {

    private static MapperBase instance;

    private final MapperFactory mapperFactory;

    private MapperBase() {
        mapperFactory = new DefaultMapperFactory.Builder().build();
        mapperFactory.getConverterFactory().registerConverter(new LocalDateTimeConverter());
        mapperFactory.getConverterFactory().registerConverter(new LocalDateConverter());
        mapperFactory.getConverterFactory().registerConverter(new LocalTimeConverter());
    }

    public static synchronized MapperBase getInstance() {
        if (instance == null) {
            instance = new MapperBase();
        }
        return instance;
    }
}
