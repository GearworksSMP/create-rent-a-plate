package io.github.jamalam360.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ReturnPlateBlock extends PlateBlock {
	public ReturnPlateBlock(Properties properties) {
		super(properties);
	}

	@Override
	public PlateType getType() {
		return PlateType.RETURN;
	}
	
	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ReturnPlateBlockEntity(pos, state);
	}
}
