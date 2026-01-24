package org.MoreMagicSpell;

import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

import org.MoreMagicSpell.Interactions.SendMessageInteraction;
import org.MoreMagicSpell.Interactions.SpawnEntityInteraction;
import org.MoreMagicSpell.Systems.PetrifiedSystem;
import org.MoreMagicSpell.Components.PetrifiedComponent;
import org.MoreMagicSpell.Interactions.PetrifiedEntityInteraction;

/**
 * This class serves as the entrypoint for your plugin. Use the setup method to register into game registries or add
 * event listeners.
 */
public class ExamplePlugin extends JavaPlugin {

    private static ExamplePlugin instance;

    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private ComponentType<EntityStore, PetrifiedComponent> PetrifiedComponentType;

    public ExamplePlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        LOGGER.atInfo().log("Hello from " + this.getName() + " version " + this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up plugin " + this.getName());
        this.getCommandRegistry().registerCommand(new ExampleCommand(this.getName(), this.getManifest().getVersion().toString()));

        // Register Interactions
        this.getCodecRegistry(Interaction.CODEC).register("my_custom_interaction_id", SendMessageInteraction.class, SendMessageInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("petrified_entity_interaction", PetrifiedEntityInteraction.class, PetrifiedEntityInteraction.CODEC);
        this.getCodecRegistry(Interaction.CODEC).register("spawn_entity_interaction", SpawnEntityInteraction.class, SpawnEntityInteraction.CODEC);
        
        // Register Components
        PetrifiedComponentType = getEntityStoreRegistry().registerComponent(
            PetrifiedComponent.class,
            "Petrified",
            PetrifiedComponent.CODEC
        );

        // Register Systems
        this.getEntityStoreRegistry().registerSystem(new PetrifiedSystem(this.PetrifiedComponentType));
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Shutting down plugin " + this.getName());
    }


    // Acces to component types
    public ComponentType<EntityStore, PetrifiedComponent> getPetrifiedComponentType() {
        return PetrifiedComponentType;
    }

    public static ExamplePlugin get() {
        return instance;
    }

}