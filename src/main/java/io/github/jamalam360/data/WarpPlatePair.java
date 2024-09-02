package io.github.jamalam360.data;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class WarpPlatePair {
	private final int id;
	private final WarpPlate warpPlate;
	private long expiryTime;
	private @Nullable WarpPlate returnPlate;

	public WarpPlatePair(int id, long expiryTime, WarpPlate warpPlate, @Nullable WarpPlate returnPlate) {
		this.id = id;
		this.expiryTime = expiryTime;
		this.warpPlate = warpPlate;
		this.returnPlate = returnPlate;
	}

	public int id() {
		return id;
	}

	public long expiryTime() {
		return expiryTime;
	}
	
	public void setExpiryTime(long expiryTime) {
		this.expiryTime = expiryTime;
	}
	
	public WarpPlate warpPlate() {
		return warpPlate;
	}

	public @Nullable WarpPlate returnPlate() {
		return returnPlate;
	}

	public void setReturnPlate(@Nullable WarpPlate returnPlate) {
		this.returnPlate = returnPlate;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (WarpPlatePair) obj;
		return this.id == that.id &&
				Objects.equals(this.warpPlate, that.warpPlate) &&
				Objects.equals(this.returnPlate, that.returnPlate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, warpPlate, returnPlate);
	}

	@Override
	public String toString() {
		return "WarpPlatePair[" +
				"id=" + id + ", " +
				"warpPlate=" + warpPlate + ", " +
				"returnPlate=" + returnPlate + ']';
	}

}
