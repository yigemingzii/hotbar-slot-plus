package com.yigemingzi.hotbarslotplus;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;

public final class ExtraHotbarRenderer {
    private static final int HUD_LABEL = 0xFFE8D84A;
    private static final int HUD_LABEL_ACTIVE = 0xFFFFF26A;
    private static final Identifier HOTBAR_TEXTURE = Identifier.ofVanilla("hud/hotbar");
    private static final Identifier HOTBAR_SELECTION_TEXTURE = Identifier.ofVanilla("hud/hotbar_selection");
    private static final Identifier HOTBAR_OFFHAND_LEFT_TEXTURE = Identifier.ofVanilla("hud/hotbar_offhand_left");
    private static final Identifier HOTBAR_OFFHAND_RIGHT_TEXTURE = Identifier.ofVanilla("hud/hotbar_offhand_right");
    private static final Identifier HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE = Identifier.ofVanilla("hud/hotbar_attack_indicator_background");
    private static final Identifier HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE = Identifier.ofVanilla("hud/hotbar_attack_indicator_progress");

    private ExtraHotbarRenderer() {
    }

    public static void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        int left = ExtraHotbarHudLayout.left(context);
        int[] rows = HotbarScrollController.visibleRowsInDisplayOrder(ExtraHotbarHudLayout.visibleRows());
        PlayerInventory inventory = client.player.getInventory();
        Inventory dedicatedInventory = dedicatedInventory(client);

        RenderSystem.enableBlend();
        context.getMatrices().push();
        context.getMatrices().translate(0.0F, 0.0F, -90.0F);
        for (int visibleOffset = 0; visibleOffset < rows.length; visibleOffset++) {
            int top = ExtraHotbarHudLayout.rowTop(context, visibleOffset);
            int logicalRow = rows[visibleOffset];
            renderHudRowBackground(context, left, top);
            renderSelection(context, inventory, logicalRow, left, top);
            if (visibleOffset == 0) {
                renderOffhandBackground(context, client.player, left, top);
            }
        }
        context.getMatrices().pop();
        RenderSystem.disableBlend();

        int seed = 1;
        for (int visibleOffset = 0; visibleOffset < rows.length; visibleOffset++) {
            int top = ExtraHotbarHudLayout.rowTop(context, visibleOffset);
            int logicalRow = rows[visibleOffset];
            seed = renderHudRowItems(context, tickCounter, client.player, inventory, dedicatedInventory, logicalRow, left, top, seed);
            renderHudRowNumber(context, logicalRow, left, top, logicalRow == HotbarScrollController.activeRow());
        }

