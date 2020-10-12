package com.guman.raft.store;

import com.alibaba.fastjson.JSON;
import com.guman.raft.Store;
import com.guman.raft.common.util.NodeIdUtil;
import com.guman.raft.model.EntryParam;
import com.guman.raft.model.Pair;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Created by guman on 2020/3/24.
 */
public class DefaultStore implements Store {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStore.class);

    /**
     * public just for test
     */
    public static String dbDir;
    public static String stateMachineDir;

    public static RocksDB machineDb;

    static {
        if (dbDir == null) {
            dbDir = "./rocksDB-raft/" + NodeIdUtil.getNodeId();
        }
        if (stateMachineDir == null) {
            stateMachineDir = dbDir + "/stateMachine";
        }
        RocksDB.loadLibrary();
    }


    private DefaultStore() {
        synchronized (this) {
            try {
                File file = new File(stateMachineDir);
                boolean success = false;
                if (!file.exists()) {
                    success = file.mkdirs();
                }
                if (success) {
                    LOGGER.warn("make a new dir : " + stateMachineDir);
                }
                Options options = new Options();
                options.setCreateIfMissing(true);
                machineDb = RocksDB.open(options, stateMachineDir);

            } catch (RocksDBException e) {
                LOGGER.info(e.getMessage());
            }
        }
    }

    public static DefaultStore getInstance() {
        return DefaultStoreLazyHolder.INSTANCE;
    }

    private static class DefaultStoreLazyHolder {

        private static final DefaultStore INSTANCE = new DefaultStore();
    }

    @Override
    public EntryParam get(String key) {
        try {
            byte[] result = machineDb.get(key.getBytes());
            if (result == null) {
                return null;
            }
            return JSON.parseObject(result, EntryParam.class);
        } catch (RocksDBException e) {
            LOGGER.info(e.getMessage());
        }
        return null;
    }

    @Override
    public String getString(String key) {
        try {
            byte[] bytes = machineDb.get(key.getBytes());
            if (bytes != null) {
                return new String(bytes);
            }
        } catch (RocksDBException e) {
            LOGGER.info(e.getMessage());
        }
        return "";
    }

    @Override
    public void setString(String key, String value) {
        try {
            machineDb.put(key.getBytes(), value.getBytes());
        } catch (RocksDBException e) {
            LOGGER.info(e.getMessage());
        }
    }

    @Override
    public void delString(String... key) {
        try {
            for (String s : key) {
                machineDb.delete(s.getBytes());
            }
        } catch (RocksDBException e) {
            LOGGER.info(e.getMessage());
        }
    }

    @Override
    public synchronized void apply(EntryParam entryParam) {

        try {
            Pair pair = entryParam.getPair();

            if (pair == null) {
                throw new IllegalArgumentException("pair can not be null, entryParam : " + entryParam.toString());
            }
            String key = pair.getKey();
            machineDb.put(key.getBytes(), JSON.toJSONBytes(entryParam));
        } catch (RocksDBException e) {
            LOGGER.info(e.getMessage());
        }
    }
}
