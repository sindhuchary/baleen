//Dstl (c) Crown Copyright 2015
package uk.gov.dstl.baleen.uima.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;

import uk.gov.dstl.baleen.exceptions.InvalidParameterException;

/**
 * Convert a string into an object. Used by LegacyMongoConsumer - considered legacy code!
 *
 * 
 */
public class StringToObject {

	private static final String CONFIG_ALLOW_DATES = "allowDates";
	private static final String CONFIG_PRECEDING_ZERO_ISNT_NUMBER = "precedingZeroIsntNumber";
	private static final String CONFIG_PRECEDING_PLUS_ISNT_NUMBER = "precedingPlusIsntNumber";

	private StringToObject() {
		// Singleton
	}

	/**
	 * Convert a string to a Java object of the correct type with the same value
	 * (e.g. "1" -> 1).
	 * <p>
	 * If a number has a preceding 0, it will be assumed not to be a number as
	 * it is likely to represent a phone number
	 * <p>
	 * This method accepts no configuration, and so uses default values
	 *
	 * @param s
	 *            String to convert
	 * @return A Java object of the correct type
	 */
	public static Object convertStringToObject(String s) {
		return convertStringToObject(s, new Properties());
	}

	/**
	 * Convert a string to a Java object of the correct type with the same value
	 * (e.g. "1" -> 1).
	 * <p>
	 * If a number has a preceding 0, it will be assumed not to be a number as
	 * it is likely to represent a phone number
	 * <p>
	 * <p>
	 * If a number has a preceding + then it could represent a phone number rather
	 * than a numeric value. The default mode of operation is to convert such a string
	 * into an appropriate numeric object. However, the configuration key
	 * precedingPlusIsntNumber allows this to be overridden and produce a String object.
	 * <p>
	 * The following configuration keys can be set:
	 * <ul>
	 * <li><b>allowDates</b> - true (default) or false
	 * <li><b>precedingZeroIsntNumber</b> - true (default) or false
	 * <li><b>precedingPlusIsntNumber</b> - true or false (default)
	 * </ul>
	 *
	 * @param s
	 *            String to convert
	 * @param config
	 *            Configuration values
	 * @return A Java object of the correct type
	 */
	public static Object convertStringToObject(String s, Properties config) {
		Boolean precedingZeroIsntNumber = getConfigPrecedingZero(config);
		Boolean precedingPlusIsntNumber = getConfigPrecedingPlus(config);
		
		if (s == null) {
			return null;
		}else if(tryNumber(s, precedingZeroIsntNumber, precedingPlusIsntNumber)){
			try {
				return parseNumber(s);
			} catch (InvalidParameterException e) {
				// Ignore
			}
		}

		if ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)) {
			return Boolean.parseBoolean(s);
		}

		return convertToDate(s, config);
	}
	
	private static boolean getConfigPrecedingZero(Properties config){
		Boolean precedingZeroIsntNumber = true;
		
		if(config.containsKey(CONFIG_PRECEDING_ZERO_ISNT_NUMBER)){
			Object o = config.get(CONFIG_PRECEDING_ZERO_ISNT_NUMBER);
			if(o instanceof Boolean){
				precedingZeroIsntNumber = (Boolean) o;
			}else{
				precedingZeroIsntNumber = Boolean.valueOf(o.toString());
			}
		}
		
		return precedingZeroIsntNumber;
	}
	
	private static boolean getConfigPrecedingPlus(Properties config){
		Boolean precedingPlusIsntNumber = true;
		
		if(config.containsKey(CONFIG_PRECEDING_PLUS_ISNT_NUMBER)){
			Object o = config.get(CONFIG_PRECEDING_PLUS_ISNT_NUMBER);
			if(o instanceof Boolean){
				precedingPlusIsntNumber = (Boolean) o;
			}else{
				precedingPlusIsntNumber = Boolean.valueOf(o.toString());
			}
		}
		
		return precedingPlusIsntNumber;
	}

	private static Number parseNumber(String s) throws InvalidParameterException{
		try {
			return Integer.parseInt(s);
		} catch (NumberFormatException e) {
			// Ignore
		}

		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			// Ignore
		}
		
		throw new InvalidParameterException("Couldn't parse number");
	}
	
	/**
	 * Test whether we should attempt to covert a string to a number, based on the current configuration
	 */
	private static boolean tryNumber(String s, boolean precedingZeroIsntNumber, boolean precedingPlusIsntNumber){
		if (s.startsWith("0.")) {
			return true;
		} else if (precedingPlusIsntNumber && s.startsWith("+")) {
			return false;
		} else if (precedingZeroIsntNumber && s.startsWith("0")) {
			return false;
		}
		
		return true;
	}

	private static Object convertToDate(String s, Properties config) {
		Object allowDates = config.get(CONFIG_ALLOW_DATES);
		if (allowDates == null || "true".equalsIgnoreCase(allowDates.toString())) {
			try {
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
				df.setTimeZone(TimeZone.getTimeZone("UTC"));
				return df.parse(s);
			} catch (ParseException e) {
				// Ignore
			}

			try {
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				return df.parse(s);
			} catch (ParseException e) {
				// Ignore
			}

			try {
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				df.setTimeZone(TimeZone.getTimeZone("UTC"));
				return df.parse(s);
			} catch (ParseException e) {
				// Ignore
			}
		}

		return s;
	}
}
