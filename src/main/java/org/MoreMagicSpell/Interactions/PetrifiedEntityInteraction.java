package org.MoreMagicSpell.Interactions;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.ColorLight;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.ModelTrail;
import com.hypixel.hytale.protocol.Phobia;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.RespondToHit;
import com.hypixel.hytale.server.core.modules.entity.component.SnapshotBuffer;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import com.hypixel.hytale.server.core.modules.entity.component.ActiveAnimationComponent;
import com.hypixel.hytale.server.core.modules.entity.component.AudioComponent;

import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.annotation.Target;
import java.util.Map;

import javax.annotation.Nullable;

import org.MoreMagicSpell.MoreMagicSpell;
import org.MoreMagicSpell.Components.PetrifiedComponent;

public class PetrifiedEntityInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<PetrifiedEntityInteraction> CODEC = BuilderCodec.builder(
            PetrifiedEntityInteraction.class, PetrifiedEntityInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private static final String STONE_TEXTURE = "NPC/Textures/Petrified.png";
    private static final int PETRIFY_DURATION_MS = 5000;

    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            interactionContext.getState().state = InteractionState.Failed;
            LOGGER.atInfo().log("CommandBuffer is null");
            return;
        }

        Store<EntityStore> store = commandBuffer.getExternalData().getStore();
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

        // Get components from target
        ActiveAnimationComponent animationComponent = store.getComponent(target, ActiveAnimationComponent.getComponentType());
        ModelComponent modelComponent = store.getComponent(target, ModelComponent.getComponentType());
        TransformComponent transformComponent = store.getComponent(target, TransformComponent.getComponentType());

        // Save current animations
        String[] saved = null;
        if (animationComponent != null) {
            saved = animationComponent.getActiveAnimations();
        }
        // Save current model
        Model originalModel = null;
        if (modelComponent != null) {
            originalModel = modelComponent.getModel();
        }
        // Save current position
        double xP = transformComponent.getPosition().getX();
        double yP = transformComponent.getPosition().getY();
        double zP = transformComponent.getPosition().getZ();
        // Save current rotation
        float xR = transformComponent.getRotation().getX();
        float yR = transformComponent.getRotation().getY();
        float zR = transformComponent.getRotation().getZ();

        // Add PetrifiedComponent to target
        commandBuffer.addComponent(
        target,
        PetrifiedComponent.getComponentType(),
        new PetrifiedComponent(PETRIFY_DURATION_MS, saved, originalModel, xP, yP, zP, xR, yR, zR));
        
        // Replace ActiveAnimationComponent from target with empty animations
        commandBuffer.replaceComponent(
        target,
        ActiveAnimationComponent.getComponentType(),
        new ActiveAnimationComponent(new String[]{}));

        // Replace Texture to use StoneWall Texture but keep the same model
        Model stoneModel = new Model(
            modelComponent.getModel().getModelAssetId(),
            modelComponent.getModel().getScale(),
            modelComponent.getModel().getRandomAttachmentIds(),
            modelComponent.getModel().getAttachments(),
            modelComponent.getModel().getBoundingBox(),
            modelComponent.getModel().getModel(),
            STONE_TEXTURE,  // Only change this line to use stone texture
            modelComponent.getModel().getGradientSet(),
            modelComponent.getModel().getGradientId(),
            modelComponent.getModel().getEyeHeight(),
            modelComponent.getModel().getCrouchOffset(),
            Map.of(), // empty animation set map to prevent animation glitches
            modelComponent.getModel().getCamera(),
            modelComponent.getModel().getLight(),
            modelComponent.getModel().getParticles(),
            modelComponent.getModel().getTrails(),
            modelComponent.getModel().getPhysicsValues(),
            modelComponent.getModel().getDetailBoxes(),
            modelComponent.getModel().getPhobia(),
            modelComponent.getModel().getPhobiaModelAssetId()
        );
        commandBuffer.replaceComponent(
        target,
        ModelComponent.getComponentType(),
        new ModelComponent(stoneModel));

        World world = commandBuffer.getExternalData().getWorld();
        /* 
        IntList l = new IntArrayList();
        l.add(index);

        commandBuffer.addComponent(
        target,
        AudioComponent.getComponentType(),
        new AudioComponent(l));
            */
         

        // Play sound effect to around player
        int index = SoundEvent.getAssetMap().getIndex("SFX_Petrification"); 
        world.execute(() -> {
            TransformComponent transform = store.getComponent(target, EntityModule.get().getTransformComponentType());
            SoundUtil.playSoundEvent3d(target, index, transform.getPosition(), store);
        });
        
        AudioComponent audio = store.getComponent(
            target,
            AudioComponent.getComponentType()
        );
        if (audio == null) {
            audio = new AudioComponent();
            commandBuffer.addComponent(
                target,
                AudioComponent.getComponentType(),
                audio
            );
        }
        audio.addSound(index);
        

    }
}
