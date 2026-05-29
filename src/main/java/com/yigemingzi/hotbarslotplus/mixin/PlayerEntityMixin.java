package com.yigemingzi.hotbarslotplus.mixin;

import com.yigemingzi.hotbarslotplus.ExtraHotbarInventory;
import com.yigemingzi.hotbarslotplus.ExtraHotbarInventoryHolder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin implements ExtraHotbarInventoryHolder {
    @Unique
    private static final String HOTBAR_SLOT_PLUS_NBT_KEY = "HotbarSlotPlusExtraHotbar";

    @Unique
    private final ExtraHotbarInventory hotbarSlotPlus$extraHotbarInventory = new ExtraHotbarInventory();

    @Override
    public Inventory hotbarSlotPlus$getExtraHotbarInventory() {
        return hotbarSlotPlus$extraHotbarInventory;
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void hotbarSlotPlus$readExtraHotbar(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains(HOTBAR_SLOT_PLUS_NBT_KEY, NbtElement.COMPOUND_TYPE)) {
            PlayerEntity player = (PlayerEntity) (Object) this;
            Inventories.readNbt(nbt.getCompound(HOTBAR_SLOT_PLUS_NBT_KEY), hotbarSlotPlus$extraHotbarInventory.stacks(), player.getRegistryManager());
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void hotbarSlotPlus$writeExtraHotbar(NbtCompound nbt, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        NbtCompound extraHotbar = new NbtCompound();
        Inventories.writeNbt(extraHotbar, hotbarSlotPlus$extraHotbarInventory.stacks(), player.getRegistryManager());
        nbt.put(HOTBAR_SLOT_PLUS_NBT_KEY, extraHotbar);
    }

    @Inject(method = "dropInventory", at = @At("TAIL"))
    private void hotbarSlotPlus$dropExtraHotbar(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        for (int slot = 0; slot < hotbarSlotPlus$extraHotbarInventory.size(); slot++) {
            if (!hotbarSlotPlus$extraHotbarInventory.getStack(slot).isEmpty()) {
                player.dropItem(hotbarSlotPlus$extraHotbarInventory.removeStack(slot), true, false);
            }
        }
    }
}
