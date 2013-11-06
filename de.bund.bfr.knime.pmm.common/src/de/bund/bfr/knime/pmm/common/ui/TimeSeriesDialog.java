package de.bund.bfr.knime.pmm.common.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import de.bund.bfr.knime.pmm.common.TimeSeriesXml;
import de.bund.bfr.knime.pmm.common.chart.ChartConstants;
import de.bund.bfr.knime.pmm.common.chart.ChartCreator;
import de.bund.bfr.knime.pmm.common.chart.Plotable;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.AttributeUtilities;
import de.bund.bfr.knime.pmm.common.units.Categories;

public class TimeSeriesDialog extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	public TimeSeriesDialog(JComponent owner, List<TimeSeriesXml> timeSeries,
			boolean showChart) {
		super(SwingUtilities.getWindowAncestor(owner), "Data Points",
				DEFAULT_MODALITY_TYPE);

		JButton okButton = new JButton("OK");
		JPanel bottomPanel = new JPanel();

		okButton.addActionListener(this);
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		bottomPanel.add(okButton);

		setLayout(new BorderLayout());

		if (showChart) {
			add(createTableChartComponent(timeSeries), BorderLayout.CENTER);
		} else {
			add(createTableComponent(timeSeries), BorderLayout.CENTER);
		}

		add(bottomPanel, BorderLayout.SOUTH);
		pack();

		setResizable(true);
		setLocationRelativeTo(owner);
		UI.adjustDialog(this);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		dispose();
	}

	private JComponent createTableComponent(List<TimeSeriesXml> timeSeries) {
		return new JScrollPane(new TimeSeriesTable(timeSeries, false, false));
	}

	private JComponent createTableChartComponent(List<TimeSeriesXml> timeSeries) {
		List<Double> timeList = new ArrayList<Double>();
		List<Double> logcList = new ArrayList<Double>();
		String timeUnit = Categories.getTimeCategory().getStandardUnit();
		String concentrationUnit = Categories.getConcentrationCategories()
				.get(0).getStandardUnit();

		for (TimeSeriesXml point : timeSeries) {
			timeList.add(point.getTime());
			logcList.add(point.getConcentration());
			timeUnit = point.getTimeUnit();
			concentrationUnit = point.getConcentrationUnit();
		}

		Plotable plotable = new Plotable(Plotable.DATASET);

		plotable.addValueList(AttributeUtilities.TIME, timeList);
		plotable.addValueList(AttributeUtilities.CONCENTRATION, logcList);

		ChartCreator creator = new ChartCreator(plotable);

		creator.setParamX(AttributeUtilities.TIME);
		creator.setParamY(AttributeUtilities.CONCENTRATION);
		creator.setTransformX(ChartConstants.NO_TRANSFORM);
		creator.setTransformY(ChartConstants.NO_TRANSFORM);
		creator.setUseManualRange(false);
		creator.setDrawLines(false);
		creator.setShowLegend(false);
		creator.setUnitX(timeUnit);
		creator.setUnitY(concentrationUnit);
		creator.createChart();

		JPanel panel = new JPanel();

		panel.setLayout(new BorderLayout());
		panel.add(createTableComponent(timeSeries), BorderLayout.EAST);
		panel.add(creator, BorderLayout.CENTER);

		return panel;
	}
}
