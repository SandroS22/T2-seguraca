package org.ufsc.model;

public class Block {
    private Block previous;
    private Block next;
    private String message;

    public Boolean hasNext() {
        return next != null;
    }

    public Boolean isFirstBlock() {
        return previous == null;
    }

    public Block next() {
        return next;
    }
}
