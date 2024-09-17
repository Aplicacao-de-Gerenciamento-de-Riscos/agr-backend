package com.catolicasc.agrbackend.shared.mapper;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeConverter extends BidirectionalConverter<LocalDateTime, String> {

    @Override
    public String convertTo(LocalDateTime date, Type<String> type, MappingContext mappingContext) {
        return !StringUtils.isEmpty(date) ? DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm" ).format( date ) : null;
    }

    @Override
    public LocalDateTime convertFrom(String date, Type<LocalDateTime> type, MappingContext mappingContext) {
        return !StringUtils.isEmpty(date) ? LocalDateTime.parse( date, DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm" ) ) : null;
    }
}
