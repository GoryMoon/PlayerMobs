package se.gory_moon.player_mobs.sound;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonnullType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.registries.ForgeRegistries;
import se.gory_moon.player_mobs.utils.CustomRegistrate;


@SuppressWarnings("unused")
public class SoundBuilder<P> extends AbstractBuilder<SoundEvent, SoundEvent, P, SoundBuilder<P>> {

    private final NonNullFunction<ResourceLocation, SoundEvent> factory;
    private final String modId;

    public SoundBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, String modId) {
        super(owner, parent, name, callback, ForgeRegistries.Keys.SOUND_EVENTS);
        this.modId = modId;
        this.factory = SoundEvent::createVariableRangeEvent;
    }

    @Override
    protected @NonnullType SoundEvent createEntry() {
        return factory.apply(new ResourceLocation(modId, getName()));
    }

    /**
     * Create a new {@link SoundBuilder} without a sound subtitle. Configure sound in the definition provider.
     *
     * @param definitionProvider A function to add sounds to the definition
     * @return this {@link SoundBuilder}
     */
    public SoundBuilder<P> sound(NonNullBiFunction<SoundDefinition, SoundProvider, SoundDefinition> definitionProvider) {
        return baseSound((provider, ctx) -> definitionProvider.apply(provider.noSubtitle(), provider));
    }

    /**
     * Create a new {@link SoundBuilder} with a block sound subtitle. Configure sound in the definition provider.
     *
     * @param definitionProvider A function to add sounds to the definition
     * @param subtitle           The english sound subtitle
     * @return this {@link SoundBuilder}
     */
    public SoundBuilder<P> blockSound(NonNullBiFunction<SoundDefinition, SoundProvider, SoundDefinition> definitionProvider, String subtitle) {
        return baseSound((provider, ctx) -> definitionProvider.apply(provider.subtitle(blockSubtitle(ctx.getId())), provider))
                .lang(sound -> blockSubtitle(sound.getLocation()), subtitle);
    }

    /**
     * Create a new {@link SoundBuilder} with a entity sound subtitle. Configure sound in the definition provider.
     *
     * @param definitionProvider A function to add sounds to the definition
     * @param subtitle           The english sound subtitle
     * @return this {@link SoundBuilder}
     */
    public SoundBuilder<P> entitySound(NonNullBiFunction<SoundDefinition, SoundProvider, SoundDefinition> definitionProvider, String subtitle) {
        return baseSound((provider, ctx) -> definitionProvider.apply(provider.subtitle(entitySubtitle(ctx.getId())), provider))
                .lang(sound -> entitySubtitle(sound.getLocation()), subtitle);
    }

    private SoundBuilder<P> baseSound(NonNullBiFunction<SoundProvider, DataGenContext<SoundEvent, SoundEvent>, SoundDefinition> soundDefinitionProvider) {
        return setData(CustomRegistrate.SOUND, (ctx, prov) -> prov.add(getName(), soundDefinitionProvider.apply(prov, ctx)));
    }

    private String entitySubtitle(ResourceLocation location) {
        return location.getNamespace() + ".subtitles.entity." + getName();
    }

    private String blockSubtitle(ResourceLocation location) {
        return location.getNamespace() + ".subtitles.block." + getName();
    }
}
