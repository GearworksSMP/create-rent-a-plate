package com.gearworks.rentaplate.mixin;

import com.gearworks.rentaplate.WarpPlates;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
	@Shadow public abstract Block getBlock();
	
	@Inject(
			method = "place",
			at = @At("HEAD"),
			cancellable = true
	)
	private void warp_plates$onlyAllowAdmins(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
		if (this.getBlock() == WarpPlates.WARP_PLATE_BLOCK && context.getPlayer() != null && !context.getPlayer().hasPermissions(2)) {
			if (!context.getLevel().isClientSide()) {
				context.getPlayer().sendSystemMessage(Component.translatable("text.warp_plates.admin_required"));
			}
			
			cir.setReturnValue(InteractionResult.FAIL);
		}
	}
}
