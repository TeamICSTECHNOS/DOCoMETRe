package fr.univamu.ism.docometre.dacqsystems.arduinouno;

import fr.univamu.ism.docometre.dacqsystems.Module;
import fr.univamu.ism.docometre.dacqsystems.Property;

public final class ADS1115ModuleProperties extends Property {
	
	public static final ADS1115ModuleProperties ADDRESS = new ADS1115ModuleProperties("ADS1115ModuleProperties.ADDRESS", ArduinoUnoMessages.address_label, ArduinoUnoMessages.address_tooltip, "\"^(0x48|0x49|0x4A|0x4B)$\"", "0x48:0x49:0x4A:0x4B");
	
	public static String ADDRESS_0X48 = "0x48";
	public static String ADDRESS_0X49 = "0x49";
	public static String ADDRESS_0X4A = "0x4A";
	public static String ADDRESS_0X4B = "0x4B";

	public static void populateProperties(Module module){
		module.setProperty(ADDRESS, ADDRESS_0X48); 
	}
	
	public static Module cloneModule(Module module) {
		ADS1115Module newModule = new ADS1115Module(null);
		newModule.setProperty(ADDRESS, new String(module.getProperty(ADDRESS)));
		return newModule;
	}
	
	public ADS1115ModuleProperties(String key, String label, String tooltip, String regExp) {
		super(key, label, tooltip, regExp);
	}

	public ADS1115ModuleProperties(String key, String label, String tooltip, String regExp, String availableValues) {
		super(key, label, tooltip, regExp, availableValues);
	}
	
}
