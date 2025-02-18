package com.example.api_v2.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.Locale;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@Converter(autoApply = true)
public class VectorConverter implements AttributeConverter<float[], String> {

    private static final DecimalFormat df;
    
    static {
        // Configurar el formato decimal para mantener la precisión
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        df = new DecimalFormat("0.#################", symbols);
        df.setMaximumFractionDigits(17); // Máxima precisión para float
    }

    @Override
    public String convertToDatabaseColumn(float[] vector) {
        if (vector == null) return null;
        if (vector.length == 0) return "[]";
        
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(df.format(vector[i]));
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public float[] convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        if (dbData.equals("[]")) return new float[0];
        
        try {
            // Remove the square brackets and split by comma
            String vectorStr = dbData.substring(1, dbData.length() - 1);
            String[] parts = vectorStr.split(",");
            float[] result = new float[parts.length];
            for (int i = 0; i < parts.length; i++) {
                result[i] = Float.parseFloat(parts[i].trim());
            }
            return result;
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Error al convertir el vector: " + e.getMessage(), e);
        }
    }
}
