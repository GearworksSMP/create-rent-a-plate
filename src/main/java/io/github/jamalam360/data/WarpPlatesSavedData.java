package io.github.jamalam360.data;

import io.github.jamalam360.block.WarpPlateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WarpPlatesSavedData extends SavedData {
	private final List<WarpPlatePair> pairs;
	private final List<WarpPlatePair> toRemove = new ArrayList<>();
	private int nextId;

	private WarpPlatesSavedData(List<WarpPlatePair> pairs, int nextId) {
		this.pairs = new ArrayList<>(pairs);
		this.nextId = nextId;
	}

	public static WarpPlatesSavedData get(MinecraftServer server) {
		return server.getLevel(ServerLevel.OVERWORLD).getDataStorage().computeIfAbsent(WarpPlatesSavedData::createFromTag, () -> new WarpPlatesSavedData(new ArrayList<>(), 0), "warp_plates");
	}

	public static WarpPlatesSavedData get(ServerLevel level) {
		return get(level.getServer());
	}

	@Nullable
	public WarpPlatePair getPair(int id) {
		for (WarpPlatePair pair : this.pairs) {
			if (pair.id() == id) {
				return pair;
			}
		}

		return null;
	}

	public int getNextId() {
		return this.nextId;
	}

	public void addPair(WarpPlatePair pair) {
		if (pair.id() != this.nextId) {
			throw new IllegalArgumentException("Pair ID does not match next ID");
		}

		this.nextId++;
		this.pairs.add(pair);
		this.setDirty();
	}

	public void removePair(int id) {
		this.pairs.removeIf(pair -> pair.id() == id);
		this.setDirty();
	}
	
	private static WarpPlatesSavedData createFromTag(CompoundTag tag) {
		List<WarpPlatePair> pairs = new ArrayList<>();

		ListTag pairsTag = tag.getList("pairs", CompoundTag.TAG_COMPOUND);

		for (int i = 0; i < pairsTag.size(); i++) {
			CompoundTag pairTag = pairsTag.getCompound(i);
			int id = pairTag.getInt("id");
			long expiryTime = pairTag.getLong("expiryTime");
			WarpPlate warpPlate = loadWarpPlate(pairTag.getCompound("warpPlate"));
			WarpPlate returnPlate;

			if (pairTag.contains("returnPlate")) {
				returnPlate = loadWarpPlate(pairTag.getCompound("returnPlate"));
			} else {
				returnPlate = null;
			}
			
			pairs.add(new WarpPlatePair(id, expiryTime, warpPlate, returnPlate));
		}

		return new WarpPlatesSavedData(pairs, tag.getInt("nextId"));
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		ListTag pairs = new ListTag();

		for (WarpPlatePair pair : this.pairs) {
			CompoundTag pairTag = new CompoundTag();
			pairTag.putInt("id", pair.id());
			pairTag.put("warpPlate", saveWarpPlate(new CompoundTag(), pair.warpPlate()));

			if (pair.returnPlate() != null) {
				pairTag.put("returnPlate", saveWarpPlate(new CompoundTag(), pair.returnPlate()));
			}

			pairs.add(pairTag);
		}

		tag.put("pairs", pairs);
		tag.putInt("nextId", this.nextId);

		return tag;
	}

	private CompoundTag saveWarpPlate(CompoundTag tag, WarpPlate warpPlate) {
		tag.putString("dimension", warpPlate.dimension().toString());
		tag.putIntArray("pos", new int[]{warpPlate.pos().getX(), warpPlate.pos().getY(), warpPlate.pos().getZ()});
		return tag;
	}

	private static WarpPlate loadWarpPlate(CompoundTag tag) {
		return new WarpPlate(new ResourceLocation(tag.getString("dimension")), new BlockPos(tag.getIntArray("pos")[0], tag.getIntArray("pos")[1], tag.getIntArray("pos")[2]));
	}
}
