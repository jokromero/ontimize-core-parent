package com.ontimize.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontimize.locator.EntityReferenceLocator;

public class LocalEntityInvocationHandler implements InvocationHandler, Entity, DynamicMemoryEntity {

    private static final Logger logger = LoggerFactory.getLogger(LocalEntityInvocationHandler.class);

    protected String entityName;

    protected EntityReferenceLocator locator;

    protected Hashtable<String, Object> entityMetadata;

    protected EntityResult cacheData = new EntityResult(EntityResult.OPERATION_SUCCESSFUL,
            EntityResult.BEST_COMPRESSION);

    public LocalEntityInvocationHandler(EntityReferenceLocator locator, String entityName) {
        this.locator = locator;
        this.entityName = entityName;
        try {
            MetadataEntity currentEntity = (MetadataEntity) this.locator.getEntityReference(this.entityName);
            this.entityMetadata = currentEntity.getMetadata(this.locator.getSessionId());
        } catch (Exception e) {
            LocalEntityInvocationHandler.logger.error("Retrieve entity metadata : {}", e.getMessage(), e);
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass().equals(Entity.class)) {
            return method.invoke(this, args);
        } else if (method.getDeclaringClass().equals(DynamicMemoryEntity.class)) {
            return method.invoke(this, args);
        }
        return method.invoke(this.locator.getEntityReference(this.entityName), args);
    }

    @Override
    public EntityResult insert(Hashtable attributesValues, int sessionId) throws Exception {
        this.checkInsertKeys(attributesValues);
        String autonumerical = this.getAutonumerical();
        List<String> generatedKeyList = this.getGeneratedKeyList();
        EntityResult entityResult = new EntityResult(EntityResult.OPERATION_SUCCESSFUL, EntityResult.BEST_COMPRESSION);
        if (autonumerical != null) {
            attributesValues.put(autonumerical, this.cacheData.calculateRecordNumber());
            entityResult.put(autonumerical, this.cacheData.calculateRecordNumber());
        } else if (generatedKeyList != null) {
            for (String currentKey : generatedKeyList) {
                if (currentKey != null) {
                    attributesValues.put(currentKey, this.cacheData.calculateRecordNumber());
                    entityResult.put(currentKey, this.cacheData.calculateRecordNumber());
                }
            }
        }
        this.cacheData.addRecord(attributesValues);
        return entityResult;
    }

    @Override
    public EntityResult update(Hashtable attributesValues, Hashtable keysValues, int sessionId) throws Exception {
        int index = EntityResultTools.getValuesKeysIndex(this.cacheData, keysValues);
        EntityResultTools.updateRecordValues(this.cacheData, attributesValues, index);
        return new EntityResult(EntityResult.OPERATION_SUCCESSFUL, EntityResult.BEST_COMPRESSION);
    }

    @Override
    public EntityResult query(Hashtable keysValues, Vector attributes, int sessionId) throws Exception {
        if (keysValues.isEmpty()) {
            EntityResult entityResult = new EntityResult(this.cacheData);
            return entityResult;
        } else {
            int index = EntityResultTools.getValuesKeysIndex(this.cacheData, keysValues);
            EntityResult entityResult = new EntityResult(EntityResult.OPERATION_SUCCESSFUL,
                    EntityResult.BEST_COMPRESSION);
            entityResult.addRecord(this.cacheData.getRecordValues(index));
            return entityResult;
        }
    }

    /**
     * Checks if <code>attributesValues</code> contains a value for all columns defined in 'insert_keys'
     * parameter.
     * <p>
     * @param attributesValues
     * @throws Exception if any of the columns defined in 'insert_keys' is not found as key in
     *         <code>attributesValues</code>
     */
    public void checkInsertKeys(Hashtable attributesValues) throws Exception {
        Vector insertKeys = (Vector) this.entityMetadata.get(MetadataEntity.INSERT_KEYS);
        String autonumericalColumn = null;
        if (this.entityMetadata.containsKey(MetadataEntity.AUTONUMERICAL)) {
            autonumericalColumn = (String) this.entityMetadata.get(MetadataEntity.AUTONUMERICAL);
        }

        for (int i = 0; i < insertKeys.size(); i++) {
            if (!attributesValues.containsKey(insertKeys.get(i).toString())
                    && ((autonumericalColumn == null) || !autonumericalColumn.equals(insertKeys.get(i)))) {
                throw new Exception("M_NECESSARY_" + insertKeys.get(i).toString().toUpperCase());
            }
        }
    }

    protected String getAutonumerical() {
        if (this.entityMetadata.containsKey(MetadataEntity.AUTONUMERICAL)) {
            return (String) this.entityMetadata.get(MetadataEntity.AUTONUMERICAL);
        }
        return null;
    }

    protected List<String> getGeneratedKeyList() {
        if (this.entityMetadata.containsKey(MetadataEntity.GENERATED_KEY)) {
            return (List<String>) this.entityMetadata.get(MetadataEntity.GENERATED_KEY);
        }
        return null;
    }

    @Override
    public EntityResult delete(Hashtable keysValues, int sessionId) throws Exception {
        EntityResult erResult = new EntityResult();
        int index = EntityResultTools.getValuesKeysIndex(this.cacheData, keysValues);
        if (index >= 0) {
            this.cacheData.deleteRecord(index);
            return erResult;
        } else {
            erResult.setCode(EntityResult.OPERATION_SUCCESSFUL_SHOW_MESSAGE);
            erResult.setMessage("M_NO_RECORD_DELETED");
            LocalEntityInvocationHandler.logger
                .debug("Delete: keys parameter does not contain any pair key-value valid");
        }
        return null;
    }

    @Override
    public void setValue(EntityResult data) {
        if (data == null) {
            this.cacheData = new EntityResult(EntityResult.OPERATION_SUCCESSFUL, EntityResult.BEST_COMPRESSION);
        } else {
            this.cacheData = data;
        }
    }

    @Override
    public void clear() {
        this.cacheData = new EntityResult(EntityResult.OPERATION_SUCCESSFUL, EntityResult.BEST_COMPRESSION);
    }

}
