package de.bund.bfr.knime.pmm.js.common.schema;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import de.bund.bfr.knime.pmm.js.common.ViewValue;

@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property="@class")
public class Model1SchemaList implements ViewValue {
	
	private static final String NUM_SCHEMAS = "numSchemas";
	private static final String SCHEMAS = "schemas";
	
	private int numSchemas;
	private Model1Schema[] schemas;
	
	public Model1Schema[] getSchemas() { return schemas; }
	
	public void setModels(final Model1Schema[] schemas) {
		numSchemas = schemas.length;
		this.schemas = schemas;
	}
	

	@Override
	public void saveToNodeSettings(NodeSettingsWO settings) {
		settings.addInt(NUM_SCHEMAS, numSchemas);
		for (int i = 0; i < numSchemas; i++) {
			schemas[i].saveToNodeSettings(settings.addNodeSettings(SCHEMAS + i));
		}
	}
	
	@Override
	public void loadFromNodeSettings(NodeSettingsRO settings) throws InvalidSettingsException {
		try {
			numSchemas = settings.getInt(NUM_SCHEMAS);
			schemas = new Model1Schema[numSchemas];
			for (int i = 0; i < numSchemas; i++) {
				schemas[i] = new Model1Schema();
				schemas[i].loadFromNodeSettings(settings.getNodeSettings(SCHEMAS + i));
			}
		} catch (InvalidSettingsException e) {
		}
	}
}