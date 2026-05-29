package com.yigemingzi.hotbarslotplus;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class HotbarSlotPlusClient implements ClientModInitializer {
    public static final String MOD_ID = "hotbar-slot-plus";

    private static KeyBinding moreRowsKey;
    private static KeyBinding fewerRowsKey;

    @Override
    public void onInitializeClient() {
        HotbarSlotPlusConfig.load();

        moreRowsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hotbar-slot-plus.more_rows",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_BRACKET,
                "key.categories.hotbar-slot-plus"
        ));
        fewerRowsKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hotbar-slot-plus.fewer_rows",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_BRACKET,
                "key.categories.hotbar-slot-plus"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(HotbarSlotPlusClient::handleKeys);
        HudRenderCallback.EVENT.register((context, tickCounter) -> ExtraHotbarRenderer.renderHud(context));
    }

    private static void handleKeys(MinecraftClient client) {
        while (moreRowsKey.wasPressed()) {
            HotbarSlotPlusConfig.get().setHudRows(HotbarSlotPlusConfig.get().hudRows() + 1);
        }

        while (fewerRowsKey.wasPressed()) {
            HotbarSlotPlusConfig.get().setHudRows(HotbarSlotPlusConfig.get().hudRows() - 1);
        }
    }
}

