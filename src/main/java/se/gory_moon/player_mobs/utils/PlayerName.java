package se.gory_moon.player_mobs.utils;

import net.minecraft.util.StringUtil;

import javax.annotation.Nullable;

public record PlayerName(String skinName, @Nullable String displayName) {

    public static PlayerName create(String combined) {
        String[] parts = combined.split(":", 2);
        return new PlayerName(parts[0], parts.length > 1 ? parts[1] : null);
    }

    public String getCombinedNames() {
        if (StringUtil.isNullOrEmpty(displayName) || skinName.equals(displayName)) {
            return skinName;
        } else {
            return skinName + ":" + displayName;
        }
    }

    public boolean noDisplayName() {
        return displayName == null;
    }

    public boolean isInvalid() {
        return StringUtil.isNullOrEmpty(skinName);
    }

    @Override
    public String displayName() {
        if (!StringUtil.isNullOrEmpty(displayName)) {
            return displayName;
        } else {
            return skinName;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof PlayerName other) {
            return this.getCombinedNames().equals(other.getCombinedNames());
        }
        return false;
    }

    @Override
    public String toString() {
        return getCombinedNames();
    }
}
