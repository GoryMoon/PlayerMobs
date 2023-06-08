package se.gory_moon.player_mobs.utils;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import se.gory_moon.player_mobs.sound.SoundBuilder;
import se.gory_moon.player_mobs.sound.SoundProvider;

public class CustomRegistrate extends AbstractRegistrate<CustomRegistrate> {

    public static final ProviderType<SoundProvider> SOUND = ProviderType.register("sound", (r, e) -> new SoundProvider(r, e.getGenerator().getPackOutput(), e.getExistingFileHelper()));

    public static CustomRegistrate create(String modId) {
        return new CustomRegistrate(modId).registerEventListeners(FMLJavaModLoadingContext.get().getModEventBus());
    }

    protected CustomRegistrate(String modId) {
        super(modId);
    }

    public SoundBuilder<CustomRegistrate> sound() {
        return sound(this, currentName());
    }

    public <P> SoundBuilder<P> sound(P parent, String name) {
        return entry(name, callback -> new SoundBuilder<>(this, parent, name, callback, getModid()));
    }
}
