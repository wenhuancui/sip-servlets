package org.mobicents.protocols.stream.impl;

import org.mobicents.protocols.stream.api.StreamSelector;

public class SelectorProvider {

	private SelectorProvider() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static StreamSelector create()
	{
		return new StreamSelectorImpl();
	}
	
}
