package org.ufsc.model;

public class Block {
    private Block next;
    private String message;
    private String username;
    private byte[] iv;
    private byte[] encryptedData;
    private String prevHash;
    private long timestamp;

    public Boolean hasNext() {
        return next != null;
    }

    public Boolean isFirstBlock() {
        return prevHash == null;
    }

    public Block next() {
        return next;
    }
}
