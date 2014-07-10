package com.mongodb.dibs;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.DBRef;
import org.bson.types.ObjectId;

import java.io.IOException;

public class JacksonMapper extends ObjectMapper {
    public JacksonMapper() {
        configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule module = new SimpleModule("jackson", Version.unknownVersion());
        module.addSerializer(new ObjectIdSerializer());
        module.addSerializer(new DBRefSerializer());
        registerModule(module);
    }

    private static class ObjectIdSerializer extends JsonSerializer<ObjectId> {
        @Override
        public Class<ObjectId> handledType() {
            return ObjectId.class;
        }

        @Override
        public void serialize(ObjectId id, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeString(id.toString());
        }
    }

    private static class DBRefSerializer extends JsonSerializer<DBRef> {
        @Override
        public Class<DBRef> handledType() {
            return DBRef.class;
        }

        @Override
        public void serialize(final DBRef value, final JsonGenerator generator, final SerializerProvider provider) throws IOException {
            generator.writeString(value.toString());
        }
    }
}
