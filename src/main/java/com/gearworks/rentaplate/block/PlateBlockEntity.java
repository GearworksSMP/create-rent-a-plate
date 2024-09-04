package com.gearworks.rentaplate.block;

import com.gearworks.rentaplate.RentAPlate;
import com.gearworks.rentaplate.data.WarpPlate;
import com.gearworks.rentaplate.data.WarpPlatePair;
import com.gearworks.rentaplate.data.WarpPlatesSavedData;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2LongArrayMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.UUID;

public abstract class PlateBlockEntity extends BlockEntity {
	private static final Map<UUID, Long> COOLDOWNS = new Object2LongArrayMap<>();
	private int id = -1;

	public PlateBlockEntity(BlockEntityType<? extends PlateBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	protected void tryTeleport(Player player, PlateType type) {
		if (COOLDOWNS.getOrDefault(player.getUUID(), 0L) > System.currentTimeMillis()) {
			return;
		}

		if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
			COOLDOWNS.put(player.getUUID(), System.currentTimeMillis() + 1250);
			WarpPlatePair pair = WarpPlatesSavedData.get(serverLevel).getPair(this.getId());

			if (pair != null) {
				WarpPlate warpPlate = type == PlateType.SOURCE ? pair.returnPlate() : pair.warpPlate();

				if (warpPlate != null) {
					serverLevel.getServer().execute(() -> warpPlate.teleportTo(serverPlayer));
					serverLevel.playSound(null, pair.returnPlate().pos(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);
					serverLevel.playSound(null, pair.warpPlate().pos(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 1.0F);

					FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
					buf.writeBlockPos(pair.returnPlate().pos());
					buf.writeBlockPos(pair.warpPlate().pos());
					ServerPlayNetworking.send(serverPlayer, RentAPlate.PARTICLE_PACKET, buf);
				}
			}
		}

	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		int lastId = this.id;
		this.id = tag.getInt("PlateId");

		if (lastId == -1 && this.level instanceof ServerLevel serverLevel) {
			this.onFirstLoad(serverLevel);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putInt("PlateId", this.id);
	}

	protected void onFirstLoad(ServerLevel serverLevel) {
	}

	protected void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}
}
