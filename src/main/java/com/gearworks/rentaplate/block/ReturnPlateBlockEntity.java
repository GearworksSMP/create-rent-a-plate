package com.gearworks.rentaplate.block;

import com.gearworks.rentaplate.RentAPlate;
import com.gearworks.rentaplate.data.WarpPlate;
import com.gearworks.rentaplate.data.WarpPlatePair;
import com.gearworks.rentaplate.data.WarpPlatesSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class ReturnPlateBlockEntity extends PlateBlockEntity {
	public ReturnPlateBlockEntity(BlockPos pos, BlockState state) {
		super(RentAPlate.RETURN_PLATE_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void onFirstLoad(ServerLevel serverLevel) {
		WarpPlatesSavedData data = WarpPlatesSavedData.get(serverLevel);
		WarpPlatePair pair = data.getPair(this.getId());

		if (pair == null) {
			RentAPlate.LOGGER.warn("Return plate at {} ({}) has an incorrect ID", this.getBlockPos(), this.getId());
			return;
		}

		pair.setReturnPlate(new WarpPlate(serverLevel.dimensionTypeId().location(), this.getBlockPos()));
		data.setDirty();
	}
}
