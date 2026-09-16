package teacommontea.veritechasse.Vanilla.PlayerMovement.PlayerServerboundPackets;

import teacommontea.veritechasse.Vanilla.Protocol;

public final class Packets {

    private Packets() {
    }

    public static boolean isAcceptCodeOfConduct(String packet) {
        return PacketNames.ACCEPT_CODE_OF_CONDUCT.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isAcceptTeleportation(String packet) {
        return PacketNames.ACCEPT_TELEPORTATION.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isAttack(String packet) {
        return PacketNames.ATTACK.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isBlockEntityTagQuery(String packet) {
        return PacketNames.BLOCK_ENTITY_TAG_QUERY.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isChangeDifficulty(String packet) {
        return PacketNames.CHANGE_DIFFICULTY.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isChangeGameMode(String packet) {
        return PacketNames.CHANGE_GAME_MODE.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isChat(String packet) {
        return PacketNames.CHAT.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isChatAck(String packet) {
        return PacketNames.CHAT_ACK.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isChatCommand(String packet) {
        return PacketNames.CHAT_COMMAND.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isChatCommandSigned(String packet) {
        return PacketNames.CHAT_COMMAND_SIGNED.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isChatPreview(String packet) {
        return PacketNames.CHAT_PREVIEW.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isChatSessionUpdate(String packet) {
        return PacketNames.CHAT_SESSION_UPDATE.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isChunkBatchReceived(String packet) {
        return PacketNames.CHUNK_BATCH_RECEIVED.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isClientCommand(String packet) {
        return PacketNames.CLIENT_COMMAND.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isClientInformation(String packet) {
        return PacketNames.CLIENT_INFORMATION.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isClientTickEnd(String packet) {
        return PacketNames.CLIENT_TICK_END.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isCommandSuggestion(String packet) {
        return PacketNames.COMMAND_SUGGESTION.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isConfigurationAcknowledged(String packet) {
        return PacketNames.CONFIGURATION_ACKNOWLEDGED.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isContainerButtonClick(String packet) {
        return PacketNames.CONTAINER_BUTTON_CLICK.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isContainerClick(String packet) {
        return PacketNames.CONTAINER_CLICK.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isContainerClose(String packet) {
        return PacketNames.CONTAINER_CLOSE.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isContainerSlotStateChanged(String packet) {
        return PacketNames.CONTAINER_SLOT_STATE_CHANGED.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isCookieResponse(String packet) {
        return PacketNames.COOKIE_RESPONSE.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isCustomClickAction(String packet) {
        return PacketNames.CUSTOM_CLICK_ACTION.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isCustomPayload(String packet) {
        return PacketNames.CUSTOM_PAYLOAD.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isCustomQueryAnswer(String packet) {
        return PacketNames.CUSTOM_QUERY_ANSWER.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isDebugSubscriptionRequest(String packet) {
        return PacketNames.DEBUG_SUBSCRIPTION_REQUEST.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isEditBook(String packet) {
        return PacketNames.EDIT_BOOK.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isEntityTagQuery(String packet) {
        return PacketNames.ENTITY_TAG_QUERY.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isFinishConfiguration(String packet) {
        return PacketNames.FINISH_CONFIGURATION.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isHello(String packet) {
        return PacketNames.HELLO.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isInteract(String packet) {
        return PacketNames.INTERACT.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isJigsawGenerate(String packet) {
        return PacketNames.JIGSAW_GENERATE.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isKeepAlive(String packet) {
        return PacketNames.KEEP_ALIVE.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isKey(String packet) {
        return PacketNames.KEY.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isLockDifficulty(String packet) {
        return PacketNames.LOCK_DIFFICULTY.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isLoginAcknowledged(String packet) {
        return PacketNames.LOGIN_ACKNOWLEDGED.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isMovePlayer(String packet) {
        return PacketNames.MOVE_PLAYER.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isMoveVehicle(String packet) {
        return PacketNames.MOVE_VEHICLE.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isPaddleBoat(String packet) {
        return PacketNames.PADDLE_BOAT.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isPickItem(String packet) {
        return PacketNames.PICK_ITEM.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isPickItemFromBlock(String packet) {
        return PacketNames.PICK_ITEM_FROM_BLOCK.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isPickItemFromEntity(String packet) {
        return PacketNames.PICK_ITEM_FROM_ENTITY.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isPingRequest(String packet) {
        return PacketNames.PING_REQUEST.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isPlaceRecipe(String packet) {
        return PacketNames.PLACE_RECIPE.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isPlayerAbilities(String packet) {
        return PacketNames.PLAYER_ABILITIES.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isPlayerAction(String packet) {
        return PacketNames.PLAYER_ACTION.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isPlayerCommand(String packet) {
        return PacketNames.PLAYER_COMMAND.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isPlayerInput(String packet) {
        return PacketNames.PLAYER_INPUT.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isPlayerLoaded(String packet) {
        return PacketNames.PLAYER_LOADED.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isPong(String packet) {
        return PacketNames.PONG.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isRecipeBookChangeSettings(String packet) {
        return PacketNames.RECIPE_BOOK_CHANGE_SETTINGS.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isRecipeBookSeenRecipe(String packet) {
        return PacketNames.RECIPE_BOOK_SEEN_RECIPE.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isRenameItem(String packet) {
        return PacketNames.RENAME_ITEM.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isResourcePack(String packet) {
        return PacketNames.RESOURCE_PACK.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSeenAdvancements(String packet) {
        return PacketNames.SEEN_ADVANCEMENTS.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSelectBundleItem(String packet) {
        return PacketNames.SELECT_BUNDLE_ITEM.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSelectKnownPacks(String packet) {
        return PacketNames.SELECT_KNOWN_PACKS.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSelectTrade(String packet) {
        return PacketNames.SELECT_TRADE.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSetBeacon(String packet) {
        return PacketNames.SET_BEACON.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSetCarriedItem(String packet) {
        return PacketNames.SET_CARRIED_ITEM.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSetCommandBlock(String packet) {
        return PacketNames.SET_COMMAND_BLOCK.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSetCommandMinecart(String packet) {
        return PacketNames.SET_COMMAND_MINECART.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSetCreativeModeSlot(String packet) {
        return PacketNames.SET_CREATIVE_MODE_SLOT.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSetGameRule(String packet) {
        return PacketNames.SET_GAME_RULE.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSetJigsawBlock(String packet) {
        return PacketNames.SET_JIGSAW_BLOCK.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSetStructureBlock(String packet) {
        return PacketNames.SET_STRUCTURE_BLOCK.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSetTestBlock(String packet) {
        return PacketNames.SET_TEST_BLOCK.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSignUpdate(String packet) {
        return PacketNames.SIGN_UPDATE.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSpectatorAction(String packet) {
        return PacketNames.SPECTATOR_ACTION.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isStatusRequest(String packet) {
        return PacketNames.STATUS_REQUEST.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isSwing(String packet) {
        return PacketNames.SWING.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isTeleportToEntity(String packet) {
        return PacketNames.TELEPORT_TO_ENTITY.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isTestInstanceBlockAction(String packet) {
        return PacketNames.TEST_INSTANCE_BLOCK_ACTION.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isUseItem(String packet) {
        return PacketNames.USE_ITEM.equals(PacketRegistry.normalise(packet));
    }

    public static boolean isUseItemOn(String packet) {
        return PacketNames.USE_ITEM_ON.equals(PacketRegistry.normalise(packet));
    }

    public static boolean hasAcceptCodeOfConduct(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.ACCEPT_CODE_OF_CONDUCT, protocol);
    }

    public static boolean hasAcceptTeleportation(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.ACCEPT_TELEPORTATION, protocol);
    }

    public static boolean hasAttack(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.ATTACK, protocol);
    }

    public static boolean hasBlockEntityTagQuery(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.BLOCK_ENTITY_TAG_QUERY, protocol);
    }

    public static boolean hasChangeDifficulty(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CHANGE_DIFFICULTY, protocol);
    }

    public static boolean hasChangeGameMode(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CHANGE_GAME_MODE, protocol);
    }

    public static boolean hasChat(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CHAT, protocol);
    }

    public static boolean hasChatAck(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CHAT_ACK, protocol);
    }

    public static boolean hasChatCommand(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CHAT_COMMAND, protocol);
    }

    public static boolean hasChatCommandSigned(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CHAT_COMMAND_SIGNED, protocol);
    }

    public static boolean hasChatPreview(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CHAT_PREVIEW, protocol);
    }

    public static boolean hasChatSessionUpdate(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CHAT_SESSION_UPDATE, protocol);
    }

    public static boolean hasChunkBatchReceived(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CHUNK_BATCH_RECEIVED, protocol);
    }

    public static boolean hasClientCommand(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CLIENT_COMMAND, protocol);
    }

    public static boolean hasClientInformation(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CLIENT_INFORMATION, protocol);
    }

    public static boolean hasClientTickEnd(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CLIENT_TICK_END, protocol);
    }

    public static boolean hasCommandSuggestion(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.COMMAND_SUGGESTION, protocol);
    }

    public static boolean hasConfigurationAcknowledged(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CONFIGURATION_ACKNOWLEDGED, protocol);
    }

    public static boolean hasContainerButtonClick(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CONTAINER_BUTTON_CLICK, protocol);
    }

    public static boolean hasContainerClick(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CONTAINER_CLICK, protocol);
    }

    public static boolean hasContainerClose(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CONTAINER_CLOSE, protocol);
    }

    public static boolean hasContainerSlotStateChanged(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CONTAINER_SLOT_STATE_CHANGED, protocol);
    }

    public static boolean hasCookieResponse(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.COOKIE_RESPONSE, protocol);
    }

    public static boolean hasCustomClickAction(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CUSTOM_CLICK_ACTION, protocol);
    }

    public static boolean hasCustomPayload(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CUSTOM_PAYLOAD, protocol);
    }

    public static boolean hasCustomQueryAnswer(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.CUSTOM_QUERY_ANSWER, protocol);
    }

    public static boolean hasDebugSubscriptionRequest(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.DEBUG_SUBSCRIPTION_REQUEST, protocol);
    }

    public static boolean hasEditBook(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.EDIT_BOOK, protocol);
    }

    public static boolean hasEntityTagQuery(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.ENTITY_TAG_QUERY, protocol);
    }

    public static boolean hasFinishConfiguration(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.FINISH_CONFIGURATION, protocol);
    }

    public static boolean hasHello(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.HELLO, protocol);
    }

    public static boolean hasInteract(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.INTERACT, protocol);
    }

    public static boolean hasJigsawGenerate(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.JIGSAW_GENERATE, protocol);
    }

    public static boolean hasKeepAlive(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.KEEP_ALIVE, protocol);
    }

    public static boolean hasKey(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.KEY, protocol);
    }

    public static boolean hasLockDifficulty(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.LOCK_DIFFICULTY, protocol);
    }

    public static boolean hasLoginAcknowledged(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.LOGIN_ACKNOWLEDGED, protocol);
    }

    public static boolean hasMovePlayer(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.MOVE_PLAYER, protocol);
    }

    public static boolean hasMoveVehicle(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.MOVE_VEHICLE, protocol);
    }

    public static boolean hasPaddleBoat(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.PADDLE_BOAT, protocol);
    }

    public static boolean hasPickItem(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.PICK_ITEM, protocol);
    }

    public static boolean hasPickItemFromBlock(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.PICK_ITEM_FROM_BLOCK, protocol);
    }

    public static boolean hasPickItemFromEntity(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.PICK_ITEM_FROM_ENTITY, protocol);
    }

    public static boolean hasPingRequest(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.PING_REQUEST, protocol);
    }

    public static boolean hasPlaceRecipe(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.PLACE_RECIPE, protocol);
    }

    public static boolean hasPlayerAbilities(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.PLAYER_ABILITIES, protocol);
    }

    public static boolean hasPlayerAction(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.PLAYER_ACTION, protocol);
    }

    public static boolean hasPlayerCommand(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.PLAYER_COMMAND, protocol);
    }

    public static boolean hasPlayerInput(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.PLAYER_INPUT, protocol);
    }

    public static boolean hasPlayerLoaded(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.PLAYER_LOADED, protocol);
    }

    public static boolean hasPong(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.PONG, protocol);
    }

    public static boolean hasRecipeBookChangeSettings(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.RECIPE_BOOK_CHANGE_SETTINGS, protocol);
    }

    public static boolean hasRecipeBookSeenRecipe(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.RECIPE_BOOK_SEEN_RECIPE, protocol);
    }

    public static boolean hasRenameItem(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.RENAME_ITEM, protocol);
    }

    public static boolean hasResourcePack(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.RESOURCE_PACK, protocol);
    }

    public static boolean hasSeenAdvancements(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SEEN_ADVANCEMENTS, protocol);
    }

    public static boolean hasSelectBundleItem(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SELECT_BUNDLE_ITEM, protocol);
    }

    public static boolean hasSelectKnownPacks(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SELECT_KNOWN_PACKS, protocol);
    }

    public static boolean hasSelectTrade(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SELECT_TRADE, protocol);
    }

    public static boolean hasSetBeacon(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SET_BEACON, protocol);
    }

    public static boolean hasSetCarriedItem(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SET_CARRIED_ITEM, protocol);
    }

    public static boolean hasSetCommandBlock(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SET_COMMAND_BLOCK, protocol);
    }

    public static boolean hasSetCommandMinecart(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SET_COMMAND_MINECART, protocol);
    }

    public static boolean hasSetCreativeModeSlot(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SET_CREATIVE_MODE_SLOT, protocol);
    }

    public static boolean hasSetGameRule(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SET_GAME_RULE, protocol);
    }

    public static boolean hasSetJigsawBlock(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SET_JIGSAW_BLOCK, protocol);
    }

    public static boolean hasSetStructureBlock(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SET_STRUCTURE_BLOCK, protocol);
    }

    public static boolean hasSetTestBlock(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SET_TEST_BLOCK, protocol);
    }

    public static boolean hasSignUpdate(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SIGN_UPDATE, protocol);
    }

    public static boolean hasSpectatorAction(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SPECTATOR_ACTION, protocol);
    }

    public static boolean hasStatusRequest(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.STATUS_REQUEST, protocol);
    }

    public static boolean hasSwing(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.SWING, protocol);
    }

    public static boolean hasTeleportToEntity(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.TELEPORT_TO_ENTITY, protocol);
    }

    public static boolean hasTestInstanceBlockAction(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.TEST_INSTANCE_BLOCK_ACTION, protocol);
    }

    public static boolean hasUseItem(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.USE_ITEM, protocol);
    }

    public static boolean hasUseItemOn(Protocol protocol) {
        return PacketRegistry.exists(PacketNames.USE_ITEM_ON, protocol);
    }

}
