package dev.autonu.framework.common.context;

import dev.autonu.framework.common.error.InvalidClientUserAssociationException;
import dev.autonu.framework.common.model.BaseClientAwareModel;
import dev.autonu.framework.common.model.ClientUserAssociation;
import dev.autonu.framework.common.properties.DateTimeFormatProperties;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * This will set clientUserAssociation related fields along with auditing fields
 *
 * @author autonu2X
 * @see BaseClientAwareModel
 */
@AutoConfiguration
@EnableConfigurationProperties(DateTimeFormatProperties.class)
public class ClientAwareModelListener {

    private final DateTimeFormatProperties dateTimeFormatProperties;

    public ClientAwareModelListener(DateTimeFormatProperties dateTimeFormatProperties){
        this.dateTimeFormatProperties = dateTimeFormatProperties;
    }

    /**
     * Set clientUserAssociation related fields along with auditing fields before saving
     *
     * @param objectToSave will never be {@literal null}
     */
    @PrePersist
    public void prePersist(Object objectToSave){
        if (!(objectToSave instanceof BaseClientAwareModel<?> baseClientAwareModel)) {
            throw new IllegalArgumentException("Model should be of type " + BaseClientAwareModel.class + " .Provided " + objectToSave.getClass());
        }
        ClientUserAssociation association = ClientContext.get();
        if (association == null) {
            throw new InvalidClientUserAssociationException("Invalid clientUserAssociation: null found");
        }
        baseClientAwareModel.setClientId(association.clientId());
        if (StringUtils.hasText(association.username())) {
            baseClientAwareModel.setCreatedBy(association.username());
            baseClientAwareModel.setUpdatedBy(association.username());
        } else {
            baseClientAwareModel.setCreatedBy(String.valueOf(association.clientId()));
            baseClientAwareModel.setUpdatedBy(String.valueOf(association.clientId()));
        }
        ZoneId zoneId = ZoneId.of(dateTimeFormatProperties.zone());
        baseClientAwareModel.setCreatedAt(ZonedDateTime.now(zoneId));
        baseClientAwareModel.setUpdatedAt(ZonedDateTime.now(zoneId));
    }

    /**
     * Update audit fields on update
     */
    @PreUpdate
    public void preUpdate(Object objectToSave){
        if (!(objectToSave instanceof BaseClientAwareModel<?> BaseClientAwareModel)) {
            throw new IllegalArgumentException("Model should be of type " + BaseClientAwareModel.class + " .Provided " + objectToSave.getClass());
        }
        ClientUserAssociation association = ClientContext.get();
        if (association == null) {
            throw new InvalidClientUserAssociationException("Invalid clientUserAssociation: null found");
        }
        if (StringUtils.hasText(association.username())) {
            BaseClientAwareModel.setUpdatedBy(association.username());
        } else {
            BaseClientAwareModel.setUpdatedBy(String.valueOf(association.clientId()));
        }
        ZoneId zoneId = ZoneId.of(dateTimeFormatProperties.zone());
        BaseClientAwareModel.setUpdatedAt(ZonedDateTime.now(zoneId));
    }
}
