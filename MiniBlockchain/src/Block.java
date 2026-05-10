import java.util.HashMap;
import java.util.Map;


public class Block {
    private String index;
    private String timestamp;
    private String dataEnc; 
    private String iv; 
    private String hashPrev; 
    private String owner;
    private String hash; 

    
    private String dataRaw; 

    public Block() {}

    public Block(String index, String timestamp, String dataEnc, String iv, String hashPrev, String owner, String hash) {
        this.index = index;
        this.timestamp = timestamp;
        this.dataEnc = dataEnc;
        this.iv = iv;
        this.hashPrev = hashPrev;
        this.owner = owner;
        this.hash = hash;
    }

    
    public String getIndex() { return index; }
    public void setIndex(String index) { this.index = index; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getDataEnc() { return dataEnc; }
    public void setDataEnc(String dataEnc) { this.dataEnc = dataEnc; }
    public String getIv() { return iv; }
    public void setIv(String iv) { this.iv = iv; }
    public String getHashPrev() { return hashPrev; }
    public void setHashPrev(String hashPrev) { this.hashPrev = hashPrev; }
    public String getOwner() { return owner; }
    public void setOwner(String owner) { this.owner = owner; }
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public String getDataRaw() { return dataRaw; }
    public void setDataRaw(String dataRaw) { this.dataRaw = dataRaw; }

    
    public byte[] getBytesForHash() {
        return BlockchainUtils.concatenate(
            BlockchainUtils.strToBytes(index),
            BlockchainUtils.strToBytes(timestamp),
            BlockchainUtils.strToBytes(dataEnc),
            BlockchainUtils.strToBytes(iv),
            BlockchainUtils.strToBytes(hashPrev),
            BlockchainUtils.strToBytes(owner)
        );
    }

    
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("index", index);
        map.put("timestamp", timestamp);
        map.put("dataEnc", dataEnc);
        map.put("iv", iv);
        map.put("hashPrev", hashPrev);
        map.put("owner", owner);
        map.put("hash", hash);
        return map;
    }

    
    public static Block fromMap(Map<String, String> map) {
        return new Block(
            map.get("index"),
            map.get("timestamp"),
            map.get("dataEnc"),
            map.get("iv"),
            map.get("hashPrev"),
            map.get("owner"),
            map.get("hash")
        );
    }
}
