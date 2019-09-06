package uk.ac.reading.cs.knime.sax;

import java.awt.Color;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriodValues;
import org.jfree.data.time.TimePeriodValuesCollection;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "SAX" Node.
 * Read and discretise time series data
 * @author Ryan Faulkner
 */
public class SAXNodeView extends NodeView<SAXNodeModel> {
	private static DateFormat df;
    
	/**
	 * Creates a new view.
	 * @param nodeModel The model (class: {@link SAXNodeModel})
	 */
	protected SAXNodeView(final SAXNodeModel nodeModel) {
		super(nodeModel);
		NormalAlphabet na = new NormalAlphabet();
		
		if(nodeModel.converter.ts_d[0].length()<=12)
			df = new SimpleDateFormat("HH:mm:ss.SSS");
		else
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		//Create plot using model data
		//Plot z-normalised time series data
        JFreeChart lineScatterPlot = ChartFactory.createXYLineChart("SAX String", "Time", "Series", getDataset(nodeModel));
        //Retreive plot and renderer
        XYPlot p = lineScatterPlot.getXYPlot();
                
        DateAxis dateAxis = new DateAxis();
        dateAxis.setDateFormatOverride(df); 
        p.setDomainAxis(dateAxis);
        
        //Plot PAA transform and SAX string as bar charts
        p.setDataset(1, getSAXDataset(nodeModel));
        XYBarRenderer r2 = new XYBarRenderer();
        r2.setSeriesPaint(0, new Color(1,1,1,.9f));
        p.setRenderer(1, r2);
        
        //Plot SAX characters alongside relevant lines
        for(int i=0; i<nodeModel.converter.paa.size(); i++) {
        	try {
        		PAARecord paa = nodeModel.converter.paa.get(i);
            	int start = (int) Math.floor(paa.start);
            	int end = (int) Math.floor(paa.end);
            	if(end >= nodeModel.converter.ts_d.length)
            		end--;
        		Millisecond m = new Millisecond(df.parse(nodeModel.converter.ts_d[start]));
        		Millisecond m2 = new Millisecond(df.parse(nodeModel.converter.ts_d[end]));
        		
        		char s = TSProcessor.num2char(nodeModel.converter.paa.get(i).level, na.getCuts(nodeModel.params.SAX_ALPHABET_SIZE));
        		
            	XYTextAnnotation annotation = new XYTextAnnotation(String.valueOf(s), (m.getLastMillisecond()+m2.getLastMillisecond())/2, paa.level);
            	p.addAnnotation(annotation);
			} catch (ParseException | SAXException e) { e.printStackTrace(); }
        }
        //Set plot as the component of the view
        ChartPanel chartPanel = new ChartPanel(lineScatterPlot);
		setComponent(chartPanel);
	}

	private static XYDataset getDataset(final SAXNodeModel nodeModel) {
    	TimeSeriesCollection dataset = new TimeSeriesCollection();
    	//Plot raw timeseries data
    	TimeSeries data = new TimeSeries("Normalised Timeseries");
    	for(int i=0; i<nodeModel.converter.ts_z.length; i++) {
			try { data.addOrUpdate(new Millisecond(df.parse(nodeModel.converter.ts_d[i])), nodeModel.converter.ts_z[i]);
			} catch (ParseException e) { e.printStackTrace(); } 
    	}
    	dataset.addSeries(data);
    	//Return set of series as dataset
    	return dataset;
    }
	
    private static XYDataset getSAXDataset(final SAXNodeModel nodeModel) { 
    	TimePeriodValuesCollection dataset = new TimePeriodValuesCollection();
    	//Plot sax string
    	TimePeriodValues sax = new TimePeriodValues("PAA - SAX String");
    	Calendar sCal = Calendar.getInstance();
    	Calendar eCal = Calendar.getInstance();
    	try {
			long unitDiff = df.parse(nodeModel.converter.ts_d[1]).getTime() - df.parse(nodeModel.converter.ts_d[0]).getTime();
	    	for(PAARecord p: nodeModel.converter.paa) {
    			int start = (int)p.start;
    			double startFract = p.start-start;
    			Date s = df.parse(nodeModel.converter.ts_d[start]);
    			sCal.setTime(s);
    			if(startFract!=0)
    				sCal.add(Calendar.MILLISECOND, (int) (unitDiff*startFract));    			
    			int end = (int) p.end;
    			double endFract = p.end-end;
    			if(end >= nodeModel.converter.ts_d.length)
    				end = nodeModel.converter.ts_d.length-1;
    			Date e = df.parse(nodeModel.converter.ts_d[end]);
    			eCal.setTime(e);
    			if(endFract!=0)
    				eCal.add(Calendar.MILLISECOND, (int) (unitDiff*endFract));
    			
    			if(sCal.before(eCal))
    				sax.add(new SimpleTimePeriod(sCal.getTime(), eCal.getTime()), p.level);
	    	}
	    	dataset.addSeries(sax);
    	} catch (ParseException e1) { e1.printStackTrace(); }
    	return dataset;
    }

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void modelChanged() {
		// TODO retrieve the new model from your nodemodel and update the view.
		SAXNodeModel nodeModel = (SAXNodeModel)getNodeModel();
		assert nodeModel != null;

        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onClose() {
		// TODO things to do when closing the view
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onOpen() {
		// TODO things to do when opening the view
	}
}