        renderOffhandItem(context, tickCounter, client.player, left, ExtraHotbarHudLayout.rowTop(context, 0), seed);
        renderAttackIndicator(context, client.player, left, ExtraHotbarHudLayout.rowTop(context, 0));
    }

    public static void renderInventoryPanel(DrawContext context, PlayerInventory inventory, Inventory dedicatedInventory, int left, int top, int rows) {
        int[] logicalRows = HotbarScrollController.visibleRowsInDisplayOrder(rows);

        RenderSystem.enableBlend();
        for (int row = 0; row < logicalRows.length; row++) {
            int rowTop = top + (logicalRows.length - 1 - row) * ExtraHotbarHudLayout.ROW_PITCH;
            renderHudRowBackground(context, left, rowTop);
            renderSelection(context, inventory, logicalRows[row], left, rowTop);
        }
        RenderSystem.disableBlend();

        MinecraftClient client = MinecraftClient.getInstance();
        for (int row = 0; row < logicalRows.length; row++) {
            int rowTop = top + (logicalRows.length - 1 - row) * ExtraHotbarHudLayout.ROW_PITCH;
            renderInventoryRowItems(context, client.player, inventory, dedicatedInventory, logicalRows[row], left, rowTop);
            renderHudRowNumber(context, logicalRows[row], left, rowTop, logicalRows[row] == HotbarScrollController.activeRow());
        }
    }

    private static int renderHudRowItems(DrawContext context, RenderTickCounter tickCounter, PlayerEntity player, PlayerInventory inventory, Inventory dedicatedInventory, int logicalRow, int left, int top, int seed) {
        for (int column = 0; column < 9; column++) {
            ItemStack stack = stackFor(logicalRow, column, inventory, dedicatedInventory);
            renderHotbarItem(context, tickCounter, player, stack, left + 3 + column * ExtraHotbarHudLayout.SLOT_PITCH, top + 3, seed++);
        }

        return seed;
    }

    private static void renderInventoryRowItems(DrawContext context, PlayerEntity player, PlayerInventory inventory, Inventory dedicatedInventory, int logicalRow, int left, int top) {
        for (int column = 0; column < 9; column++) {
            ItemStack stack = stackFor(logicalRow, column, inventory, dedicatedInventory);
            renderPlainItem(context, player, stack, left + 3 + column * ExtraHotbarHudLayout.SLOT_PITCH, top + 3);
        }
    }

    private static void renderHudRowBackground(DrawContext context, int left, int top) {
        context.drawGuiTexture(HOTBAR_TEXTURE, left, top, ExtraHotbarHudLayout.HOTBAR_WIDTH, ExtraHotbarHudLayout.HOTBAR_HEIGHT);
    }

    private static void renderSelection(DrawContext context, PlayerInventory inventory, int logicalRow, int left, int top) {
        if (logicalRow == HotbarScrollController.activeRow()) {
            context.drawGuiTexture(HOTBAR_SELECTION_TEXTURE, left - 1 + inventory.selectedSlot * ExtraHotbarHudLayout.SLOT_PITCH, top - 1, 24, 23);
        }
    }

    private static void renderOffhandBackground(DrawContext context, PlayerEntity player, int left, int top) {
        if (player.getOffHandStack().isEmpty()) {
            return;
        }

        if (player.getMainArm().getOpposite() == Arm.LEFT) {
            context.drawGuiTexture(HOTBAR_OFFHAND_LEFT_TEXTURE, left - 29, top - 1, 29, 24);
            return;
        }

        context.drawGuiTexture(HOTBAR_OFFHAND_RIGHT_TEXTURE, left + ExtraHotbarHudLayout.HOTBAR_WIDTH, top - 1, 29, 24);
    }

    private static void renderOffhandItem(DrawContext context, RenderTickCounter tickCounter, PlayerEntity player, int left, int top, int seed) {
        ItemStack offhandStack = player.getOffHandStack();
        if (offhandStack.isEmpty()) {
            return;
        }

        int itemLeft = player.getMainArm().getOpposite() == Arm.LEFT ? left - 26 : left + ExtraHotbarHudLayout.HOTBAR_WIDTH + 10;
        renderHotbarItem(context, tickCounter, player, offhandStack, itemLeft, top + 3, seed);
    }

    private static void renderAttackIndicator(DrawContext context, PlayerEntity player, int left, int top) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.getAttackIndicator().getValue() != AttackIndicator.HOTBAR) {
            return;
        }

        float cooldown = client.player.getAttackCooldownProgress(0.0F);
        if (cooldown >= 1.0F) {
            return;
        }

        RenderSystem.enableBlend();
        int indicatorLeft = left + ExtraHotbarHudLayout.HOTBAR_WIDTH + 6;
        if (player.getMainArm().getOpposite() == Arm.RIGHT) {
            indicatorLeft = left - 22;
        }

        int indicatorTop = top + 2;
        int progress = (int) (cooldown * 19.0F);
        context.drawGuiTexture(HOTBAR_ATTACK_INDICATOR_BACKGROUND_TEXTURE, indicatorLeft, indicatorTop, 18, 18);
        context.drawGuiTexture(HOTBAR_ATTACK_INDICATOR_PROGRESS_TEXTURE, 18, 18, 0, 18 - progress, indicatorLeft, indicatorTop + 18 - progress, 18, progress);
        RenderSystem.disableBlend();
    }

    private static void renderHudRowNumber(DrawContext context, int logicalRow, int left, int top, boolean active) {
        MinecraftClient client = MinecraftClient.getInstance();
        String label = Integer.toString(logicalRow + 1);
        int color = active ? HUD_LABEL_ACTIVE : HUD_LABEL;
        int y = top + 7;
        context.drawTextWithShadow(client.textRenderer, label, left - ExtraHotbarHudLayout.ROW_NUMBER_GAP - client.textRenderer.getWidth(label), y, color);
        context.drawTextWithShadow(client.textRenderer, label, left + ExtraHotbarHudLayout.HOTBAR_WIDTH + ExtraHotbarHudLayout.ROW_NUMBER_GAP, y, color);
    }

    private static void renderHotbarItem(DrawContext context, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int left, int top, int seed) {
        if (stack.isEmpty()) {
            return;
        }

        float animation = stack.getBobbingAnimationTime() - tickCounter.getTickDelta(false);
        if (animation > 0.0F) {
            float scale = 1.0F + animation / 5.0F;
            context.getMatrices().push();
            context.getMatrices().translate(left + 8.0F, top + 12.0F, 0.0F);
            context.getMatrices().scale(1.0F / scale, (scale + 1.0F) / 2.0F, 1.0F);
            context.getMatrices().translate(-(left + 8.0F), -(top + 12.0F), 0.0F);
        }

        context.drawItem(player, stack, left, top, seed);
        if (animation > 0.0F) {
            context.getMatrices().pop();
        }

        context.drawItemInSlot(MinecraftClient.getInstance().textRenderer, stack, left, top);
    }

    private static void renderPlainItem(DrawContext context, PlayerEntity player, ItemStack stack, int left, int top) {
        if (stack.isEmpty()) {
            return;
        }

        context.drawItem(player, stack, left, top, 0);
        context.drawItemInSlot(MinecraftClient.getInstance().textRenderer, stack, left, top);
    }

    private static ItemStack stackFor(int row, int column, PlayerInventory inventory, Inventory dedicatedInventory) {
        int storageRow = HotbarScrollController.actualStorageRowForLogicalRow(row);
        if (storageRow == 0 || HotbarSlotPlusConfig.get().storageMode() == HotbarSlotPlusConfig.StorageMode.INVENTORY_ROWS) {
            return inventory.getStack(ExtraHotbarLayout.playerInventoryIndex(storageRow, column));
        }

        if (dedicatedInventory == null) {
            return ItemStack.EMPTY;
        }

        int index = ExtraHotbarLayout.dedicatedInventoryIndex(storageRow, column);
        if (index < 0 || index >= dedicatedInventory.size()) {
            return ItemStack.EMPTY;
        }

        return dedicatedInventory.getStack(index);
    }

    private static Inventory dedicatedInventory(MinecraftClient client) {
        if (client.player instanceof ExtraHotbarInventoryHolder holder) {
            return holder.hotbarSlotPlus$getExtraHotbarInventory();
        }

        return null;
    }
}
