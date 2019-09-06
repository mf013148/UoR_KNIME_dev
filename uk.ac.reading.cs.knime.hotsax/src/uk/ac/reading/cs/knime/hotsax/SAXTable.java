package uk.ac.reading.cs.knime.hotsax;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.RowIterator;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.ExecutionContext;

public class SAXTable implements DataTable {
	private DataTableSpec dataTableSpec = createDataTableSpec();
	private String filename;
	
	public SAXTable(String filename, final ExecutionContext exec) {
		this.filename=filename;
	}

	public static DataTableSpec createDataTableSpec() {
		DataColumnSpec word = new DataColumnSpecCreator("Word", DataType.getType(StringCell.class)).createSpec();
		DataColumnSpec pos  = new DataColumnSpecCreator("Position", DataType.getType(IntCell.class)).createSpec();
		DataColumnSpec len  = new DataColumnSpecCreator("Length", DataType.getType(IntCell.class)).createSpec();
		DataColumnSpec dist = new DataColumnSpecCreator("Nearest Neighbour Distance", DataType.getType(DoubleCell.class)).createSpec();
		DataColumnSpec rid  = new DataColumnSpecCreator("Rule ID", DataType.getType(IntCell.class)).createSpec();
		return new DataTableSpec(word, pos, len, dist, rid);
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