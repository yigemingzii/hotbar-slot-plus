package com.yigemingzi.hotbarslotplus.mixin;

import com.yigemingzi.hotbarslotplus.ExtraHotbarLayout;
import com.yigemingzi.hotbarslotplus.ExtraHotbarRenderer;
import com.yigemingzi.hotbarslotplus.HotbarSlotPlusConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractInventoryScreen<PlayerScreenHandler> {
    private InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void hotbarSlotPlus$renderExtraHotbars(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int rows = HotbarSlotPlusConfig.get().inventoryRows();
        ExtraHotbarRenderer.renderInventoryPanel(
                context,
                this.client.player.getInventory(),
                this.client.player instanceof com.yigemingzi.hotbarslotplus.ExtraHotbarInventoryHolder holder ? holder.hotbarSlotPlus$getExtraHotbarInventory() : null,
                hotbarSlotPlus$panelLeft(),
                hotbarSlotPlus$panelTop(rows),
                rows
        );
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void hotbarSlotPlus$clickExtraHotbar(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (this.client == null || this.client.interactionManager == null || this.client.player == null) {
            return;
        }

        int rows = HotbarSlotPlusConfig.get().inventoryRows();
        int panelLeft = hotbarSlotPlus$panelLeft();
        int panelTop = hotbarSlotPlus$panelTop(rows);
        int row = ExtraHotbarLayout.rowAt(mouseY, panelTop, rows);
        int column = ExtraHotbarLayout.columnAt(mouseX, panelLeft);
        if (row < 0 || column < 0) {
            return;
        }

        int logicalRow = com.yigemingzi.hotbarslotplus.HotbarScrollController.logicalRowForVisibleOffset(row, rows);
        int slotId = ExtraHotbarLayout.inventoryScreenSlotId(HotbarSlotPlusConfig.get().storageMode(), logicalRow, column);
        this.client.interactionManager.clickSlot(this.handler.syncId, slotId, button, SlotActionType.PICKUP, this.client.player);
        cir.setReturnValue(true);
    }

    private int hotbarSlotPlus$panelLeft() {
        if (HotbarSlotPlusConfig.get().inventoryPanelOnRight()) {
            return this.x + this.backgroundWidth + 10;
        }

        return this.x - ExtraHotbarLayout.ROW_WIDTH - 10;
    }

    private int hotbarSlotPlus$panelTop(int rows) {
        int panelHeight = rows * (ExtraHotbarLayout.ROW_HEIGHT + ExtraHotbarLayout.ROW_GAP) - ExtraHotbarLayout.ROW_GAP;
        return this.y + (this.backgroundHeight - panelHeight) / 2;
    }
}
