package de.bund.bfr.knime.pmm.combinedfsk.port;

import java.io.Serializable;

public class Replacement implements Serializable {

	private static final long serialVersionUID = 8394179818213471453L;
	
	public final String originVar;
	public final String destVar;
	
	public Replacement(final String originVar, final String destVar) {
		this.originVar = originVar;
		this.destVar = destVar;
	}
}
