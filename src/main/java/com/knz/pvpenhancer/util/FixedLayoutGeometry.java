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
}
