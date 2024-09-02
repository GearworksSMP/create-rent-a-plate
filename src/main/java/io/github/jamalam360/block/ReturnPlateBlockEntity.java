package io.github.jamalam360.block;

import io.github.jamalam360.WarpPlates;
import io.github.jamalam360.data.WarpPlate;
import io.github.jamalam360.data.WarpPlatePair;
import io.github.jamalam360.data.WarpPlatesSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.security.cert.CertPathBuilderSpi;

public class ReturnPlateBlockEntity extends PlateBlockEntity {
	public ReturnPlateBlockEntity(BlockPos pos, BlockState state) {
		super(WarpPlates.RETURN_PLATE_BLOCK_ENTITY, pos, state);
	}

	@Override
	protected void onFirstLoad(ServerLevel serverLevel) {
		WarpPlatesSavedData data = WarpPlatesSavedData.get(serverLevel);
		WarpPlatePair pair = data.getPair(this.getId());
		
		if (pair == null) {
			WarpPlates.LOGGER.warn("Return plate at {} ({}) has an incorrect ID", this.getBlockPos(), this.getId());
			return;
		}
		
		pair.setReturnPlate(new WarpPlate(serverLevel.dimensionTypeId().location(), this.getBlockPos()));
		data.setDirty();
	}
}
