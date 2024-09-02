package io.github.jamalam360.block;

import io.github.jamalam360.data.WarpPlate;
import io.github.jamalam360.data.WarpPlatePair;
import io.github.jamalam360.data.WarpPlatesSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class PlateBlock extends Block implements EntityBlock {
	public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
	private static final VoxelShape INACTIVE_SHAPE = Shapes.or(
			Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
			Block.box(2.0, 2.0, 2.0, 14.0, 4.0, 14.0)
	);
	private static final VoxelShape ACTIVE_SHAPE = Shapes.or(
			Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
			Block.box(2.0, 2.0, 2.0, 14.0, 3.0, 14.0)
	);
	private static final AABB TOUCH_AABB = new AABB(0.0625, 0.0, 0.0625, 0.9375, 0.25, 0.9375);

	public PlateBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.defaultBlockState().setValue(ACTIVE, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ACTIVE);
	}

	@SuppressWarnings("deprecation")
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(ACTIVE) ? ACTIVE_SHAPE : INACTIVE_SHAPE;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		int count = level.getEntitiesOfClass(Player.class, TOUCH_AABB.move(pos), EntitySelector.NO_SPECTATORS).size();
		if (count == 0) {
			level.setBlock(pos, state.setValue(ACTIVE, false), 2);
			level.playSound(null, pos, SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF, SoundSource.BLOCKS);
		} else {
			level.scheduleTick(pos, this, 20);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (state.getValue(ACTIVE)) {
			return;
		}

		PlateBlockEntity blockEntity = (PlateBlockEntity) level.getBlockEntity(pos);

		if (blockEntity != null && entity instanceof Player player) {
			blockEntity.tryTeleport(player, this.getType());
			level.setBlock(pos, state.setValue(ACTIVE, true), 2);
			level.playSound(null, pos, SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON, SoundSource.BLOCKS);
			level.scheduleTick(pos, this, 20);
		}
	}

	@Override
	public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		super.playerWillDestroy(level, pos, state, player);

		if (level instanceof ServerLevel serverLevel) {
			PlateBlockEntity blockEntity = (PlateBlockEntity) level.getBlockEntity(pos);
			
			if (blockEntity == null) {
				return;
			}
			
			WarpPlatesSavedData data = WarpPlatesSavedData.get(serverLevel);
			WarpPlatePair pair = data.getPair(blockEntity.getId());

			if (pair == null) {
				return;
			}

			switch (this.getType()) {
				case SOURCE -> {
					WarpPlate returnPlate = pair.returnPlate();

					if (returnPlate != null) {
						level.removeBlock(returnPlate.pos(), false);
					}

					data.removePair(blockEntity.getId());
				}
				case RETURN -> {
					pair.setReturnPlate(null);
					data.setDirty();
				}
			}
		}
	}

	public static void spawnTeleportParticles(Level level, BlockPos pos) {
		RandomSource random = level.getRandom();

		for (int i = 0; i < 3; ++i) {
			int j = random.nextInt(2) * 2 - 1;
			int k = random.nextInt(2) * 2 - 1;
			double d = (double) pos.getX() + 0.5 + 0.25 * (double) j;
			double e = (float) pos.getY() + random.nextFloat();
			double f = (double) pos.getZ() + 0.5 + 0.25 * (double) k;
			double g = random.nextFloat() * (float) j;
			double h = ((double) random.nextFloat() - 0.5) * 0.125;
			double l = random.nextFloat() * (float) k;
			level.addParticle(ParticleTypes.PORTAL, d, e, f, g, h, l);
		}
	}

	public abstract PlateType getType();
}

