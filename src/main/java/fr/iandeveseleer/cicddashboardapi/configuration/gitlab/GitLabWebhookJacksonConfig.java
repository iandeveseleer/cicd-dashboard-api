package fr.iandeveseleer.cicddashboardapi.configuration.gitlab;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Configuration
public class GitLabWebhookJacksonConfig {

    @Bean(name = "gitlabWebhookObjectMapper")
    public ObjectMapper gitlabWebhookObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new GitLabDateDeserializer());
        mapper.registerModule(module);

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);

        return mapper;
    }

    /**
     * Deserializer personnalisé pour gérer les différents formats de date de GitLab
     */
    public static class GitLabDateDeserializer extends DateDeserializers.DateDeserializer {
        private static final String[] DATE_FORMATS = {
            "yyyy-MM-dd'T'HH:mm:ssXXX",           // ISO-8601 format with timezone offset:2025-12-14T16:07:21+01:00
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",       // ISO-8601 format with milliseconds and timezone offset: 2025-12-14T16:07:21.000+01:00
            "yyyy-MM-dd HH:mm:ss 'UTC'",          // Simple UTC format: 2025-12-14 15:10:01 UTC
            "yyyy-MM-dd'T'HH:mm:ss'Z'",           // ISO-8601 Zulu format: 2025-12-14T16:07:21Z
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"        // ISO-8601 format with milliseconds Zulu
        };

        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String dateStr = p.getValueAsString();
            if (dateStr == null) {
                return null;
            }

            for (String format : DATE_FORMATS) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat(format);
                    sdf.setLenient(false);
                    return sdf.parse(dateStr);
                } catch (ParseException e) {
                    // continue
                }
            }

            // Else default deserialization
            return super.deserialize(p, ctxt);
        }
    }
}

