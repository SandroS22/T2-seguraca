package org.ufsc.model;

public class User {
    private String username;
    private byte[] passwordHash;
    private byte[] salt;
    private String totpSecret;

    public User(String username, byte[] passwordHash, byte[] salt, String totpSecret) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.totpSecret = totpSecret;
    }

    public String getUsername() {
        return username;
    }

    public byte[] getPasswordHash() {
        return passwordHash;
    }

    public byte[] getSalt() {
        return salt;
    }

    public String getTotpSecret() {
        return totpSecret;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(byte[] passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public void setTotpSecret(String totpSecret) {
        this.totpSecret = totpSecret;
    }
}