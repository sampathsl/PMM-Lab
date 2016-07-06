package de.bund.bfr.knime.pmm.combinedfsk.port;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;

import javax.swing.JComponent;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

/**
 * A port object for combined FSK models.
 *
 * @author Miguel Alba, BfR, Berlin.
 */
public class CombinedFskPortObject implements PortObject {

	/**
	 * Convenience access member for
	 * <code>new PortType(FskPortObject.class)</code>
	 */
	public static final PortType TYPE = PortTypeRegistry.getInstance().getPortType(CombinedFskPortObject.class);

	private final FskModel[] models;
	private final Replacement[] replacements;

	private final int id;
	private static int numOfInstances = 0;

	public CombinedFskPortObject(final FskModel[] models, final Replacement[] replacements) {
		this.models = models;
		this.replacements = replacements;

		id = numOfInstances;
		numOfInstances++;
	}

	public int getId() {
		return id;
	}

	public FskModel[] getModels() {
		return models;
	}

	public Replacement[] getReplacements() {
		return replacements;
	}

	// --- PortObject ---

	@Override
	public String getSummary() {
		return "Combined FSK Object";
	}

	@Override
	public PortObjectSpec getSpec() {
		return CombinedFskPortObjectSpec.INSTANCE;
	}

	@Override
	public JComponent[] getViews() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Serializer used to save a CombinedFskPortObject.
	 */
	public static final class Serializer extends PortObjectSerializer<CombinedFskPortObject> {

		@Override
		public void savePortObject(CombinedFskPortObject portObject, PortObjectZipOutputStream out,
				ExecutionMonitor exec) throws IOException, CanceledExecutionException {

			for (FskModel model : portObject.models) {
				out.putNextEntry(new ZipEntry("model"));
				ObjectOutputStream objectStream = new ObjectOutputStream(out);
				objectStream.writeObject(model);
				out.closeEntry();
			}

			for (Replacement replacement : portObject.replacements) {
				out.putNextEntry(new ZipEntry("replacement"));
				ObjectOutputStream objectStream = new ObjectOutputStream(out);
				objectStream.writeObject(replacement);
				out.closeEntry();
			}

			out.close();
		}

		@Override
		public CombinedFskPortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec,
				ExecutionMonitor exec) throws IOException, CanceledExecutionException {

			List<FskModel> modelList = new LinkedList<>();
			List<Replacement> replacementList = new LinkedList<>();

			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {

				ObjectInputStream objectStream = new ObjectInputStream(in);
				try {
					if (entry.getName().equals("model")) {
						modelList.add((FskModel) objectStream.readObject());
					} else if (entry.getName().equals("replacement")) {
						replacementList.add((Replacement) objectStream.readObject());
					}
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e.getMessage(), e.getCause());
				}
			}
			in.close();

			FskModel[] modelsArray = modelList.toArray(new FskModel[modelList.size()]);
			Replacement[] replacementsArray = replacementList.toArray(new Replacement[replacementList.size()]);

			return new CombinedFskPortObject(modelsArray, replacementsArray);
		}
	}
}