package uk.ac.reading.cs.knime.hotsax;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.knime.core.node.NodeView;

/**
 * <code>NodeView</code> for the "HotSAXProcessor" Node.
 * Finds time series discords; maximally different subsequences within a time series.
 * @author Ryan Faulkner
 */
public class HotSAXNodeView extends NodeView<HotSAXNodeModel> {
	private static DateFormat df;

    /**
     * Creates a new view.
     * @param nodeModel The model (class: {@link HotSAXNodeModel})
     */
    protected HotSAXNodeView(final HotSAXNodeModel nodeModel) {
        super(nodeModel);
        
        if(nodeModel.hsp.ts_d[0].length()<=12)
			df = new SimpleDateFormat("HH:mm:ss.SSS");
		else
			df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        
        //Create plot using model data
        JFreeChart lineScatterPlot = ChartFactory.createXYLineChart("HotSAX Discords", "Time", "Series", getDataset(nodeModel));
        //Retreive plot and renderer
        XYPlot p = lineScatterPlot.getXYPlot();
                
        DateAxis dateAxis = new DateAxis();
        dateAxis.setDateFormatOverride(df); 
        p.setDomainAxis(dateAxis);

        //Set plot as the component of the view
        ChartPanel chartPanel = new ChartPanel(lineScatterPlot);
		setComponent(chartPanel);
    }

    private static XYDataset getDataset(final HotSAXNodeModel nodeModel) {
    	TimeSeriesCollection dataset = new TimeSeriesCollection();
    	//Plot timeseries discords
    	int id=1;
    	for(DiscordRecord dr : nodeModel.hsp.dr) {
    		TimeSeries series = new TimeSeries("Discord_"+id);
					for(int l=0; l<dr.getLength(); l++)
						try { series.addOrUpdate(new Millisecond(df.parse(nodeModel.hsp.ts_d[dr.getPosition()+l])), nodeModel.hsp.ts[dr.getPosition()+l]);
						} catch (ParseException e) { e.printStackTrace(); }
        	dataset.addSeries(series);
    		id++;
    	}
    	//Plot raw timeseries data
    	TimeSeries data = new TimeSeries("Timeseries");
    	for(int i=0; i<nodeModel.hsp.ts.length; i++)
			try { data.addOrUpdate(new Millisecond(df.parse(nodeModel.hsp.ts_d[i])), nodeModel.hsp.ts[i]);
			} catch (ParseException e) { e.printStackTrace(); } 
    	dataset.addSeries(data);
    	//Return set of series as dataset
    	return dataset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {
        // Retrieve the new model from your nodemodel and update the view.
        HotSAXNodeModel nodeModel = 
            (HotSAXNodeModel)getNodeModel();
        assert nodeModel != null;
        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {}

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {}
}