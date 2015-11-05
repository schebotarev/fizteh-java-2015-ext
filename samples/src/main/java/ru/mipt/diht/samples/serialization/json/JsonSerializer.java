package ru.mipt.diht.samples.serialization.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author s.chebotarev
 * @since 05.11.2015
 */
public class JsonSerializer {
    private final ObjectMapper mapper;

    public JsonSerializer() {
        mapper = new ObjectMapper();
    }

    public String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("failed to create json from: " + object, e);
        }
    }

    public <T> T fromJson(String json, Class<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException("failed to read object from json: " + json, e);
        }
    }
}
