package com.ontimize.dto;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.zip.Deflater;

public interface EntityResult {

    boolean DEBUG = false;

    boolean LIMIT_SPEED = false;

    int DATA_RESULT = 0;

    int NODATA_RESULT = 1;

    int OPERATION_SUCCESSFUL = 0;

    int OPERATION_WRONG = 1;

    int OPERATION_SUCCESSFUL_SHOW_MESSAGE = 2;

    int DEFAULT_COMPRESSION_THRESHOLD = Integer.MAX_VALUE;

    int DEFAULT_COMPRESSION = Deflater.DEFAULT_COMPRESSION;

    int NO_COMPRESSION = Deflater.NO_COMPRESSION;

    int BEST_COMPRESSION = Deflater.BEST_COMPRESSION;

    int BEST_SPEED = Deflater.BEST_SPEED;

    int HUFFMAN_ONLY = Deflater.HUFFMAN_ONLY;

    int MIN_BYTE_PROGRESS = 1024 * 50;

    int MIN_PERCENT_PROGRESS = 3;

    int byteBlock = 40 * 1024;// 40 K

    Object get(Object key);

    boolean containsKey(Object key);

    Object put(Object key, Object value)

    int calculateRecordNumber();

    class TimeUtil {

        long time = 0;

        public void setTime(long t) {
            this.time = t;
        }

        public long getTime() {
            return this.time;
        }

    }

    default int getValuesKeysIndex(EntityResult entityResult, Map kv) {

        // Check fast
        if (kv.isEmpty()) {
            return -1;
        }
        List vKeys = new ArrayList();
        Enumeration enumKeys = (Enumeration) kv.keySet();
        while (enumKeys.hasMoreElements()) {
            vKeys.add(enumKeys.nextElement());
        }

        Object vData = entityResult.get(vKeys.get(0));
        if ((vData == null) || (!(vData instanceof List))) {
            return -1;
        }
        int currentValueIndex = -1;

        if (vKeys.size() == 1) {
            return ((List) vData).indexOf(kv.get(vKeys.get(0)));
        }

        while ((currentValueIndex = ((List) vData).indexOf(kv.get(vKeys.get(0)), currentValueIndex + 1)) >= 0) {
            boolean allValuesCoincidence = true;
            for (int i = 1; i < vKeys.size(); i++) {
                Object requestValue = kv.get(vKeys.get(i));
                Object vDataAux = entityResult.get(vKeys.get(i));
                if ((vDataAux == null) || (!(vDataAux instanceof List))) {
                    return -1;
                }
                if (!requestValue.equals(((List) vDataAux).get(currentValueIndex))) {
                    allValuesCoincidence = false;
                    break;
                }
            }

            if (allValuesCoincidence) {
                return currentValueIndex;
            }
        }
        return -1;
    }

    default void main(String[] args) {
        List<String> columns = new ArrayList<String>();
        columns.add("test");
        EntityResult eR = new EntityResultMapImpl(columns);
        Map record = new HashMap<String, String>();
        record.put("test", "value");
        int total = 1000000;
        System.out.println("Creating " + total + " records");
        long startTime = System.nanoTime();
        for (int i = 0; i < total; i++) {
            ((EntityResultMapImpl) eR).addRecord(record);
        }
        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("Time to create the entity result  ->" + estimatedTime);

    }

}
