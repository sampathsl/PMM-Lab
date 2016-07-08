package de.bund.bfr.knime.pmm.combinedfsk.archive;

/**
 * Keeps the linkage between two models.
 * <p>
 * E.g: {@code }
 * 
 * @author Miguel Alba, BfR, Berlin
 */
public class Link {

	public final int originModelId;
	public final String originVar;

	public final int destModelId;
	public final String destVar;

	public Link(final int originModelId, final String originVar, final int destModelId, final String destVar) {
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
		Link other = (Link) obj;
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