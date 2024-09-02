package io.github.jamalam360.block;

import io.github.jamalam360.data.WarpPlatePair;
import io.github.jamalam360.data.WarpPlatesSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WarpPlateBlock extends PlateBlock {
	public WarpPlateBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HorizontalDirectionalBlock.FACING);
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(HorizontalDirectionalBlock.FACING, context.getHorizontalDirection().getOpposite());
	}

	@SuppressWarnings("deprecation")
	@Override
	public @NotNull InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		WarpPlateBlockEntity blockEntity = (WarpPlateBlockEntity) level.getBlockEntity(pos);
		if (blockEntity != null && level instanceof ServerLevel) {
			WarpPlatesSavedData data = WarpPlatesSavedData.get((ServerLevel) level);
			WarpPlatePair pair = data.getPair(blockEntity.getId());

			if (!blockEntity.isRented() || (pair != null && blockEntity.getRenter().equals(player.getUUID()))) {
				player.openMenu(blockEntity);
			} else {
				player.displayClientMessage(Component.translatable("text.warp_plates.already_rented"), true);
			}
		}

		return super.use(state, level, pos, player, hand, hit);
	}

	@Override
	public PlateType getType() {
		return PlateType.SOURCE;
	}

	@Override
	public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new WarpPlateBlockEntity(pos, state);
	}

	@Override
	public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return (l, p, s, be) -> WarpPlateBlockEntity.tick(l, p, s, (WarpPlateBlockEntity) be);
	}
}
