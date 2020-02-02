package net.kraschitzer.intellij.plugin.sevenpace.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TokenDateDeserializer extends JsonDeserializer {

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        try {
            return LocalDateTime.parse(p.getValueAsString(), DateTimeFormatter.ofPattern("E, d MMM yyyy HH:mm:ss zzz"));
        } catch (DateTimeParseException ex) {
            throw new JsonParseException(p, "failed to parse date string", ex);
        }
    }
}
