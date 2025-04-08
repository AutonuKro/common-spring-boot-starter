package dev.autonu.framework.common.model;

import jakarta.persistence.MappedSuperclass;

import java.time.ZonedDateTime;

/**
 * A base class for all kind of database entity.
 *
 * @author autonu2X
 */
@MappedSuperclass
abstract class AbstractClientAwareModel<ID> {

    protected Integer clientId;
    protected String createdBy;
    protected String updatedBy;
    protected ZonedDateTime createdAt;
    protected ZonedDateTime updatedAt;

    public abstract ID getId();

    public abstract void setId(ID id);

    public abstract Integer getClientId();

    public abstract void setClientId(Integer clientId);

    public abstract ZonedDateTime getCreatedAt();

    public abstract void setCreatedAt(ZonedDateTime createdAt);

    public abstract ZonedDateTime getUpdatedAt();

    public abstract void setUpdatedAt(ZonedDateTime updatedAt);

    public abstract String getCreatedBy();

    public abstract void setCreatedBy(String createdBy);

    public abstract String getUpdatedBy();

    public abstract void setUpdatedBy(String updatedBy);
}
