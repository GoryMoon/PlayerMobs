package se.gory_moon.player_mobs.utils;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import se.gory_moon.player_mobs.sound.SoundBuilder;

public class CustomRegistrate extends AbstractRegistrate<CustomRegistrate> {

    public static CustomRegistrate create(String modid) {
        return new CustomRegistrate(modid).registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
    }

    protected CustomRegistrate(String modid) {
        super(modid);
    }

    public SoundBuilder<CustomRegistrate> sound() {
        return sound(this, currentName());
    }

    public <P> SoundBuilder<P> sound(P parent, String name) {
        return entry(name, callback -> new SoundBuilder<>(this, parent, name, callback, getModid()));
    }
}
