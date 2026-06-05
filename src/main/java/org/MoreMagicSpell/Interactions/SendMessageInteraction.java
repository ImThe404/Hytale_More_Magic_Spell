package org.MoreMagicSpell.Interactions;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Simple Interaction that sends a message to the player when used. Useful for Debugging for now.
 */
public class SendMessageInteraction extends SimpleInstantInteraction {
    public static final BuilderCodec<SendMessageInteraction> CODEC = BuilderCodec.builder(
            SendMessageInteraction.class, SendMessageInteraction::new, SimpleInstantInteraction.CODEC
    ).build();

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

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
        Ref<EntityStore> ref = interactionContext.getEntity();
        PlayerRef player = commandBuffer.getComponent(ref, PlayerRef.getComponentType());
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

        // Send message to player for Debugging IN-GAME
        player.sendMessage(Message.raw("You have used the custom item +" + itemStack.getItemId()));
        TransformComponent playerTransform = store.getComponent(ref, TransformComponent.getComponentType());
        player.sendMessage(Message.raw(" PlayerRotation : " + playerTransform.getRotation().y() ));
    }
}

