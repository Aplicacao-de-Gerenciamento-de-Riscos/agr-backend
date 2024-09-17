package com.catolicasc.agrbackend.shared.mapper;

import ma.glasnost.orika.MappingContext;
import ma.glasnost.orika.converter.BidirectionalConverter;
import ma.glasnost.orika.metadata.Type;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateConverter extends BidirectionalConverter<LocalDate, String> {

    @Override
    public String convertTo(LocalDate date, Type<String> type, MappingContext mappingContext) {
        return !StringUtils.isEmpty(date) ? DateTimeFormatter.ofPattern( "yyyy-MM-dd" ).format( date ) : null;
    }

    @Override
    public LocalDate convertFrom(String date, Type<LocalDate> type, MappingContext mappingContext) {
        return !StringUtils.isEmpty(date) ? LocalDate.parse( date, DateTimeFormatter.ofPattern( "yyyy-MM-dd" ) ) : null;
    }
}