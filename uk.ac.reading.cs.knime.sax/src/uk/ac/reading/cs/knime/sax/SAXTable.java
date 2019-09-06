package uk.ac.reading.cs.knime.sax;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowIterator;
import org.knime.core.data.date.DateAndTimeCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.ExecutionContext;

public class SAXTable implements DataTable {
	private DataTableSpec dataTableSpec = createDataTableSpec();
	private String filename;
	
	public SAXTable(String filename, final ExecutionContext exec) {
		this.filename=filename;
	}

	public static DataTableSpec createDataTableSpec() {
		DataColumnSpec time = new DataColumnSpecCreator("Time", DataType.getType(DateAndTimeCell.class)).createSpec();
		DataColumnSpec sax  = new DataColumnSpecCreator("SAX String", DataType.getType(StringCell.class)).createSpec();
		return new DataTableSpec(time, sax);
	}

	@Override
	public DataTableSpec getDataTableSpec() {
		return this.dataTableSpec;
	}

	//TODO
	@Override
	public RowIterator iterator() {
		return new SAXIterator(this.filename);
	}
}