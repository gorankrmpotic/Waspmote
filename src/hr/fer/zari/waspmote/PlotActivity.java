package hr.fer.zari.waspmote;

import hr.fer.zari.waspmote.db.dao.SensorMeasurementDataSource;
import hr.fer.zari.waspmote.db.dao.SensorsDataSource;
import hr.fer.zari.waspmote.models.SensorMeasurement;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
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
import com.androidplot.xy.XYStepMode;

public class PlotActivity extends Activity {

	private XYPlot plot;
	private List<Date> dates;
	private Number[] series1;
	private Number[] series2;
	
	private int sensorId;
	private String dataType;
	private boolean internal;
	private List<SensorMeasurement> measurements;
	
	private WaspmoteApplication waspApp;
	private SensorsDataSource sensorsDs;
	private SensorMeasurementDataSource sensorMeasurements;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plot);
		
		waspApp = (WaspmoteApplication) getApplication();
		sensorsDs = (SensorsDataSource) waspApp.getWaspmoteSqlHelper().getSensorsDataSource(this);
		sensorMeasurements = (SensorMeasurementDataSource) waspApp
				.getWaspmoteSqlHelper().getSensorMeasurementDataSource(this);
		
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			//Toast.makeText(this, "Unable to comply, exiting.", Toast.LENGTH_SHORT).show();
		} else {
			sensorId = extras.getInt("sensorId", 0);
			measurements = sensorMeasurements.getAllSensorMeasurementBySensorId(sensorId);
			if (null == extras.getString("dataType", null)) {
				internal = true;
			} else {
				dataType = extras.getString("dataType", null);
			}
		}
		
		series1 = new Number[measurements.size()];
		series2 = new Number[measurements.size()];
		
		for (int i=0; i<measurements.size(); i++) {
			series1[i] = measurements.get(i).getTimestamp();
			if (internal) {
				series2[i] = Float.parseFloat(measurements.get(i).getValue());
			} else {
				series2[i] = parseDataType(measurements.get(i).getValue());
			}
		}
		
		plot = (XYPlot) findViewById(R.id.plot_sensor_data);
		
		//dates = generateDates();
		
		// Number[] dateSeries = dates;
		
		// Create series and appropriate formatters
		//XYSeries series1 = new SimpleXYSeries(
			//	Arrays.asList(series1Numbers),
				//SimpleXYSeries.ArrayFormat.Y_VALS_ONLY,
				//"Vrijednosti");
		
		LineAndPointFormatter series1Format = new LineAndPointFormatter();
        series1Format.setPointLabelFormatter(new PointLabelFormatter());
        series1Format.configure(getApplicationContext(),
            R.xml.line_point_formatter_with_plf1);
        
        
		XYSeries series22 = new SimpleXYSeries(
				Arrays.asList(series1),
				Arrays.asList(series2),
				sensorsDs.getSensorById(sensorId).getSensorName());
		
        LineAndPointFormatter series2Format = new LineAndPointFormatter();
        series2Format.setPointLabelFormatter(new PointLabelFormatter());
        series2Format.configure(getApplicationContext(),
                R.xml.line_point_formatter_with_plf3);
        
        plot.addSeries(series22, series2Format);
        plot.setDomainLabel("Vrijeme");
        plot.setRangeLabel("Vrijednosti");
        
        // reduce the number of range labels
        plot.setTicksPerRangeLabel(3);
        plot.setTicksPerDomainLabel(3);
        
        //plot.setRangeStep(XYStepMode.SUBDIVIDE, series1.length);
        //plot.setDomainStep(XYStepMode.SUBDIVIDE, series2.length);
        
        plot.setDomainValueFormat(new Format() {
        	 
            // create a simple date format that draws on the year portion of our timestamp
            // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
            // for a full description of SimpleDateFormat.
            private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
 
            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
                long timestamp = ((Number) obj).longValue();
                Date date = new Date(timestamp);
                return dateFormat.format(date, toAppendTo, pos);
            }
 
            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;
            }
        });
        
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

	private Double parseDataType(String value) {
		String separator = "!"+dataType+"!";
		value = value.substring(value.indexOf(separator));
		value = value.substring(separator.length());
		value = value.substring(0, value.indexOf(separator));
		return Double.parseDouble(value);
	}

}
