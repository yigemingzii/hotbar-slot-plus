package com.yigemingzi.hotbarslotplus.mixin;

import com.yigemingzi.hotbarslotplus.ExtraHotbarInventoryHolder;
import com.yigemingzi.hotbarslotplus.ExtraHotbarLayout;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin extends ScreenHandler {
    private PlayerScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void hotbarSlotPlus$addExtraHotbarSlots(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
        if (!(owner instanceof ExtraHotbarInventoryHolder holder)) {
            return;
        }

        for (int slot = 0; slot < ExtraHotbarLayout.DEDICATED_SLOT_COUNT; slot++) {
            this.addSlot(new Slot(holder.hotbarSlotPlus$getExtraHotbarInventory(), slot, -1000, -1000));
        }
    }
}
