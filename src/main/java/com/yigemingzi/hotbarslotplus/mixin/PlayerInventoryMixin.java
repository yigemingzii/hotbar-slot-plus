package com.yigemingzi.hotbarslotplus.mixin;

import com.yigemingzi.hotbarslotplus.HotbarScrollController;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Inject(method = "scrollInHotbar", at = @At("HEAD"), cancellable = true)
    private void hotbarSlotPlus$scrollVisibleHotbars(double amount, CallbackInfo ci) {
        if (HotbarScrollController.handleHotbarScroll((PlayerInventory) (Object) this, amount)) {
            ci.cancel();
        }
    }
}
