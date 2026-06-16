package com.smartstay.console.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstay.console.dto.settlement.CurrentOtherItems;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class CurrentOtherItemConverter implements AttributeConverter<List<CurrentOtherItems>, String> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public String convertToDatabaseColumn(List<CurrentOtherItems> currentOtherItems) {
        try {
            return objectMapper.writeValueAsString(currentOtherItems);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting to json");
        }
    }

    @Override
    public List<CurrentOtherItems> convertToEntityAttribute(String s) {
        try {
            if (s != null) {
                return objectMapper.readValue(s, new TypeReference<List<CurrentOtherItems>>() {});
            }
            return null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error reading the values");
        }
    }
}
