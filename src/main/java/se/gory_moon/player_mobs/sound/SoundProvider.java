package se.gory_moon.player_mobs.sound;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.RegistrateProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import net.minecraftforge.fml.LogicalSide;
import se.gory_moon.player_mobs.utils.CustomRegistrate;

public class SoundProvider extends SoundDefinitionsProvider implements RegistrateProvider {

    private final AbstractRegistrate<?> owner;

    /**
     * Creates a new instance of this data provider.
     *
     * @param generator The data generator instance provided by the event you are initializing this provider in.
     * @param helper    The existing file helper provided by the event you are initializing this provider in.
     */
    public SoundProvider(AbstractRegistrate<?> owner, DataGenerator generator, ExistingFileHelper helper) {
        super(generator, owner.getModid(), helper);
        this.owner = owner;
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    @Override
    public void registerSounds() {
        owner.genData(CustomRegistrate.SOUND, this);
    }

    /**
     * Creates a new sound with minecraft as the base
     *
     * @param name the path/name of the sound
     * @return a new  {@link SoundDefinition.Sound}
     */
    public SoundDefinition.Sound createMcSound(final String name) {
        return SoundDefinitionsProvider.sound(name);
    }

    /**
     * Creates a new sound with the current mod as the base
     *
     * @param name the path/name of the sound
     * @return a new  {@link SoundDefinition.Sound}
     */
    public SoundDefinition.Sound createSound(final String name) {
        return SoundDefinitionsProvider.sound(new ResourceLocation(owner.getModid(), name));
    }

    protected SoundDefinition subtitle(String subtitle) {
        return SoundDefinitionsProvider.definition().subtitle(subtitle);
    }

    public SoundDefinition noSubtitle() {
        return SoundDefinitionsProvider.definition();
    }

    @Override
    protected void add(ResourceLocation soundEvent, SoundDefinition definition) {
        super.add(soundEvent, definition);
    }

    @Override
    protected void add(String soundEvent, SoundDefinition definition) {
        super.add(soundEvent, definition);
    }
}
