package org.MoreMagicSpell;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

import org.MoreMagicSpell.Systems.*;
import org.MoreMagicSpell.Builtin.HexcodeBuiltin;
import org.MoreMagicSpell.Components.*;
import org.MoreMagicSpell.Interactions.*;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to
 * register into game registries or add
 * event listeners.
 */
public class MoreMagicSpell extends JavaPlugin {

    private static MoreMagicSpell instance;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private ComponentType<EntityStore, PetrifiedComponent> PetrifiedComponentType;
    private ComponentType<EntityStore, StoneWallComponent> StoneWallComponentType;

    public MoreMagicSpell(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());

        // Register Interactions
        this.getCodecRegistry(Interaction.CODEC).register("my_custom_interaction_id", SendMessageInteraction.class,
                SendMessageInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("petrified_entity_interaction",
                PetrifiedEntityInteraction.class, PetrifiedEntityInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("spawn_entity_interaction", SpawnStoneWallInteraction.class,
                SpawnStoneWallInteraction.CODEC);

        // Register Components
        PetrifiedComponentType = getEntityStoreRegistry().registerComponent(
                PetrifiedComponent.class,
                "Petrified",
                PetrifiedComponent.CODEC);
        StoneWallComponentType = getEntityStoreRegistry().registerComponent(
                StoneWallComponent.class,
                "StoneWall",
                StoneWallComponent.CODEC);

        // Register Systems
        this.getEntityStoreRegistry().registerSystem(new PetrifiedSystem(this.PetrifiedComponentType));
        this.getEntityStoreRegistry().registerSystem(new StoneWallSystem(this.StoneWallComponentType));

        if (isHexcodePresent()) {
            HexcodeBuiltin.Setup();
        } else {
            LOGGER.atInfo().log("Hexcode not installed");
        }

    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Shutting down plugin " + this.getName());
    }

    // Acces to component types
    public ComponentType<EntityStore, PetrifiedComponent> getPetrifiedComponentType() {
        return PetrifiedComponentType;
    }

    public ComponentType<EntityStore, StoneWallComponent> getStoneWallComponentType() {
        return StoneWallComponentType;
    }

    public static MoreMagicSpell get() {
        return instance;
    }

    private boolean isHexcodePresent() {
        PluginBase hexcode = PluginManager.get()
                .getPlugin(PluginIdentifier.fromString("Riprod:Hexcode"));
        return hexcode != null && hexcode.isEnabled();
    }

}