package com.telekom.cot.device.agent.system;

import static com.telekom.cot.device.agent.common.util.AssertionUtil.assertNotNull;
import static com.telekom.cot.device.agent.common.util.AssertionUtil.createExceptionAndLog;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.telekom.cot.device.agent.common.exc.AbstractAgentException;
import com.telekom.cot.device.agent.common.exc.PropertyNotFoundException;
import com.telekom.cot.device.agent.service.AbstractAgentService;
import com.telekom.cot.device.agent.service.configuration.MobilePropertiesConfiguration;
import com.telekom.cot.device.agent.system.properties.ConfigurationProperties;
import com.telekom.cot.device.agent.system.properties.MobileProperties;
import com.telekom.cot.device.agent.system.properties.Properties;
import com.telekom.cot.device.agent.system.properties.SoftwareProperties;

public abstract class AbstractSystemService extends AbstractAgentService implements SystemService {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSystemService.class);

	private HashMap<Class<? extends Properties>, Properties> properties = new HashMap<>();

	@Override
	public void start() throws AbstractAgentException {
		// get and set configuration properties, software properties and mobile
		// properties
		setProperties(ConfigurationProperties.class, getConfigurationProperties());
		setProperties(SoftwareProperties.class, getSoftwareProperties());
		setProperties(MobileProperties.class, getMobileProperties());

		super.start();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T extends Properties> T getProperties(Class<T> propertyType) throws AbstractAgentException {
		assertNotNull(propertyType, PropertyNotFoundException.class, LOGGER, "no property type given");

		LOGGER.debug("get properties of type {}", propertyType);
		if (!properties.containsKey(propertyType)) {
			throw createExceptionAndLog(PropertyNotFoundException.class, LOGGER,
					"can't get properties of type '" + propertyType.getName() + "'");
		}

		LOGGER.info("got properties of type '{}'", propertyType.getName());
		return propertyType.cast(properties.get(propertyType));
	}

	/**
	 * Associates the specified configurationProperties with the specified key
	 * propertyType in this map. If the map previously contained a mapping for the
	 * key propertyType, the old value is replaced.
	 * 
	 * @param propertyType
	 * @param configurationProperties
	 */
	protected void setProperties(Class<? extends Properties> propertyType, Properties configurationProperties) {
		properties.put(propertyType, configurationProperties);
	}

	/**
	 * Gets the configuration properties
	 * 
	 * @return the configuration properties
	 */
	private ConfigurationProperties getConfigurationProperties() {
		try {
			return new ConfigurationProperties(getConfigurationManager().getConfiguration());
		} catch (Exception e) {
			LOGGER.info("can't get configuration content, create new empty configuration properties", e);
			return new ConfigurationProperties();
		}
	}

	/**
	 * Gets all software properties.
	 * 
	 * @return The software properties
	 * @throws AbstractAgentException
	 */
	private SoftwareProperties getSoftwareProperties() throws AbstractAgentException {
		SoftwareProperties softwareProperties = new SoftwareProperties();

		// Read the specification title and version from the package containing the
		// SystemService
		SystemService service = getService(SystemService.class);
		Package objPackage = service.getClass().getPackage();
		String name = objPackage.getSpecificationTitle();
		String version = objPackage.getSpecificationVersion();
		softwareProperties.addSoftware(name, version, null);

		return softwareProperties;
	}

	/**
	 * gets the mobile properties from configuration file
	 */
	private MobileProperties getMobileProperties() throws AbstractAgentException {
		try {
			return getConfigurationManager().getConfiguration(MobilePropertiesConfiguration.class);
		} catch (Exception e) {
			LOGGER.info("can't get mobile properties from configuration, create new empty mobile properties", e);
			return new MobileProperties();
		}
	}
}
