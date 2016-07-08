package de.bund.bfr.knime.pmm.combinedfsk.port;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
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
import javax.swing.ListSelectionModel;

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

	private final ArrayList<FskModel> models;
	private final ArrayList<VariableLink> links;

	private final int id;
	private static int numOfInstances = 0;

	public CombinedFskPortObject(final List<FskModel> models, final List<VariableLink> links) {
		this.models = new ArrayList<>(models);
		this.links = new ArrayList<>(links);

		id = numOfInstances;
		numOfInstances++;
	}

	public int getId() {
		return id;
	}

	public List<FskModel> getModels() {
		return models;
	}

	public List<VariableLink> getLinks() {
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
		for (int i = 0; i < models.size(); i++) {
			listModel.addElement("Model " + i);
		}

		JScrollPane scrollPane = new JScrollPane(new JList<>(listModel));

		JPanel modelScriptPanel = new ScriptPanel("Model script", models.get(0).getModelScript(), false);
		JPanel paramScriptPanel = new ScriptPanel("Param script", models.get(0).getParametersScript(), false);
		JPanel vizScriptPanel = new ScriptPanel("Visualization script", models.get(0).getVisualizationScript(), false);

		// Libraries panel
		String[] libNames;
		if (models.get(0).isSetLibraries()) {
			libNames = models.get(0).getLibraries().toArray(new String[0]);
		} else {
			libNames = new String[0];
		}

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Model script", modelScriptPanel);
		tabbedPane.addTab("Parameters script", paramScriptPanel);
		tabbedPane.addTab("Visualization script", vizScriptPanel);
		tabbedPane.addTab("Libraries", new LibrariesPanel(libNames));

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

			LinkedList<FskModel> models = new LinkedList<>();
			LinkedList<VariableLink> links = new LinkedList<>();

			ZipEntry entry;
			while ((entry = in.getNextEntry()) != null) {

				ObjectInputStream objectStream = new ObjectInputStream(in);
				try {
					if (entry.getName().equals("model")) {
						models.add((FskModel) objectStream.readObject());
					} else if (entry.getName().equals("replacement")) {
						links.add((VariableLink) objectStream.readObject());
					}
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e.getMessage(), e.getCause());
				}
			}
			in.close();

			return new CombinedFskPortObject(models, links);
		}
	}

	private class LibrariesPanel extends JPanel {

		private static final long serialVersionUID = 5664169861264900195L;

		LibrariesPanel(String[] libNames) {
			super(new BorderLayout());
			setName("Libraries list");

			JList<String> list = new JList<>(libNames);
			list.setLayoutOrientation(JList.VERTICAL);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			add(new JScrollPane(list));
		}
	}
}