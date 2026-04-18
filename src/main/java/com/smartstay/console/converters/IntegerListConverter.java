package com.smartstay.console.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

@Converter
public class IntegerListConverter implements AttributeConverter<List<Integer>, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(List<Integer> attribute) {
        try {
            if (attribute == null) return null;

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(attribute);
            out.flush();

            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error serializing list", e);
        }
    }

    @Override
    public List<Integer> convertToEntityAttribute(byte[] dbData) {
        try {
            if (dbData == null) return null;

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(dbData));
            return (List<Integer>) ois.readObject();

        } catch (Exception e) {
            throw new RuntimeException("Error deserializing list", e);
        }
    }
}
