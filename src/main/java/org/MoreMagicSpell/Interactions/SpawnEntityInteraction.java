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
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import org.MoreMagicSpell.HolderType.HealthComponent;
import org.MoreMagicSpell.HolderType.PositionComponent;
import org.MoreMagicSpell.HolderType.VelocityComponent;

public class SpawnEntityInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<SpawnEntityInteraction> CODEC = BuilderCodec.builder(
            SpawnEntityInteraction.class, SpawnEntityInteraction::new, SimpleInstantInteraction.CODEC
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

        TransformComponent playerTransform = store.getComponent(ref, TransformComponent.getComponentType());
        float direction = playerTransform.getRotation().getY(); // facing direction
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
        TransformComponent wallTransform = new TransformComponent(wallPosition, playerTransform.getRotation());

        holder.addComponent(TransformComponent.getComponentType(), wallTransform);
        holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
        holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
        holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
        holder.addComponent(NetworkId.getComponentType(), new NetworkId(store.getExternalData().takeNextNetworkId()));
        // add componant for anime spawn
        // add compnnent for delay before despawn and anime despawn 
        //holder.addComponent(Interactions.getComponentType(), new Interactions()); // you need to add interactions here if you want your entity to be interactable

        holder.ensureComponent(UUIDComponent.getComponentType());
        //holder.ensureComponent(Interactable.getComponentType()); // if you want your entity to be interactable

        world.execute(() -> {
            store.addEntity(holder, AddReason.SPAWN);
        });

        player.sendMessage(Message.raw("You have used the custom item +" + itemStack.getItemId()));

        /* 

        // Get the EntityStore from a world, then get the underlying Store
        EntityStore entityStore = world.getEntityStore();
        Store<EntityStore> Estore = entityStore.getStore();

        // Create an entity using a Holder
        Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
        holder.addComponent(PositionComponenttype, new PositionComponent(0, 64, 0));
        holder.addComponent(VelocityComponenttype, new VelocityComponent());
        holder.addComponent(getHealthComponentType(), new HealthComponent(100));

        Ref<EntityStore> entity = Estore.addEntity(holder, AddReason.SPAWN);

        // Get a component from an entity using ComponentType (not Class)
        PositionComponent pos = Estore.getComponent(entity, positionType);

        // Iterate over entities with specific components using forEachChunk
        Estore.forEachChunk(positionType, (archetypeChunk, commandBuffer) -> {
            for (int i = 0; i < archetypeChunk.size(); i++) {
                Ref<EntityStore> ref = archetypeChunk.getReferenceTo(i);
                PositionComponent position = archetypeChunk.getComponent(i, positionType);
                VelocityComponent velocity = archetypeChunk.getComponent(i, velocityType);
                if (position != null && velocity != null) {
                    // Process entities with both components
                    position.add(velocity);
                }
            }
        });

        */

    }
    
}
