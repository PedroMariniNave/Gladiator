package com.zpedroo.gladiator.data;

import java.util.UUID;

public class PlayerData{

    private UUID uuid;
    private boolean trusted;

    public PlayerData(UUID uuid, boolean trusted) {
        this.uuid = uuid;
        this.trusted = trusted;
    }

    public UUID getUUID() {
        return uuid;
    }

    public boolean isTrusted() {
        return trusted;
    }

    public void setTrusted(boolean trusted) {
        this.trusted = trusted;
    }
}