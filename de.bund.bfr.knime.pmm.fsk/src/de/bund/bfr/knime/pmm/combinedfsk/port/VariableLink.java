package de.bund.bfr.knime.pmm.combinedfsk.port;

import java.io.Serializable;

public class VariableLink implements Serializable {

	private static final long serialVersionUID = 8394179818213471453L;
	
	public final int originModelId;
	public final String originVar;

	public final int destModelId;
	public final String destVar;
	
	public VariableLink(final int originModelId, final String originVar, final int destModelId, final String destVar) {
		this.originModelId = originModelId;
		this.originVar = originVar;
		this.destModelId = destModelId;
		this.destVar = destVar;
	}
}
