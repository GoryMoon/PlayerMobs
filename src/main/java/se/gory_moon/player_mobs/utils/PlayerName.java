package se.gory_moon.player_mobs.utils;

import net.minecraft.util.StringUtil;
import javax.annotation.Nullable;

public class PlayerName {
    private String skinName;
    @Nullable
    private String displayName;

    public PlayerName(String combined) {
        String[] parts = combined.split(":", 2);
        skinName = parts[0];
        if (parts.length > 1) {
            displayName = parts[1];
        }
    }

    public PlayerName(String skin, @Nullable String display) {
        skinName = skin;
        if (!StringUtil.isNullOrEmpty(display)) {
            displayName = display;
        }
    }

    public String getCombinedNames() {
        if (StringUtil.isNullOrEmpty(displayName) || skinName.equals(displayName)) {
            return skinName;
        } else {
            return skinName + ":" + displayName;
        }
    }

    public void setNames(String name) {
        skinName = displayName = name;
    }

    public void setSkinName(String name) {
        skinName = name;
    }

    public String getSkinName() {
        return skinName;
    }

    public void setDisplayName(String display) {
        displayName = display;
    }

    public String getDisplayName() {
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
