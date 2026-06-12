package com.knz.pvpenhancer.util;

/**
 * Geometry constants for the fixed-layout-in-resizable feature (B030), shared by the plugin (which
 * relocates the live Resizable-Classic widgets) and the guide overlay (which draws the reference
 * outline you drag). All block positions are the fixed-mode top-left of each panel <b>relative to
 * the fixed client's top-left</b> (the 765×503 footprint), measured from the fixed-mode (group 548)
 * widget dump. Block sizes are the Resizable-Classic (group 161) root-container bounds.
 *
 * <p>The guide overlay is movable: its on-screen top-left is the fixed-client origin, so a block's
 * live target is simply {@code guideTopLeft + (blockFixedX, blockFixedY)}.
 */
public final class FixedLayoutGeometry
{
	private FixedLayoutGeometry()
	{
	}

	/** Fixed 3D scene (viewport) size + its inset inside the client footprint. */
	public static final int SCENE_W = 512;
	public static final int SCENE_H = 334;
	public static final int SCENE_INSET = 4;

	/** Whole fixed client footprint (the draggable guide handle). */
	public static final int CLIENT_W = 765;
	public static final int CLIENT_H = 503;

	/** Inventory / tabs panel (root child 97): fixed top-left + size. */
	public static final int INV_FX = 516, INV_FY = 167, INV_W = 241, INV_H = 335;
	/** Minimap + orbs panel (root child 95). */
	public static final int MM_FX = 516, MM_FY = 4, MM_W = 211, MM_H = 207;
	/** Chatbox (root child 96). */
	public static final int CHAT_FX = 0, CHAT_FY = 338, CHAT_W = 519, CHAT_H = 165;
}
