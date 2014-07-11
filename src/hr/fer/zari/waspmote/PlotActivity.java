package hr.fer.zari.waspmote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

public class PlotActivity extends Activity {

	private XYPlot plot;
	private List<Date> dates;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plot);
		
		plot = (XYPlot) findViewById(R.id.plot_sensor_data);
		
		Number[] series1Numbers = {1,1,2,2,3,3,4,5,6,10,8,7};
		Number[] series2Numbers = {1,2,3,4,5,6,7,8,9,10,11,12};
		//dates = generateDates();
		
		// Number[] dateSeries = dates;
		
		// Create series and appropriate formatters
		XYSeries series1 = new SimpleXYSeries(
				Arrays.asList(series1Numbers),
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
				"Vrijednosti");
		
		LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(),
            R.xml.line_point_formatter_with_plf1);
        
        
		XYSeries series2 = new SimpleXYSeries(
				Arrays.asList(series2Numbers),
				Arrays.asList(series1Numbers),
				"Vrijeme");
		
        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_plf2);
        
        
        // add a new series' to the xyplot:
        //plot.addSeries(series1, series1Format);
        
        plot.addSeries(series2, series2Format);
        plot.setDomainLabel("Vrijeme");
        plot.setRangeLabel("Vrijednosti");
        
        //plot.setDomainStep(XYStepMode.SUBDIVIDE, dates.size());
        
        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        
        /*
        plot.setDomainValueFormat(new Format() {
        	 
            // create a simple date format that draws on the year portion of our timestamp
            // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
            // for a full description of SimpleDateFormat.
            private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
 
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
 
                // because our timestamps are in seconds and SimpleDateFormat expects milliseconds
                // we multiply our timestamp by 1000:
                long timestamp = ((Number) obj).longValue() * 1000;
                Date date = new Date(timestamp);
                return dateFormat.format(date, toAppendTo, pos);
            }
 
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });
        */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.graph, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private List<Date> generateDates() {
		//DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		ArrayList<Date> dates = new ArrayList<>();
		for (int i=0; i<=1000; i+=100) {
			dates.add(new Date(1078307200+i));
		}
		return new ArrayList<Date>();
	}

	

}
