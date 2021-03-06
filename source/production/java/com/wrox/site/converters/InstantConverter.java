package com.wrox.site.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Timestamp;
import java.time.Instant;

@Converter
public class InstantConverter implements AttributeConverter<Instant, Timestamp>
{
    @Override
    public Timestamp convertToDatabaseColumn(Instant instant)
    {
        if(instant==null) return null;
        return new Timestamp(instant.toEpochMilli());
    }

    @Override
    public Instant convertToEntityAttribute(Timestamp timestamp)
    {
        if(timestamp==null)
            return null;
        return Instant.ofEpochMilli(timestamp.getTime());
    }
}
