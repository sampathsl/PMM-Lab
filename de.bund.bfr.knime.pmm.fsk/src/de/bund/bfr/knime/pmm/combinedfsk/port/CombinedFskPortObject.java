package de.bund.bfr.knime.pmm.combinedfsk.port;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortObjectZipInputStream;
import org.knime.core.node.port.PortObjectZipOutputStream;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.PortTypeRegistry;

import de.bund.bfr.knime.pmm.fskx.ui.ScriptPanel;

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
	private final VariableLink[] links;

	private final int id;
	private static int numOfInstances = 0;

	public CombinedFskPortObject(final FskModel[] models, final VariableLink[] links) {
		this.models = models;
		this.links = links;

		id = numOfInstances;
		numOfInstances++;
	}

	public int getId() {
		return id;
	}

	public FskModel[] getModels() {
		return models;
	}

	public VariableLink[] getLinks() {
		return links;
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
		return new JComponent[] { createView() };
	}

	private Box createView() {
		DefaultListModel<String> listModel = new DefaultListModel<>();
		for (int i = 0; i < models.length; i++) {
			listModel.addElement("Model " + i);
		}

		JScrollPane scrollPane = new JScrollPane(new JList<>(listModel));

		JPanel modelScriptPanel = new ScriptPanel("Model script", models[0].getModelScript(), false);
		JPanel paramScriptPanel = new ScriptPanel("Param script", models[0].getParametersScript(), false);
		JPanel vizScriptPanel = new ScriptPanel("Visualization script", models[0].getVisualizationScript(), false);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Model script", modelScriptPanel);
		tabbedPane.addTab("Parameters script", paramScriptPanel);
		tabbedPane.addTab("Visualization script", vizScriptPanel);

		Box box = Box.createHorizontalBox();
		box.add(scrollPane);
		box.add(tabbedPane);
		box.setName("Fsk models");

		return box;
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

			for (VariableLink link : portObject.links) {
				out.putNextEntry(new ZipEntry("link"));
				ObjectOutputStream objectStream = new ObjectOutputStream(out);
				objectStream.writeObject(link);
				out.closeEntry();
			}

			out.close();
		}

		@Override
		public CombinedFskPortObject loadPortObject(PortObjectZipInputStream in, PortObjectSpec spec,
				ExecutionMonitor exec) throws IOException, CanceledExecutionException {

			List<FskModel> modelList = new LinkedList<>();
			List<VariableLink> linkList = new LinkedList<>();

			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {

				ObjectInputStream objectStream = new ObjectInputStream(in);
				try {
					if (entry.getName().equals("model")) {
						modelList.add((FskModel) objectStream.readObject());
					} else if (entry.getName().equals("replacement")) {
						linkList.add((VariableLink) objectStream.readObject());
					}
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e.getMessage(), e.getCause());
				}
			}
			in.close();

			FskModel[] modelsArray = modelList.toArray(new FskModel[modelList.size()]);
			VariableLink[] replacementsArray = linkList.toArray(new VariableLink[linkList.size()]);

			return new CombinedFskPortObject(modelsArray, replacementsArray);
		}
	}
}