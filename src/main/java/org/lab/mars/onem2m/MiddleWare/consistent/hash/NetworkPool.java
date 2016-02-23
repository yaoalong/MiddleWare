package org.lab.mars.onem2m.MiddleWare.consistent.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.zookeeper.server.quorum.QuorumPeer.QuorumServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkPool {
    public static final int CONSISTENT_HASH = 3;
    private static Logger log = LoggerFactory.getLogger(NetworkPool.class);
    private static ThreadLocal<MessageDigest> MD5 = new ThreadLocal<MessageDigest>() {
        @Override
        protected final MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                if (log.isErrorEnabled())
                    log.error("++++ no md5 algorithm found");
                throw new IllegalStateException("++++ no md5 algorythm found");
            }
        }
    };
    private volatile List<String> servers;
    private volatile TreeMap<Long, String> consistentBuckets;

    private volatile TreeMap<Long, String> allConsistentBuckets;
    private volatile boolean initialized = false;
    private int hashingAlg = CONSISTENT_HASH;

    private volatile List<String> allServers;
    private volatile String mySelfIpAndPort;

    /**
     * server对应的位置
     */
    private ConcurrentHashMap<String, Long> allServerToPosition = new ConcurrentHashMap<String, Long>();

    /**
     * 位置对应的server
     */
    private ConcurrentHashMap<Long, String> allPositionToServer = new ConcurrentHashMap<Long, String>();

    protected HashMap<Long, QuorumServer> allQuorumServers = null;

    public static ConcurrentHashMap<String, Integer> webPort = new ConcurrentHashMap<String, Integer>();
    /**
     * server对应的位置
     */
    private ConcurrentHashMap<String, Long> serverToPosition = new ConcurrentHashMap<String, Long>();

    /**
     * 位置对应的server
     */
    private ConcurrentHashMap<Long, String> positionToServer = new ConcurrentHashMap<Long, String>();

    /**
     * Internal private hashing method.
     * <p>
     * MD5 based hash algorithm for use in the consistent hashing approach.
     *
     * @param key
     * @return
     */

    private volatile List<String> deadServers = new ArrayList<String>();

    private Integer replicationFactor;
    protected Map<String, Long> allServersToSid;

    public static long md5HashingAlg(String key) {
        MessageDigest md5 = MD5.get();
        md5.reset();
        md5.update(key.getBytes());
        byte[] bKey = md5.digest();
        long res = ((long) (bKey[3] & 0xFF) << 24)
                | ((long) (bKey[2] & 0xFF) << 16)
                | ((long) (bKey[1] & 0xFF) << 8) | (long) (bKey[0] & 0xFF);
        return res;
    }

    /*
     * 初始化
     */
    public synchronized void initialize() {
        try {

            // if servers is not set, or it empty, then
            // throw a runtime exception
            if (servers == null || servers.size() <= 0) {
                if (log.isErrorEnabled())
                    log.error("++++ trying to initialize with no servers");
                throw new IllegalStateException(
                        "++++ trying to initialize with no servers");
            }

            // only create up to maxCreate connections at once

            // initalize our internal hashing structures
            if (this.hashingAlg == CONSISTENT_HASH)
                populateConsistentBuckets();

            this.initialized = true;
        } catch (Exception ex) {
            log.error("error occur:{}", ex.getMessage());
        }
    }

    public void populateConsistentBuckets() {
        TreeMap<Long, String> newConsistentBuckets = new TreeMap<Long, String>();
        MessageDigest md5 = MD5.get();

        for (int i = 0; i < servers.size(); i++) {
            long factor = 1;
            for (long j = 0; j < factor; j++) {
                byte[] d = md5.digest((servers.get(i) + "-" + j).getBytes());
                for (int h = 0; h < 1; h++) {
                    Long k = ((long) (d[3 + h * 4] & 0xFF) << 24)
                            | ((long) (d[2 + h * 4] & 0xFF) << 16)
                            | ((long) (d[1 + h * 4] & 0xFF) << 8)
                            | ((long) (d[0 + h * 4] & 0xFF));

                    newConsistentBuckets.put(k, servers.get(i));
                }
            }
        }
        long position = 0;
        for (Map.Entry<Long, String> map : newConsistentBuckets.entrySet()) {
            serverToPosition.put(map.getValue(), position);
            positionToServer.put(position, map.getValue());
            position++;
        }
        this.consistentBuckets = newConsistentBuckets;
        initialized = true;
    }

    public final String getSock(String key) {
        if (initialized == false) {
            log.error("can't get sock becaus network is not intialzed!");
            throw new NullPointerException();
        }
        return consistentBuckets.get(getBucket(key));
    }

    /**
     * 获取处理某个key的所有server
     * 
     * @param key
     * @return
     */
    public final List<String> getAllSock(String key) {
        long hc = getHash(key);
        long result = findPointFor(hc);
        List<String> servers = new ArrayList<String>();
        for (int i = 0; i < replicationFactor; i++) {
            synchronized (deadServers) {

                if (!deadServers.contains(allConsistentBuckets.get(result))) {
                    servers.add(allConsistentBuckets.get(result));
                }
                result = findPointFor(result + 1);
            }

        }
        return servers;

    }

    private final long getBucket(String key) {
        long hc = getHash(key);
        long result = findPointFor(hc);
        return result;
    }

    private final Long findPointFor(Long hv) {
        synchronized (this.consistentBuckets) {

        }
        SortedMap<Long, String> tmap = this.consistentBuckets.tailMap(hv);

        return (tmap.isEmpty()) ? this.consistentBuckets.firstKey() : tmap
                .firstKey();
    }

    private final long getHash(String key) {
        return md5HashingAlg(key);
    }

    /**
     * 在设置最新的server列表 servers 所有存活的节点
     * 
     * 
     * @param servers
     */
    public synchronized void setServers(List<String> servers, boolean isOk) {
        this.servers = servers;

    }

    public ConcurrentHashMap<String, Long> getServerPosition() {
        return serverToPosition;
    }

    public void setServerPosition(ConcurrentHashMap<String, Long> serverPosition) {
        this.serverToPosition = serverPosition;
    }

    public ConcurrentHashMap<Long, String> getPositionToServer() {
        return positionToServer;
    }

    public void setPositionToServer(
            ConcurrentHashMap<Long, String> positionToServer) {
        this.positionToServer = positionToServer;
    }

    public List<String> getServers() {
        return servers;
    }

    public List<String> getAllServers() {
        return allServers;
    }

    /**
     * 让networkPool知道所有的节点
     * 
     * @param allServers
     */
    public void setAllServers(List<String> allServers) {
        this.allServers = allServers;
        TreeMap<Long, String> newConsistentBuckets = new TreeMap<Long, String>();
        MessageDigest md5 = MD5.get();

        for (int i = 0; i < allServers.size(); i++) {
            long factor = 1;
            for (long j = 0; j < factor; j++) {
                byte[] d = md5.digest((allServers.get(i) + "-" + j).getBytes());
                for (int h = 0; h < 1; h++) {
                    Long k = ((long) (d[3 + h * 4] & 0xFF) << 24)
                            | ((long) (d[2 + h * 4] & 0xFF) << 16)
                            | ((long) (d[1 + h * 4] & 0xFF) << 8)
                            | ((long) (d[0 + h * 4] & 0xFF));

                    newConsistentBuckets.put(k, allServers.get(i));
                }
            }
        }
        long position = 0;
        for (Map.Entry<Long, String> map : newConsistentBuckets.entrySet()) {
            allServerToPosition.put(map.getValue(), position);
            allPositionToServer.put(position, map.getValue());
            position++;
        }
        this.allConsistentBuckets = newConsistentBuckets;
    }

    public HashMap<Long, QuorumServer> getAllQuorumServers() {
        return allQuorumServers;
    }

    public void setAllPositionToServer(
            ConcurrentHashMap<Long, String> allPositionToServer) {
        this.allPositionToServer = allPositionToServer;
    }

    public String getMySelfIpAndPort() {
        return mySelfIpAndPort;
    }

    public void setMySelfIpAndPort(String mySelfIpAndPort) {
        this.mySelfIpAndPort = mySelfIpAndPort;
    }

    public Integer getReplicationFactor() {
        return replicationFactor;
    }

    public void setReplicationFactor(Integer replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public Map<String, Long> getAllServersToSid() {
        return allServersToSid;
    }

    public void setAllServersToSid(Map<String, Long> allServersToSid) {
        this.allServersToSid = allServersToSid;
    }

    public void setAllQuorumServers(HashMap<Long, QuorumServer> allQuorumServers) {
        this.allQuorumServers = allQuorumServers;
    }

    public TreeMap<Long, String> getAllConsistentBuckets() {
        return allConsistentBuckets;
    }

}
