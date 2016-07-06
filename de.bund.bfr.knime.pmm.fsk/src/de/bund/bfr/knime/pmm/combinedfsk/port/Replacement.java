package de.bund.bfr.knime.pmm.combinedfsk.port;

import java.io.Serializable;

public class Replacement implements Serializable {

	public final String originVar;
	public final String destVar;
	
	public Replacement(final String originVar, final String destVar) {
		this.originVar = originVar;
		this.destVar = destVar;
	}
}
