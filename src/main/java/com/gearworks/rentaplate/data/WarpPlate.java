package com.gearworks.rentaplate.data;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;

public record WarpPlate(ResourceLocation dimension, BlockPos pos) {
	public void teleportTo(ServerPlayer player) {
		ServerLevel level = player.serverLevel().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, this.dimension));
		FabricDimensions.teleport(player, level, new PortalInfo(this.pos.getCenter(), Vec3.ZERO, player.getYRot(), player.getXRot()));
	}
}
