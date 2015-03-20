package de.bund.bfr.knime.pmm.pdfkeeper;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.knime.core.data.DataTableSpec;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;

import com.sun.org.apache.xml.internal.security.utils.Base64;

/**
 * <code>NodeDialog</code> for the "PdfKeeper" Node.
 * 
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more
 * complex dialog please derive directly from {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author BfR
 */
public class PdfKeeperNodeDialog extends NodeDialogPane {

    private String fileName = null;
    private String fileBytes = null;
    private JLabel tFileName;

	/**
	 * New pane for configuring the PdfKeeper node.
	 */
	protected PdfKeeperNodeDialog() {
		super();
        final JPanel p = new JPanel(new BorderLayout());
        JButton chooseFile = new JButton("Choose File");
        chooseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		        JFileChooser pdfDialog = new JFileChooser(); 
		        //pdfDialog.setCurrentDirectory(new java.io.File(textField2.getText()));
		        pdfDialog.setDialogTitle("Choose a pdf file");
		        pdfDialog.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		        pdfDialog.setMultiSelectionEnabled(false);
		        pdfDialog.setAcceptAllFileFilterUsed(false);
		        FileFilter filter = new FileNameExtensionFilter("PDF File","pdf");
		        pdfDialog.setFileFilter(filter);
				if (pdfDialog.showOpenDialog(p) == JFileChooser.APPROVE_OPTION) {
					File f = pdfDialog.getSelectedFile();
					fileName = f.getName();
					tFileName.setText(fileName);
			    	fileBytes = null;
			    	Path path = Paths.get(f.getAbsolutePath());
					try {
						fileBytes = Base64.encode(Files.readAllBytes(path));
					} catch (IOException e1) {
						e1.printStackTrace();
					}			
				}
				else {
					fileBytes = null; fileName = null;
				}
			}
		});
        p.add(chooseFile, BorderLayout.NORTH);
        
        tFileName = new JLabel();
        tFileName.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(tFileName, BorderLayout.CENTER);

		JButton openFile = new JButton("Open File");
		openFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println("You clicked the button");
				openPDF();
			}
		});

		p.add(openFile, BorderLayout.SOUTH);

        super.addTab("Settings", p);
	}
	private void openPDF() {
		try {
			if (fileBytes != null && fileName != null) {
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						try {
							String tmpFolder = System.getProperty("java.io.tmpdir");
							String pathname = "";
							if (tmpFolder != null && tmpFolder.length() > 0) {
								FileOutputStream out = null;
								try {
									if (!tmpFolder.endsWith(System.getProperty("file.separator"))) {
										tmpFolder += System.getProperty("file.separator");
									}
									pathname = tmpFolder + fileName;
									out = new FileOutputStream(pathname);
									out.write(Base64.decode(fileBytes));
								} finally {
									if (out != null) {
										out.close();
									}
								}
								if (pathname.length() > 0) {
									Runtime.getRuntime().exec(new String[] { "rundll32", "url.dll,FileProtocolHandler", new File(pathname).getAbsolutePath() });
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				};
				Thread thread = new Thread(runnable);
				thread.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings,
            final DataTableSpec[] specs) throws NotConfigurableException {
    	try {
    		if (settings.containsKey(PdfKeeperNodeModel.PDF_FILE)) fileName = settings.getString(PdfKeeperNodeModel.PDF_FILE);
    		tFileName.setText(fileName);
	    	if (settings.containsKey(PdfKeeperNodeModel.PDF_BYTES)) fileBytes = settings.getString(PdfKeeperNodeModel.PDF_BYTES);
		} catch (InvalidSettingsException e) {
			e.printStackTrace();
		}
    }
	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		settings.addString(PdfKeeperNodeModel.PDF_FILE, fileName);
		settings.addString(PdfKeeperNodeModel.PDF_BYTES, fileBytes);		
	}
}