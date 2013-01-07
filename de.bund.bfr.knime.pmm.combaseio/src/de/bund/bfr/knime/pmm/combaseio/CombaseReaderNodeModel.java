/*******************************************************************************
 * PMM-Lab � 2012, Federal Institute for Risk Assessment (BfR), Germany
 * 
 * PMM-Lab is a set of KNIME-Nodes and KNIME workflows running within the KNIME software plattform (http://www.knime.org.).
 * 
 * PMM-Lab � 2012, Federal Institute for Risk Assessment (BfR), Germany
 * Contact: armin.weiser@bfr.bund.de or matthias.filter@bfr.bund.de 
 * 
 * Developers and contributors to the PMM-Lab project are 
 * J�rgen Brandt (BfR)
 * Armin A. Weiser (BfR)
 * Matthias Filter (BfR)
 * Alexander Falenski (BfR)
 * Christian Th�ns (BfR)
 * Annemarie K�sbohrer (BfR)
 * Bernd Appel (BfR)
 * 
 * PMM-Lab is a project under development. Contributions are welcome.
 * 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.bund.bfr.knime.pmm.combaseio;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import de.bund.bfr.knime.pmm.combaseio.lib.CombaseReader;

import de.bund.bfr.knime.pmm.common.CatalogModelXml;
import de.bund.bfr.knime.pmm.common.DepXml;
import de.bund.bfr.knime.pmm.common.EstModelXml;
import de.bund.bfr.knime.pmm.common.IndepXml;
import de.bund.bfr.knime.pmm.common.ParamXml;
import de.bund.bfr.knime.pmm.common.PmmException;
import de.bund.bfr.knime.pmm.common.PmmTimeSeries;
import de.bund.bfr.knime.pmm.common.PmmXmlDoc;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeSchema;
import de.bund.bfr.knime.pmm.common.generictablemodel.KnimeTuple;
import de.bund.bfr.knime.pmm.common.math.MathUtilities;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.Model1Schema;
import de.bund.bfr.knime.pmm.common.pmmtablemodel.TimeSeriesSchema;

/**
 * This is the model implementation of CombaseReader.
 * 
 *
 * @author Jorgen Brandt
 */
public class CombaseReaderNodeModel extends NodeModel {
		
		
	protected static final String PARAM_FILENAME = "filename";	
	protected static final String PARAM_STARTELIM = "startElim";
	protected static final String PARAM_STARTGROW = "startGrow";
	private static final String COMMENT_CLAUSE = "LogC0 artificially generated by Combase Reader.";
	
	protected static final String DEFAULT_FILENAME = "";
	protected static final double DEFAULT_STARTELIM = 10.0;
	protected static final double DEFAULT_STARTGROW = 0.0;

	private String filename;
	private double startElim;
	private double startGrow;
    
