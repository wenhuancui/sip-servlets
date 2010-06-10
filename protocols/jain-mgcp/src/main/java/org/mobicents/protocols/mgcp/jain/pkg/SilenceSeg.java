package org.mobicents.protocols.mgcp.jain.pkg;

public class SilenceSeg {
	private String silenceSeg = null;

	public SilenceSeg(String silenceSeg) {
		this.silenceSeg = silenceSeg;
	}

	@Override
	public String toString() {
		String s = ParameterEnum.si + "(" + this.silenceSeg + ")";
		return s;
	}

}
