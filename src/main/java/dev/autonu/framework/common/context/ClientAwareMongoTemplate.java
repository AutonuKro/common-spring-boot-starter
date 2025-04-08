package dev.autonu.framework.common.context;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.CountOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import dev.autonu.framework.common.error.InvalidClientUserAssociationException;
import dev.autonu.framework.common.model.BaseClientAwareMongoModel;
import dev.autonu.framework.common.model.ClientUserAssociation;
import dev.autonu.framework.common.properties.DateTimeFormatProperties;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.*;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.convert.MongoWriter;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.UpdateDefinition;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * This will try to mimic RLS(sort of) for MongoDB by overriding {@link MongoTemplate}.
 * Before doing any CRUD operations set client id from {@link ClientContext}
 *
 * @author autonu2X
 */
public class ClientAwareMongoTemplate extends MongoTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientAwareMongoTemplate.class);
    private final DateTimeFormatProperties dateTimeFormatProperties;
    protected static final String FIELD_CLIENT_ID = "client_id";

    public ClientAwareMongoTemplate(MongoClient mongoClient, String databaseName, DateTimeFormatProperties dateTimeFormatProperties){
        super(mongoClient, databaseName);
        this.dateTimeFormatProperties = dateTimeFormatProperties;
    }

    public ClientAwareMongoTemplate(MongoDatabaseFactory mongoDbFactory, DateTimeFormatProperties dateTimeFormatProperties){
        super(mongoDbFactory);
        this.dateTimeFormatProperties = dateTimeFormatProperties;
    }

    public ClientAwareMongoTemplate(MongoDatabaseFactory mongoDbFactory, MongoConverter mongoConverter, DateTimeFormatProperties dateTimeFormatProperties){
        super(mongoDbFactory, mongoConverter);
        this.dateTimeFormatProperties = dateTimeFormatProperties;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected <T> Stream<T> doStream(Query query, Class<?> entityType, String collectionName, Class<T> returnType){
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(entityType, "Entity type must not be null");
        Assert.hasText(collectionName, "Collection name must not be null or empty");
        Assert.notNull(returnType, "ReturnType must not be null");
        Criteria criteria = validateClientIdAndGetCriteria();
        query.addCriteria(criteria);
        return super.doStream(query, entityType, collectionName, returnType);
    }

    @Override
    protected void executeQuery(Query query, String collectionName, DocumentCallbackHandler documentCallbackHandler, @Nullable CursorPreparer preparer){
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(collectionName, "CollectionName must not be null");
        Assert.notNull(documentCallbackHandler, "DocumentCallbackHandler must not be null");
        Criteria criteria = validateClientIdAndGetCriteria();
        query.addCriteria(criteria);
        super.executeQuery(query, collectionName, documentCallbackHandler, preparer);
    }

    @Override
    protected long doCount(CollectionPreparer collectionPreparer, String collectionName, Document filter, CountOptions options){
        Document tenantCriteria = validateClientIdAndGetDocumentCriteria();
        filter.putAll(tenantCriteria);
        return super.doCount(collectionPreparer, collectionName, filter, options);
    }

    @Override
    protected <T> T doInsert(String collectionName, T objectToSave, MongoWriter<T> writer){
        ClientUserAssociation association = validateModelAndClientId(objectToSave);
        updateObjectToSaveWithClientId(objectToSave, association);
        return super.doInsert(collectionName, objectToSave, writer);
    }

    @Override
    protected <T> Collection<T> doInsertAll(Collection<? extends T> listToSave, MongoWriter<T> writer){
        for (T objectToSave : listToSave) {
            ClientUserAssociation association = validateModelAndClientId(objectToSave);
            updateObjectToSaveWithClientId(objectToSave, association);
        }
        return super.doInsertAll(listToSave, writer);
    }

    @Override
    protected <T> T doSave(String collectionName, T objectToSave, MongoWriter<T> writer){
        ClientUserAssociation association = validateModelAndClientId(objectToSave);
        updateObjectToSaveWithClientId(objectToSave, association);
        return super.doSave(collectionName, objectToSave, writer);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected UpdateResult doUpdate(String collectionName, Query query, UpdateDefinition update, @Nullable Class<?> entityClass, boolean upsert, boolean multi){
        Assert.notNull(collectionName, "CollectionName must not be null");
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(update, "Update must not be null");
        Criteria criteria = validateClientIdAndGetCriteria();
        query.addCriteria(criteria);
        return super.doUpdate(collectionName, query, update, entityClass, upsert, multi);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected <T> DeleteResult doRemove(String collectionName, Query query, @Nullable Class<T> entityClass, boolean multi){
        Assert.notNull(query, "Query must not be null");
        Assert.hasText(collectionName, "Collection name must not be null or empty");
        Criteria criteria = validateClientIdAndGetCriteria();
        query.addCriteria(criteria);
        return super.doRemove(collectionName, query, entityClass, multi);
    }

    @Override
    protected <S, T> UpdateResult replace(Query query, Class<S> entityType, T replacement, ReplaceOptions options, String collectionName){
        Assert.notNull(query, "Query must not be null");
        Assert.notNull(replacement, "Replacement must not be null");
        Assert.notNull(options, "Options must not be null Use ReplaceOptions#none() instead");
        Assert.notNull(entityType, "EntityType must not be null");
        Assert.notNull(collectionName, "CollectionName must not be null");
        Assert.isTrue(query.getLimit() <= 1, "Query must not define a limit other than 1 ore none");
        Assert.isTrue(query.getSkip() <= 0, "Query must not define skip");
        Criteria criteria = validateClientIdAndGetCriteria();
        query.addCriteria(criteria);
        return super.replace(query, entityType, replacement, options, collectionName);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected <O> AggregationResults<O> doAggregate(Aggregation aggregation, String collectionName, Class<O> outputType, AggregationOperationContext context){
        Criteria criteria = validateClientIdAndGetCriteria();
        aggregation.getPipeline()
                .add(Aggregation.match(criteria));
        return super.doAggregate(aggregation, collectionName, outputType, context);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected <O> Stream<O> aggregateStream(Aggregation aggregation, String collectionName, Class<O> outputType, @Nullable AggregationOperationContext context){
        Criteria criteria = validateClientIdAndGetCriteria();
        aggregation.getPipeline()
                .add(Aggregation.match(criteria));
        return super.aggregateStream(aggregation, collectionName, outputType, context);
    }

    @Nullable
    @Override
    protected <T> T doFindOne(String collectionName, CollectionPreparer<MongoCollection<Document>> collectionPreparer, Document query, Document fields, CursorPreparer preparer, Class<T> entityClass){
        Document tenantCriteria = validateClientIdAndGetDocumentCriteria();
        query.putAll(tenantCriteria);
        return super.doFindOne(collectionName, collectionPreparer, query, fields, preparer, entityClass);
    }

    @Override
    protected <T> List<T> doFind(String collectionName, CollectionPreparer<MongoCollection<Document>> collectionPreparer, Document query, Document fields, Class<T> entityClass, CursorPreparer preparer){
        Document tenantCriteria = validateClientIdAndGetDocumentCriteria();
        List<T> results;
        if (query.isEmpty()) {
            results = super.doFind(collectionName, collectionPreparer, tenantCriteria, fields, entityClass, preparer);
        } else {
            query.putAll(tenantCriteria);
            results = super.doFind(collectionName, collectionPreparer, query, fields, entityClass, preparer);
        }
        return results;
    }

    @Override
    protected <T> T doFindAndRemove(CollectionPreparer collectionPreparer, String collectionName, Document query, Document fields, Document sort, @Nullable Collation collation, Class<T> entityClass){
        Document tenantCriteria = validateClientIdAndGetDocumentCriteria();
        query.putAll(tenantCriteria);
        return super.doFindAndRemove(collectionPreparer, collectionName, query, fields, sort, collation, entityClass);
    }

    @Override
    protected <T> T doFindAndModify(CollectionPreparer collectionPreparer, String collectionName, Document query, Document fields, Document sort, Class<T> entityClass, UpdateDefinition update, @Nullable FindAndModifyOptions options){
        Document tenantCriteria = validateClientIdAndGetDocumentCriteria();
        query.putAll(tenantCriteria);
        return super.doFindAndModify(collectionPreparer, collectionName, query, fields, sort, entityClass, update, options);
    }

    @Nullable
    @Override
    protected <T> T doFindAndReplace(CollectionPreparer collectionPreparer, String collectionName, Document mappedQuery, Document mappedFields, Document mappedSort, @Nullable com.mongodb.client.model.Collation collation, Class<?> entityType, Document replacement, FindAndReplaceOptions options, Class<T> resultType){
        Document tenantCriteria = validateClientIdAndGetDocumentCriteria();
        mappedQuery.putAll(tenantCriteria);
        return super.doFindAndReplace(collectionPreparer, collectionName, mappedQuery, mappedFields, mappedSort, collation, entityType, replacement, options, resultType);
    }

    @Override
    public <T> List<T> findAll(Class<T> entityClass, String collectionName){
        Integer clientId = validateClientId().clientId();
        List<T> results = super.findAll(entityClass, collectionName);
        final List<T> filteredResult = new ArrayList<>();
        for (T result : results) {
            if (!(result instanceof BaseClientAwareMongoModel baseClientAwareMongoModel)) {
                throw new IllegalArgumentException("Model should be of type " + BaseClientAwareMongoModel.class + " .Provided " + result.getClass());
            }
            if (clientId.equals(baseClientAwareMongoModel.getClientId())) {
                filteredResult.add(result);
            }
        }
        return filteredResult;
    }

    private <T> ClientUserAssociation validateModelAndClientId(T objectToSave){
        if (!(objectToSave instanceof BaseClientAwareMongoModel)) {
            throw new IllegalArgumentException("Model should be of type " + BaseClientAwareMongoModel.class + " .Provided " + objectToSave.getClass());
        }
        return validateClientId();
    }

    private Criteria validateClientIdAndGetCriteria(){
        int clientId = validateClientId().clientId();
        return Criteria.where(FIELD_CLIENT_ID)
                .is(clientId);
    }

    private Document validateClientIdAndGetDocumentCriteria(){
        int clientId = validateClientId().clientId();
        return Query.query(Criteria.where(FIELD_CLIENT_ID)
                        .is(clientId))
                .getQueryObject();
    }

    private ClientUserAssociation validateClientId(){
        ClientUserAssociation association = ClientContext.get();
        if (association == null) {
            throw new InvalidClientUserAssociationException("Query performed is not allowed. Invalid client_id: null");
        }
        if (association.clientId() == null) {
            throw new InvalidClientUserAssociationException("Query performed is not allowed. Invalid client_id: null");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Query is being performed by client_id: {}", association.clientId());
        }
        return association;
    }

    private <T> void updateObjectToSaveWithClientId(T objectToSave, ClientUserAssociation association){
        BaseClientAwareMongoModel baseClientAwareMongoModel = (BaseClientAwareMongoModel) objectToSave;
        baseClientAwareMongoModel.setClientId(association.clientId());
        if (StringUtils.hasText(association.username())) {
            baseClientAwareMongoModel.setCreatedBy(association.username());
            baseClientAwareMongoModel.setUpdatedBy(association.username());
        } else {
            baseClientAwareMongoModel.setCreatedBy(String.valueOf(association.clientId()));
            baseClientAwareMongoModel.setUpdatedBy(String.valueOf(association.clientId()));
        }
        ZoneId zoneId = ZoneId.of(dateTimeFormatProperties.zone());
        baseClientAwareMongoModel.setCreatedAt(ZonedDateTime.now(zoneId));
        baseClientAwareMongoModel.setUpdatedAt(ZonedDateTime.now(zoneId));
    }
}
