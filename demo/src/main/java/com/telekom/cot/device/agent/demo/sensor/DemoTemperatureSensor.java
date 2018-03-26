package com.telekom.cot.device.agent.demo.sensor;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.SensorDeviceServiceException;
import com.telekom.cot.device.agent.sensor.SensorMeasurement;
import com.telekom.cot.device.agent.sensor.configuration.SensorConfiguration;
import com.telekom.cot.device.agent.sensor.deviceservices.TemperatureSensor;

public class DemoTemperatureSensor extends TemperatureSensor {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DemoTemperatureSensor.class);

    private DemoTemperatureSensorConfiguration configuration;
    private TemperatureFileReader temperatureFileReader = new TemperatureFileReader();

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() throws AbstractAgentException {
    	// get configuration and create TemperatureFileReader
    	configuration = getConfigurationManager().getConfiguration(DemoTemperatureSensorConfiguration.class);
    	temperatureFileReader.setRepeatMeasurements(configuration.isRepeatMeasurements());
    	
    	// read temperature measurements from file
        if (!temperatureFileReader.readFile(configuration.getValueFilePath())) {
            LOGGER.error("Can't read temperature values from file '{}'", configuration.getValueFilePath());
            throw new SensorDeviceServiceException("Can't read temperature values from file '" + configuration.getValueFilePath() + "'");
        }

        // call TemperatureSensor.start()
        super.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected SensorMeasurement getTemperatureMeasurement() {
        Float value = temperatureFileReader.getTemperatureMeasurement();
        if (Objects.isNull(value)) {
            return null;
        }

        return new SensorMeasurement(TEMPERATURE_MEASUREMENT_TYPE, value, "�C");
    }

    /**
     * {@inheritDoc}
     */
	@Override
	protected SensorConfiguration getSensorConfiguration() throws AbstractAgentException {
		return configuration;
	}
}