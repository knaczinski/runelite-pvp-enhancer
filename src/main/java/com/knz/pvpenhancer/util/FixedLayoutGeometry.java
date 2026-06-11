package com.knz.pvpenhancer.util;

/**
 * Geometry constants for the fixed-layout-in-resizable feature (B030), shared by the plugin (which
 * relocates the live Resizable-Classic widgets) and the guide overlay (which draws reference
 * outlines). Offsets are each block's fixed-mode top-left minus the fixed scene centre (≈260,171
 * within the 765×503 fixed client), measured from the fixed-mode (group 548) widget dump. Block
 * sizes are the Resizable-Classic (group 161) root-container bounds.
 *
 * <p>A pinned block's on-screen target is {@code resizableSceneCentre + (offX, offY)}.
 */
public final class FixedLayoutGeometry
{
	private FixedLayoutGeometry()
	{
	}

	/** Fixed 3D scene (viewport) size. */
	public static final int SCENE_W = 512;
	public static final int SCENE_H = 334;
	/** Fixed scene centre offset from the scene top-left (= half the scene). */
	public static final int SCENE_HALF_X = SCENE_W / 2;
	public static final int SCENE_HALF_Y = SCENE_H / 2;

	/** Whole fixed client footprint (scene sits ~4px inside it). */
	public static final int CLIENT_W = 765;
	public static final int CLIENT_H = 503;
	public static final int SCENE_INSET = 4;

	/** Inventory / tabs panel (root child 97). */
	public static final int INV_OFF_X = 256, INV_OFF_Y = -4, INV_W = 241, INV_H = 335;
	/** Minimap + orbs panel (root child 95). */
	public static final int MM_OFF_X = 256, MM_OFF_Y = -167, MM_W = 211, MM_H = 207;
	/** Chatbox (root child 96). */
	public static final int CHAT_OFF_X = -260, CHAT_OFF_Y = 167, CHAT_W = 519, CHAT_H = 165;

	/**
	 * Computes the anchor (the virtual fixed-scene centre) for the enabled blocks.
	 *
	 * <p>Base anchor is the viewport CENTRE (where the character renders) — blocks then sit at
	 * fixed-mode distance from the character, which is correct on a wide window. When the window is
	 * too narrow to fit a block (the inventory would run off the right edge), the whole anchor is
	 * auto-shifted just enough to bring the enabled blocks back on-screen, preserving their relative
	 * fixed-mode arrangement. On horizontal overflow the shift is right-biased so the inventory (the
	 * muscle-memory block) stays fully visible and the character ends up right-of-centre of the
	 * fixed overlay (like fixed mode). On a wide window the shift is zero (centre anchor unchanged).
	 *
	 * @return {@code {cx, cy}} the anchor to feed block targets as {@code (cx,cy) + blockOffset}.
	 */
	public static int[] anchor(int viewportX, int viewportY, int viewportW, int viewportH,
		int canvasW, int canvasH, int nudgeX, int nudgeY, boolean inv, boolean mm, boolean chat)
	{
		int cx = viewportX + viewportW / 2 + nudgeX;
		int cy = viewportY + viewportH / 2 + nudgeY;

		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		boolean any = false;
		if (inv)
		{
			any = true;
			minX = Math.min(minX, cx + INV_OFF_X); maxX = Math.max(maxX, cx + INV_OFF_X + INV_W);
			minY = Math.min(minY, cy + INV_OFF_Y); maxY = Math.max(maxY, cy + INV_OFF_Y + INV_H);
		}
		if (mm)
		{
			any = true;
			minX = Math.min(minX, cx + MM_OFF_X); maxX = Math.max(maxX, cx + MM_OFF_X + MM_W);
			minY = Math.min(minY, cy + MM_OFF_Y); maxY = Math.max(maxY, cy + MM_OFF_Y + MM_H);
		}
		if (chat)
		{
			any = true;
			minX = Math.min(minX, cx + CHAT_OFF_X); maxX = Math.max(maxX, cx + CHAT_OFF_X + CHAT_W);
			minY = Math.min(minY, cy + CHAT_OFF_Y); maxY = Math.max(maxY, cy + CHAT_OFF_Y + CHAT_H);
		}
		if (any)
		{
			cx += fitShift(minX, maxX, canvasW, true);  // right-bias: keep inventory visible
			cy += fitShift(minY, maxY, canvasH, false); // top-bias: keep the top (minimap) visible
		}
		return new int[]{cx, cy};
	}

	/**
	 * Shift needed to bring the span [lo,hi] inside [0,size]. Zero when it already fits inside (the
	 * wide-window case). On overflow, biases to pin the high edge (right-bias) or low edge.
	 */
	private static int fitShift(int lo, int hi, int size, boolean biasHigh)
	{
		if (hi - lo > size)
		{
			return biasHigh ? size - hi : -lo;
		}
		if (lo < 0)
		{
			return -lo;
		}
		if (hi > size)
		{
			return size - hi;
		}
		return 0;
	}
}