    /**
     * Constructor for the node model.
     */
    protected CombaseReaderNodeModel() {
    	
        super( 0, 2 );
        
        filename = DEFAULT_FILENAME;
        startElim = DEFAULT_STARTELIM;
        startGrow = DEFAULT_STARTGROW;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BufferedDataTable[] execute(final BufferedDataTable[] inData,
            final ExecutionContext exec) throws Exception {
    	
    	BufferedDataContainer buf, buf2;
    	CombaseReader reader;
    	PmmTimeSeries candidate;
    	int j;
    	PmmXmlDoc doc;
    	ParamXml paramXml;
    	KnimeSchema tsm1Schema;
    	KnimeTuple modelTuple;
    	double start;
    	KnimeSchema commonSchema;
    	
    	tsm1Schema = KnimeSchema.merge( new TimeSeriesSchema(), new Model1Schema() );

    	// initialize combase reader
    	reader = new CombaseReader( filename );
    	
    	// initialize table buffer
    	buf = exec.createDataContainer( new TimeSeriesSchema().createSpec() );
    	buf2 = exec.createDataContainer( tsm1Schema.createSpec() );
    	
    	j = 0;
		commonSchema = KnimeSchema.merge( new Model1Schema(), new TimeSeriesSchema() );
    	while( reader.hasMoreElements() ) {
    		
    		// fetch time series
    		candidate = reader.nextElement();
    		
    		if( candidate.isEmpty() ) {
    			
    			PmmXmlDoc indepXML = new PmmXmlDoc();
    			
    			modelTuple = new KnimeTuple( commonSchema );
    			indepXML.add(new IndepXml(TimeSeriesSchema.TIME, null, null));
    			
    			modelTuple = KnimeTuple.merge( commonSchema, modelTuple, candidate );
    			
    			PmmXmlDoc cmDoc = new PmmXmlDoc();
    			CatalogModelXml cmx = new CatalogModelXml(MathUtilities.getRandomNegativeInt(), "D-Value", TimeSeriesSchema.LOGC+"=LogC0+mumax*"+TimeSeriesSchema.TIME);
    			cmDoc.add(cmx);
    			modelTuple.setValue( Model1Schema.ATT_MODELCATALOG, cmDoc);
    			
    			//modelTuple.setValue( Model1Schema.ATT_FORMULA, TimeSeriesSchema.LOGC+"=LogC0+mumax*"+TimeSeriesSchema.TIME );
    			//modelTuple.setValue( Model1Schema.ATT_PARAMNAME, "LocC0,mumax" );
    			
    			if( Double.isNaN( candidate.getMaximumRate() )
					|| Double.isInfinite( candidate.getMaximumRate() ) )
    				continue;
    			
    			if( candidate.getMaximumRate() >= 0 )
    				start = startGrow;
    			else
    				start = startElim;
    			
    			PmmXmlDoc paramDoc = new PmmXmlDoc();
				ParamXml px = new ParamXml("LocC0",start,null,null,null,null,null);
				paramDoc.add(px);
				px = new ParamXml("mumax",candidate.getMaximumRate(),null,null,null,null,null);
				paramDoc.add(px);
				modelTuple.setValue(Model1Schema.ATT_PARAMETER, paramDoc);
    			//modelTuple.setValue( Model1Schema.ATT_VALUE, start+","+candidate.getMaximumRate() );
    			modelTuple.setValue( Model1Schema.ATT_INDEPENDENT, indepXML );
    			paramDoc = new PmmXmlDoc();
				DepXml dx = new DepXml(TimeSeriesSchema.LOGC);
				paramDoc.add(dx);
				modelTuple.setValue(Model1Schema.ATT_DEPENDENT, paramDoc);
    			//modelTuple.setValue(Model1Schema.ATT_DEPVAR, TimeSeriesSchema.LOGC);
    			//modelTuple.setValue( Model1Schema.ATT_MODELID, MathUtilities.getRandomNegativeInt() );
    			int ri = MathUtilities.getRandomNegativeInt();
    			PmmXmlDoc emDoc = new PmmXmlDoc();
    			EstModelXml emx = new EstModelXml(ri, "EM_" + ri, null, null, null, null, null);
    			emDoc.add(emx);
    			modelTuple.setValue( Model1Schema.ATT_ESTMODEL, emDoc);
    			//modelTuple.setValue( Model1Schema.ATT_ESTMODELID, MathUtilities.getRandomNegativeInt() );
    			//modelTuple.setValue( Model1Schema.ATT_MININDEP, "?" );
    			//modelTuple.setValue( Model1Schema.ATT_MAXINDEP, "?" );
    			modelTuple.setValue( TimeSeriesSchema.ATT_COMMENT, COMMENT_CLAUSE );
    			
    			doc = new PmmXmlDoc();
    			
    			paramXml = new ParamXml("LogC0", start);
    			doc.add(paramXml);
    			
    			paramXml = new ParamXml("mumax", candidate.getMaximumRate());
    			doc.add(paramXml);
    			
    			modelTuple.setValue(Model1Schema.ATT_PARAMETER, doc);
    			
    			buf2.addRowToTable(new DefaultRow(String.valueOf(j++), KnimeTuple.merge(tsm1Schema, candidate, modelTuple)));
    			
    			continue;
    		}
    		
    		// doc.add( candidate );
			buf.addRowToTable( new DefaultRow( String.valueOf( j++ ), candidate ) );
    	
    	}
    	reader.close();
    	buf.close();
    	buf2.close();
    	
    	/* buf2 = exec.createDataContainer( createXmlSpec() );
    	row = new StringCell[ 1 ];
    	row[ 0 ] = new StringCell( doc.toXmlString() );
    	
    	buf2.addRowToTable( new DefaultRow( "0", row ) );
    	buf2.close(); */
    	
        // return new BufferedDataTable[]{ buf.getTable(), buf2.getTable() };
    	return new BufferedDataTable[]{ buf.getTable(), buf2.getTable() };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected DataTableSpec[] configure( final DataTableSpec[] inSpecs )
            throws InvalidSettingsException {
    	
    	DataTableSpec[] ret;
    	
    	ret = null;

    	if( filename.isEmpty() )
    		throw new InvalidSettingsException( "Filename must be specified." );
    	
       	try {
			ret = new DataTableSpec[] {
				new TimeSeriesSchema().createSpec(),
				KnimeSchema.merge(  new TimeSeriesSchema(),
									new Model1Schema() ).createSpec() };
		} catch (PmmException e) {
			e.printStackTrace();
		}
		
		return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo( final NodeSettingsWO settings ) {

    	settings.addString( PARAM_FILENAME, filename );
    	settings.addDouble( PARAM_STARTELIM, startElim );
    	settings.addDouble( PARAM_STARTGROW, startGrow );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadValidatedSettingsFrom(final NodeSettingsRO settings)
            throws InvalidSettingsException {

    	filename = settings.getString( PARAM_FILENAME );
    	startElim = settings.getDouble( PARAM_STARTELIM );
    	startGrow = settings.getDouble( PARAM_STARTGROW );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings)
            throws InvalidSettingsException {}
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {}
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveInternals(final File internDir,
            final ExecutionMonitor exec) throws IOException,
            CanceledExecutionException {}
        
    protected static DataTableSpec createXmlSpec() {
    	
    	DataColumnSpec[] spec;
    	
    	spec = new DataColumnSpec[ 1 ];
    	// spec[ 0 ] = new DataColumnSpecCreator( "xmlString", XMLCell.TYPE ).createSpec();
    	spec[ 0 ] = new DataColumnSpecCreator( "xmlString", StringCell.TYPE ).createSpec();
    	
    	return new DataTableSpec( spec );
    }

}

