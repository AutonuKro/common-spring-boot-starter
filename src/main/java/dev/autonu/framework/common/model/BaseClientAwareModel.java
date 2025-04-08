package dev.autonu.framework.common.model;

import dev.autonu.framework.common.context.ClientAwareModelListener;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * This class has to extended by any other entity class.
 *
 * @author autonu2X
 */
@MappedSuperclass
@EntityListeners(ClientAwareModelListener.class)
public class BaseClientAwareModel<ID extends Serializable> extends AbstractClientAwareModel<ID> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private ID id;

    public ID getId(){
        return id;
    }

    public void setId(ID id){
        this.id = id;
    }

    @Column(name = "client_id")
    public Integer getClientId(){
        return clientId;
    }

    public void setClientId(Integer clientId){
        this.clientId = clientId;
    }

    @Column(name = "created_at", updatable = false)
    public ZonedDateTime getCreatedAt(){
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt){
        this.createdAt = createdAt;
    }

    @Column(name = "updated_at")
    public ZonedDateTime getUpdatedAt(){
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt){
        this.updatedAt = updatedAt;
    }

    @Column(name = "created_by")
    public String getCreatedBy(){
        return createdBy;
    }

    public void setCreatedBy(String createdBy){
        this.createdBy = createdBy;
    }

    @Column(name = "updated_by")
    public String getUpdatedBy(){
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy){
        this.updatedBy = updatedBy;
    }

    @Version
    private Integer version;
}
