package de.bund.bfr.knime.pmm.combinedfsk.creator;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.nio.file.InvalidPathException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DialogComponentFileChooser;
import org.knime.core.node.defaultnodesettings.DialogComponentStringListSelection;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.defaultnodesettings.SettingsModelStringArray;

import de.bund.bfr.knime.pmm.common.KnimeUtils;

public class CombinedFskCreatorNodeDialog extends NodeDialogPane {

	// model
	private final SettingsModelString modelScript;
	private final SettingsModelString parametersScript;
	private final SettingsModelString visualizationScript;
	private final SettingsModelString metadata;
	private final SettingsModelString libDirectory;
	private final SettingsModelStringArray libraries;

	// type of the dialogs
	private static final int dialogType = JFileChooser.OPEN_DIALOG;

	public CombinedFskCreatorNodeDialog() {

		// initialize models
		modelScript = new SettingsModelString(CombinedFskCreatorNodeModel.MODEL_SCRIPT, "");
		parametersScript = new SettingsModelString(CombinedFskCreatorNodeModel.PARAM_SCRIPT, "");
		visualizationScript = new SettingsModelString(CombinedFskCreatorNodeModel.VIZ_SCRIPT, "");
		metadata = new SettingsModelString(CombinedFskCreatorNodeModel.METADATA, "");
		libDirectory = new SettingsModelString(CombinedFskCreatorNodeModel.LIB_DIR, "");
		libraries = new SettingsModelStringArray(CombinedFskCreatorNodeModel.LIBRARIES, new String[0]);

		// Creates GUI
		Box box = Box.createHorizontalBox();
		box.add(createFilesSelection());
		box.add(createLibrariesSelection());

		addTab("Selection", box);
		removeTab("Options");
	}

	private Box createFilesSelection() {
		String rFilters = ".r|.R"; // Extension filters for the R scripts

		DialogComponentFileChooser modelChooser = new DialogComponentFileChooser(modelScript,
				"modelScript-history", dialogType, rFilters);
		modelChooser.setBorderTitle("Select model script");

		DialogComponentFileChooser paramChooser = new DialogComponentFileChooser(parametersScript,
				"paramScript-history", dialogType, rFilters);
		paramChooser.setBorderTitle("Select param script");

		DialogComponentFileChooser vizChooser = new DialogComponentFileChooser(visualizationScript,
				"vizScript-history", dialogType, rFilters);
		vizChooser.setBorderTitle("Select visualization script");

		DialogComponentFileChooser metaDataChooser = new DialogComponentFileChooser(metadata, "metaData-history",
				dialogType);
		metaDataChooser.setBorderTitle("Select spreadsheet");

		Box box = Box.createVerticalBox();
		box.add(modelChooser.getComponentPanel());
		box.add(paramChooser.getComponentPanel());
		box.add(vizChooser.getComponentPanel());
		box.add(metaDataChooser.getComponentPanel());

		return box;
	}

	private Box createLibrariesSelection() {
		DialogComponentStringListSelection libraryChooser = new DialogComponentStringListSelection(libraries,
				"Select libraries", Arrays.asList(""), true, 10);

		DialogComponentFileChooser dirChooser = new DialogComponentFileChooser(libDirectory, "libraries directory",
				dialogType, true);
		dirChooser.addChangeListener(new ChangeListener() {

			private FileFilter filter = new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					return new FileNameExtensionFilter("Zip files", "zip").accept(pathname);
				}
			};

			@Override
			public void stateChanged(ChangeEvent e) {
				try {
					File dirFile = KnimeUtils.getFile(libDirectory.getStringValue());
					File[] filesInDir = dirFile.listFiles(filter);
					if (filesInDir.length > 0) {
						List<String> fnames = Arrays.stream(filesInDir).map(File::getName).collect(Collectors.toList());
						libraryChooser.replaceListItems(fnames, (String[]) null);
					}
				} catch (InvalidPathException | MalformedURLException error) {
					error.printStackTrace();
				}
			}
		});

		Box box = Box.createVerticalBox();
		box.add(dirChooser.getComponentPanel());
		box.add(libraryChooser.getComponentPanel());

		return box;
	}

	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		try {
			modelScript.loadSettingsFrom(settings);
			parametersScript.loadSettingsFrom(settings);
			visualizationScript.loadSettingsFrom(settings);
			metadata.loadSettingsFrom(settings);
			libDirectory.loadSettingsFrom(settings);
			libraries.loadSettingsFrom(settings);
		} catch (InvalidSettingsException error) {
			throw new NotConfigurableException(error.getMessage(), error.getCause());
		}
	}

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		modelScript.saveSettingsTo(settings);
		parametersScript.saveSettingsTo(settings);
		visualizationScript.saveSettingsTo(settings);
		metadata.saveSettingsTo(settings);
		libDirectory.saveSettingsTo(settings);
		libraries.saveSettingsTo(settings);
	}
}
