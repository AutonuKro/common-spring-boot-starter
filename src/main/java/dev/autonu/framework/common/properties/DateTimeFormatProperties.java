package dev.autonu.framework.common.properties;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.time.DateTimeException;
import java.time.ZoneId;

/**
 * @author autonu
 */
@Validated
@ConfigurationProperties(prefix = "common.starter.time")
public record DateTimeFormatProperties(
        @NotBlank(message = "Provide value for dev.autonu.time.zone") @DefaultValue(DEFAULT_ZONE) String zone,
        Format format) {

    public record Format(
            @NotBlank(message = "Provide value for dev.autonu.time.date") @DefaultValue(ISO_DATE) String date,
            @NotBlank(message = "Provide value for dev.autonu.time.datetime") @DefaultValue(ISO_DATE_TIME) String datetime) {

    }

    public DateTimeFormatProperties{
        if (format == null) {
            format = new Format(ISO_DATE, ISO_DATE_TIME);
        }
    }

    @AssertTrue(message = "Property value of dev.autonu.time.zone is not valid. Provide valid zone id")
    public boolean isValidZone(){
        try {
            ZoneId.of(zone);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }

    public static final String DEFAULT_ZONE = "Asia/Kolkata";

    public static final String ISO_DATE = "yyyy-MM-dd";

    public static final String ISO_DATE_TIME = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
}
