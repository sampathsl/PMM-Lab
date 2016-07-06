package de.bund.bfr.knime.pmm.combinedfsk.port;

import java.io.IOException;

import javax.swing.JComponent;

import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectSpecZipInputStream;
import org.knime.core.node.port.PortObjectSpecZipOutputStream;

/**
 * A port object spec for Combined FSK models.
 *
 * @author Miguel Alba, BfR, Berlin.
 */
public class CombinedFskPortObjectSpec implements PortObjectSpec {

	public static final CombinedFskPortObjectSpec INSTANCE =
		new CombinedFskPortObjectSpec();

	private CombinedFskPortObjectSpec() {
		// empty
	}

	/** Serializer used to save this port object spec. */
	public static final class Serializer
		extends PortObjectSpecSerializer<CombinedFskPortObjectSpec> {
		
		/** {@inheritDoc} */
		@Override
		public CombinedFskPortObjectSpec loadPortObjectSpec(
			final PortObjectSpecZipInputStream in) throws IOException {
			return INSTANCE;
		}			

		/** {@inheritDoc} */
		@Override
		public void savePortObjectSpec(
			final CombinedFskPortObjectSpec portObjectSpec,
			PortObjectSpecZipOutputStream out) throws IOException {
			// empty
		}
	}

	@Override
	public JComponent[] getViews() {
		return new JComponent[] {};
	}
}