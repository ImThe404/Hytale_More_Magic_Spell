package org.MoreMagicSpell.Interactions;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.MoreMagicSpell.Components.PetrifiedComponent;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;

/**
 * Interaction that petrifies a target entity, changing its model to a stone texture and stopping its animations for a duration.
 */
public class PetrifiedEntityInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<PetrifiedEntityInteraction> CODEC = BuilderCodec.builder(
            PetrifiedEntityInteraction.class, PetrifiedEntityInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final String STONE_TEXTURE = "NPC/Textures/Petrified.png";
    private static final int PETRIFY_DURATION_MS = 5000;

    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        
        // Get CommandBuffer
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        
        // Verfication Checks for CommandBuffer, Player, and ItemStack
        if (commandBuffer == null) {
            interactionContext.getState().state = InteractionState.Failed;
            LOGGER.atInfo().log("CommandBuffer is null");
            return;
        }
        Store<EntityStore> store = commandBuffer.getExternalData().getStore();
        World world = commandBuffer.getExternalData().getWorld();
        Ref<EntityStore> ref = interactionContext.getEntity();
        Player player = commandBuffer.getComponent(ref, Player.getComponentType());
        if (player == null) {
            interactionContext.getState().state = InteractionState.Failed;
            LOGGER.atInfo().log("Player is null");
            return;
        }
        ItemStack itemStack = interactionContext.getHeldItem();
        if (itemStack == null) {
            interactionContext.getState().state = InteractionState.Failed;
            LOGGER.atInfo().log("ItemStack is null");
            return;
        }

        // Get target from context
        Ref<EntityStore> target = interactionContext.getTargetEntity();
        if (target == null) {
            return;
        } else {
            LOGGER.atInfo().log("Target entity found: " + target.toString());
        }

        // If the entity is a player, do not petrify
        Player targetPlayer = store.getComponent(target, Player.getComponentType());
        if (targetPlayer != null) {
            LOGGER.atInfo().log("Target entity is a player, cannot petrify.");
            return;
        }

        // Get components from target
        TransformComponent targetTransformComponent = store.getComponent(target, TransformComponent.getComponentType());
        ModelComponent targetModelComponent = store.getComponent(target, ModelComponent.getComponentType());
        ActiveAnimationComponent targetActiveAnimationComponent = store.getComponent(target, ActiveAnimationComponent.getComponentType());

        if (targetTransformComponent == null || targetModelComponent == null || targetActiveAnimationComponent == null) { // Security check
            LOGGER.atInfo().log("Target entity missing required components for petrification.");
            return;
        }

        // Create Pertrified Model
        Model targetModel = targetModelComponent.getModel();
        Model stoneModel = new Model(
            targetModel.getModelAssetId(),
            targetModel.getScale(),
            targetModel.getRandomAttachmentIds(),
            targetModel.getAttachments(),
            targetModel.getBoundingBox(),
            targetModel.getModel(),
            STONE_TEXTURE,  // Only change this line to use stone texture
            targetModel.getGradientSet(),
            targetModel.getGradientId(),
            targetModel.getEyeHeight(),
            targetModel.getCrouchOffset(),
            targetModel.getAnimationSetMap(),
            targetModel.getCamera(),
            targetModel.getLight(),
            targetModel.getParticles(),
            targetModel.getTrails(),
            targetModel.getPhysicsValues(),
            targetModel.getDetailBoxes(),
            targetModel.getPhobia(),
            targetModel.getPhobiaModelAssetId()
        );

        // Create an brainless Model of the target entity
        Holder<EntityStore> brainlessHolder = EntityStore.REGISTRY.newHolder();
        brainlessHolder.addComponent(TransformComponent.getComponentType(), targetTransformComponent); // keep same transform (position/rotation)
        brainlessHolder.addComponent(PersistentModel.getComponentType(), new PersistentModel(stoneModel.toReference())); 
        brainlessHolder.addComponent(ModelComponent.getComponentType(), new ModelComponent(stoneModel)); // use stone model
        brainlessHolder.addComponent(BoundingBox.getComponentType(), new BoundingBox(stoneModel.getBoundingBox()));
        brainlessHolder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
        brainlessHolder.addComponent(ActiveAnimationComponent.getComponentType(), targetActiveAnimationComponent); // keep same active animations
        brainlessHolder.addComponent(Intangible.getComponentType(), Intangible.INSTANCE); // make intangible so entity can't be hit while petrified
        brainlessHolder.addComponent(PhysicsValues.getComponentType(), new PhysicsValues(2.0, 0.5, false)); // basic physics values
        brainlessHolder.addComponent(Velocity.getComponentType(), new Velocity(new Vector3d(0, 0, 0)));
        

        // Save and remove Original Entity
        Holder<EntityStore> originalHolder = EntityStore.REGISTRY.newHolder();
        commandBuffer.removeEntity(target, originalHolder, RemoveReason.UNLOAD);
        PetrifiedComponent petrifiedComp = new PetrifiedComponent(PETRIFY_DURATION_MS, originalHolder);
        brainlessHolder.addComponent(PetrifiedComponent.getComponentType(), petrifiedComp); // add PetrifiedComponent to brainless entity

        // Add the brainless Model entity to the world, and play petrification sound
        int index = SoundEvent.getAssetMap().getIndex("SFX_Petrification"); // get petrification sound index
        world.execute(() -> {   
            store.addEntity(brainlessHolder, AddReason.SPAWN);
            SoundUtil.playSoundEvent3d(target, index, targetTransformComponent.getPosition(), store);
        });

    }
}
