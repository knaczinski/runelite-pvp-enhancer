package com.knz.pvpenhancer;

/**
 * Which actors an overhead-element feature applies to.
 */
public enum OverheadScope
{
	OFF("Off"),
	SELF("Self"),
	OPPONENTS("Self + opponents"),
	EVERYONE("Everyone");

	private final String label;

	OverheadScope(String label)
	{
		this.label = label;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
