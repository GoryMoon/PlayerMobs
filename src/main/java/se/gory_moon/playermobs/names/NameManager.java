package se.gory_moon.playermobs.names;

import se.gory_moon.playermobs.Configs;

import java.util.List;
import java.util.Random;

public class NameManager {

    public static final NameManager INSTANCE = new NameManager();

    public String getRandomName() {
        List<? extends String> names = Configs.COMMON.mobNames.get();
        return names.get(new Random().nextInt(names.size()));
    }

    public void useName(String name) {

    }
}
