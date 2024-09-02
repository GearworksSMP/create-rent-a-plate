package io.github.jamalam360.block;

import io.github.jamalam360.WarpPlates;
import io.github.jamalam360.WarpPlatesConfig;
import io.github.jamalam360.data.WarpPlate;
import io.github.jamalam360.data.WarpPlatePair;
import io.github.jamalam360.data.WarpPlatesSavedData;
import io.github.jamalam360.menu.WarpPlateRentMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class WarpPlateBlockEntity extends PlateBlockEntity implements ExtendedScreenHandlerFactory {
//	private static final long RENT_DURATION = 1000L * 60L * 60L * 24L * 30L;
	private static final long RENT_DURATION = 1000L * 75L;
	private String warpTitle = "";
	@Nullable
	private UUID renter;

	public WarpPlateBlockEntity(BlockPos pos, BlockState state) {
		super(WarpPlates.WARP_PLATE_BLOCK_ENTITY, pos, state);
	}
	
	public static void tick(Level level, BlockPos pos,  BlockState state,  WarpPlateBlockEntity blockEntity) {
		if (blockEntity.isRented() && blockEntity.level instanceof ServerLevel serverLevel) {
			WarpPlatesSavedData data = WarpPlatesSavedData.get(serverLevel);
			WarpPlatePair pair = data.getPair(blockEntity.getId());
			
			if (pair != null && pair.expiryTime() < System.currentTimeMillis()) {
				if (blockEntity.renter != null && level.getPlayerByUUID(blockEntity.renter) instanceof ServerPlayer player) {
					player.displayClientMessage(Component.translatable("text.warp_plates.rent_expired", blockEntity.warpTitle), true);
				}
				
				data.removePair(blockEntity.getId());
				WarpPlate returnPlate = pair.returnPlate();
				
				if (returnPlate != null) {
					BlockPos returnPos = returnPlate.pos();

					if (serverLevel.getBlockState(returnPos).getBlock() == WarpPlates.RETURN_PLATE_BLOCK) {
						serverLevel.removeBlock(returnPos, false);
					}
				}
				
				blockEntity.setId(-1);
				blockEntity.renter = null;
				blockEntity.warpTitle = "";
				blockEntity.setChanged();
				level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
			}
		}
	}

	public int rent(Player renter) {
		this.renter = renter.getUUID();

		if (this.level instanceof ServerLevel serverLevel) {
			WarpPlatesSavedData data = WarpPlatesSavedData.get(serverLevel);
			
			if (data.getPair(this.getId()) == null) {
				long thisTime = System.currentTimeMillis();
				long expiryTime = thisTime + RENT_DURATION;
				WarpPlatePair pair = new WarpPlatePair(data.getNextId(), expiryTime, new WarpPlate(serverLevel.dimensionTypeId().location(), this.getBlockPos()), null);
				WarpPlates.LOGGER.info("Warp Plate at {} rented with ID {}", this.getBlockPos(), pair.id());
				data.addPair(pair);
				this.setId(pair.id());
			} else {
				WarpPlatePair pair = data.getPair(this.getId());
				pair.setExpiryTime(pair.expiryTime() + RENT_DURATION);
				WarpPlates.LOGGER.info("Warp Plate at {} rent extended", this.getBlockPos());
			}

			data.setDirty();
			this.setChanged();
		}

		return this.getId();
	}

	public boolean isRented() {
		return this.renter != null;
	}

	public @Nullable UUID getRenter() {
		return this.renter;
	}

	public String getWarpTitle() {
		return this.warpTitle;
	}

	public void setWarpTitle(String warpTitle) {
		this.warpTitle = warpTitle;
		this.setChanged();
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.warpTitle = tag.getString("WarpTitle");

		if (tag.contains("Renter")) {
			this.renter = tag.getUUID("Renter");
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.putString("WarpTitle", this.warpTitle);

		if (this.renter != null) {
			tag.putUUID("Renter", this.renter);
		}
	}

	@Override
	public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}

	@Override
	public Component getDisplayName() {
		return Component.translatable("block.warp_plate.warp_plate");
	}

	@Override
	public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
		return new WarpPlateRentMenu(i, inventory, ContainerLevelAccess.NULL, this.getRenter(), (newWarpTitle) -> {
			if (!newWarpTitle.isEmpty()) {
				this.setWarpTitle(newWarpTitle);
			}
			
			int id = this.rent(player);
			WarpPlatePair pair = WarpPlatesSavedData.get((ServerLevel) this.level).getPair(id);
			
			if (pair.returnPlate() == null) {
				ItemStack returnPlate = WarpPlates.RETURN_PLATE_BLOCK_ITEM.getDefaultInstance();
				CompoundTag tag = new CompoundTag();
				tag.putInt("PlateId", id);
				BlockItem.setBlockEntityData(returnPlate, WarpPlates.RETURN_PLATE_BLOCK_ENTITY, tag);
				returnPlate.setHoverName(returnPlate.getDisplayName().copy().append(Component.literal(" - " + this.warpTitle)));
				player.getInventory().add(returnPlate);
			}
			
			player.level().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
		});
	}

	@Override
	public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
		WarpPlatePair pair = WarpPlatesSavedData.get((ServerLevel) this.level).getPair(this.getId());
		buf.writeUtf(warpTitle);
		buf.writeLong(pair == null ? -1L : pair.expiryTime());
	}
}
