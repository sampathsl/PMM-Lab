package de.bund.bfr.knime.pmm.combinedfsk.port;

import java.io.Serializable;

public class VariableLink implements Serializable {

	private static final long serialVersionUID = 8394179818213471453L;
	
	public final int originModelId;
	public final String originVar;

	public final int destModelId;
	public final String destVar;
	
	public VariableLink(final int originModelId, final String originVar,
		final int destModelId, final String destVar) {
		this.originModelId = originModelId;
		this.originVar = originVar;
		this.destModelId = destModelId;
		this.destVar = destVar;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + destModelId;
		result = prime * result + ((destVar == null) ? 0 : destVar.hashCode());
		result = prime * result + originModelId;
		result = prime * result + ((originVar == null) ? 0 : originVar.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariableLink other = (VariableLink) obj;
		if (destModelId != other.destModelId)
			return false;
		if (destVar == null) {
			if (other.destVar != null)
				return false;
		} else if (!destVar.equals(other.destVar))
			return false;
		if (originModelId != other.originModelId)
			return false;
		if (originVar == null) {
			if (other.originVar != null)
				return false;
		} else if (!originVar.equals(other.originVar))
			return false;
		return true;
	}
}
