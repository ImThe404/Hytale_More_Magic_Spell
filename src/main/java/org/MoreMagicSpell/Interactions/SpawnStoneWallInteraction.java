package org.MoreMagicSpell.Interactions;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.AudioComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import com.hypixel.hytale.server.npc.corecomponents.audiovisual.ActionAppearance;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.ActionPlayAnimation;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.ActionPlaySound;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.ActionSpawnParticles;

import com.hypixel.hytale.server.npc.animations.*;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders.BuilderActionPlayAnimation;

import com.hypixel.hytale.Main;

public class SpawnStoneWallInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<SpawnStoneWallInteraction> CODEC = BuilderCodec.builder(
            SpawnStoneWallInteraction.class, SpawnStoneWallInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Override
    protected void firstRun(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NonNullDecl CooldownHandler cooldownHandler) {
        CommandBuffer<EntityStore> commandBuffer = interactionContext.getCommandBuffer();
        if (commandBuffer == null) {
            interactionContext.getState().state = InteractionState.Failed;
            LOGGER.atInfo().log("CommandBuffer is null");
            return;
        }

        World world = commandBuffer.getExternalData().getWorld(); // just to show how to get the world if needed
        Store<EntityStore> store = commandBuffer.getExternalData().getStore(); // just to show how to get the store if needed
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

        player.sendMessage(Message.raw("You are using the custom item +" + itemStack.getItemId()));

        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();

        ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset("StoneWall");
        Model model = Model.createScaledModel(modelAsset, 1.0f);
        LOGGER.atInfo().log("Animation Set : " + modelAsset.getAnimationSetMap());

        TransformComponent playerTransform = store.getComponent(ref, TransformComponent.getComponentType());
        HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
        float direction = headRotation.getRotation().getY(); // facing direction
        float distance = 4.0f;
        double offsetX = -Math.sin(direction) * distance;
        double offsetZ = -Math.cos(direction) * distance;
        Vector3d wallPosition = new Vector3d(playerTransform.getPosition());
        wallPosition.add( // spawn in front of player
            new Vector3d(
                offsetX,
                0,
                offsetZ
            )
        );
        Vector3f wallRotation = new Vector3f(0, direction, 0); // align wall to face player
        TransformComponent wallTransform = new TransformComponent(wallPosition, wallRotation);
        AudioComponent audioComponent = new AudioComponent();
        audioComponent.addSound(SoundEvent.getAssetMap().getIndex("SFX_Stone_Wall_Spawning"));

        holder.addComponent(TransformComponent.getComponentType(), wallTransform);
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
        //holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
        holder.addComponent(AudioComponent.getComponentType(), audioComponent);
        // add componant for anime spawn
        // add compnnent for delay before despawn and anime despawn 
        //holder.addComponent(Interactions.getComponentType(), new Interactions()); // you need to add interactions here if you want your entity to be interactable

        holder.ensureComponent(UUIDComponent.getComponentType());
        //holder.ensureComponent(Interactable.getComponentType()); // if you want your entity to be interactable

        world.execute(() -> {
            store.addEntity(holder, AddReason.SPAWN);
        });

        /* 
        "SFX_Stone_Wall_Spawning"); 
        world.execute(() -> {
            TransformComponent transform = store.getComponent(ref, EntityModule.get().getTransformComponentType());
            SoundUtil.playSoundEvent3d(ref, index, transform.getPosition(), store);
        });
        */


    }
    
}
