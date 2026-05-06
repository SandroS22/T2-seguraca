package org.ufsc.model;

public class Block {
    private Integer id;
    private String username;
    private byte[] iv;
    private byte[] encryptedData;
    private String prevHash;
    private long timestamp;

    public Block(Integer id, String username, byte[] iv, byte[] encryptedData, String prevHash, long timestamp) {
        this.id = id;
        this.username = username;
        this.iv = iv;
        this.encryptedData = encryptedData;
        this.prevHash = prevHash;
        this.timestamp = timestamp;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public byte[] getIv() {
        return iv;
    }

    public byte[] getEncryptedData() {
        return encryptedData;
    }

    public String getPrevHash() {
        return prevHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public void setEncryptedData(byte[] encryptedData) {
        this.encryptedData = encryptedData;
    }

    public void setPrevHash(String prevHash) {
        this.prevHash = prevHash;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}