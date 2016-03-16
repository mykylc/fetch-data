package com.fetch.data.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesUtils {
	protected static final Logger log = LoggerFactory.getLogger(PropertiesUtils.class);
	private static Properties properties = null;
	private static String fileConfig = "important.properties";
	
	private PropertiesUtils() {}
	public static Properties getProperties(){
		if (properties == null) {
			File file = new File(System.getProperty("user.dir") + fileConfig);  
			log.info(file.getAbsolutePath());
	        InputStream is = null;	
	        properties = new Properties();  
	        if (file.exists() && !file.isDirectory()) {  
	            try {  
	                is = new FileInputStream(file);  
	                properties.load(is);  
	            }catch (IOException ioE){  
	            	log.error(String.format("[PropertiesUtils.getProperties] 加载配置文件出错：%s", ioE.getMessage()), ioE);  
	            }finally{  
	                try {
						is.close();
					} catch (IOException e) {
						log.error(String.format("[PropertiesUtils.getProperties] 加载配置文件关闭流出错：%s", e.getMessage()), e);
					}  
	            }  
	        }
	    }
		return properties;
	}
	
    
    public static String getProperty(String key){
    	getProperties();
    	return (String) properties.get(key);
    }
    
    public static String getProperty(String key,String defaultValue){
		String value = getProperty(key);
		if(StringUtils.isEmpty(value)) {
			value = defaultValue;
		}
		return value;
	}
}
