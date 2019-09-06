package uk.ac.reading.cs.knime.sax;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.RowIterator;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.StringCell;
//TODO
public class SAXIterator extends RowIterator {
	private PushbackReader reader = null;
	StringBuilder name=new StringBuilder();
	StringBuilder seq=new StringBuilder();
	int rowIndex=0;
	private boolean has_next_tested=false;
	private boolean has_next=false;
	
	SAXIterator(String source) {
		try {
			this.reader = new PushbackReader(new BufferedReader(new FileReader(source)));
		} catch(IOException err) {
			this.reader=null;
			err.printStackTrace();
		}
	}

	@Override
	public boolean hasNext() {
		System.err.println("hasNext called");
		
		if(has_next_tested) 
			return has_next;
		
		has_next_tested=true;
		has_next=false;
		
		if(reader==null) 
			return has_next;
		
		try {
			int c;
			while((c=reader.read())!=-1) {
				if(Character.isWhitespace(c)) 
					continue;
				break;
			}
			if(c!=',')
				throw new IOException("expected ','");
			
			while((c=reader.read())!=-1) {
				if(c=='\n') 
					break;
				name.append((char)c);
			}
			
			boolean at_start=true;
			System.err.println("Found >"+name);
			
			while((c=reader.read())!=-1) {
				if(at_start && c==',') {
					reader.unread(c);
					break;
				}
				else if(c=='\n') {
					at_start=true;
					continue;
				}
				at_start=false;
				if(Character.isWhitespace(c)) 
					continue;
				seq.append((char)c);
			}
			System.err.println("Found :"+seq);
		}
		catch(IOException err) {
			try { reader.close(); reader=null; } catch(IOException err2) {} 
		}
		
		System.err.println("name & seq: "+name+" "+seq);
		has_next=name.length()>0;
		return has_next;
	}

	@SuppressWarnings("deprecation")
	@Override
	public DataRow next() {
		if(!has_next_tested) 
			hasNext();
		if(!has_next) 
			throw new IllegalStateException("No next SAX sequence");
		
		has_next_tested=false;
		has_next=false;
		DataCell cellName=new StringCell(this.name.toString());
		DataCell cellSeq=new StringCell(this.seq.toString());
		this.name.setLength(0);
		this.seq.setLength(0);
		int index=rowIndex;
		++rowIndex;
		return new DefaultRow(RowKey.createRowKey(index),cellName,cellSeq);
	}
}