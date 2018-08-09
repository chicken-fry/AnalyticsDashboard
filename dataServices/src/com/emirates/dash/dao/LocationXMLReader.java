package com.emirates.dash.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.apache.log4j.Level;

import com.emirates.dash.dto.MasterResponseDTO;
import com.emirates.dash.location.vo.List;
import com.emirates.dash.utilities.DashLoggerUtil;
import com.emirates.dash.vo.LatLongVO;


public class LocationXMLReader {

	
	public static Map<String, JAXBContext> jaxbContestMap = new HashMap<String, JAXBContext>();

	public static com.emirates.dash.location.vo.List loadLocationXml() {
	    String filePath = "../ibedashboard_node1/config/dashProps/locations.xml";
//		String filePath = "../standalone/config/dashProps/locations.xml";
		DashLoggerUtil.log(Level.DEBUG,"LocationXMLReader init started");
		com.emirates.dash.location.vo.List locationList = null;
		InputStream is = null;
		try {
				// sample en_GB
			DashLoggerUtil.log(Level.DEBUG,"LocationXMLReader file_path : " + filePath);
				is = new FileInputStream(new File(filePath));
				locationList = (com.emirates.dash.location.vo.List) jaxbParse(
						is, "com.emirates.dash.location.vo");
		} catch (FileNotFoundException e) {
			DashLoggerUtil.log(Level.ERROR,"LocationXMLReader FileNotFoundException ", e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					DashLoggerUtil.log(Level.ERROR,"error", e);
				}
			}
		}
		DashLoggerUtil.log(Level.DEBUG,"LocationXMLReader init completed");
		DashLoggerUtil.log(Level.DEBUG,"LocationXMLReader locationList  is null : "+(locationList == null));
		 
		return locationList;
	}

	public static Object jaxbParse(InputStream is, String packageName) {
		Object xmlBean = null;
		try {
			JAXBContext jaxbContext = getJAXBContext(packageName);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			xmlBean = unmarshaller.unmarshal(is);
		} catch (JAXBException e) {
			DashLoggerUtil.log(Level.ERROR,"error", e);
		}
		return xmlBean;
	}

	public static JAXBContext getJAXBContext(String packageName)
			throws JAXBException {
		if (jaxbContestMap.get(packageName) == null) {
			jaxbContestMap.put(packageName, JAXBContext
					.newInstance(packageName));
		}
		return jaxbContestMap.get(packageName);
	}

	
	private static MasterResponseDTO abridgeLocationsXML(com.emirates.dash.location.vo.List locationsFull){
        MasterResponseDTO mrto = new MasterResponseDTO();
        HashMap<String, LatLongVO> EKLocations = new HashMap<String, LatLongVO>();
        HashMap<String, LatLongVO> countries = new HashMap<String, LatLongVO>();
        LatLongVO latLongVO = null;
        com.emirates.dash.location.vo.List.Region.Country.City currentCityReal = null;
        com.emirates.dash.location.vo.List.Region.Country.City.Airport currentAirportReal = null;
        for(Iterator iterator = locationsFull.getRegion().iterator(); iterator.hasNext(); )
        {
        	com.emirates.dash.location.vo.List.Region currentRegion = (com.emirates.dash.location.vo.List.Region)iterator.next();
            for(Iterator iterator1 = currentRegion.getCountry().iterator(); iterator1.hasNext();)
            {
            	com.emirates.dash.location.vo.List.Region.Country currentCountry = (com.emirates.dash.location.vo.List.Region.Country)iterator1.next();
                latLongVO = new LatLongVO();
                latLongVO.setCountryCode(currentCountry.getCode());
                latLongVO.setCountry(currentCountry.getName());
                latLongVO.setLatitudeCountry(currentCountry.getLatitude().toString());
                latLongVO.setLongitudeCountry(currentCountry.getLongitude().toString());
                countries.put(latLongVO.getCountryCode(),latLongVO);
                
                for(Iterator iterator2 = currentCountry.getCity().iterator(); iterator2.hasNext();)
                {
                    Object currentCity = iterator2.next();
                    currentCityReal = (com.emirates.dash.location.vo.List.Region.Country.City)currentCity;
                    latLongVO = new LatLongVO();
                    latLongVO.setCountryCode(currentCountry.getCode());
                    latLongVO.setCountry(currentCountry.getName());
                    latLongVO.setLatitudeCountry(currentCountry.getLatitude().toString());
                    latLongVO.setLongitudeCountry(currentCountry.getLongitude().toString());
                    latLongVO.setCityCode(currentCityReal.getCode());
                    latLongVO.setCity(currentCityReal.getName());
                    latLongVO.setLatitudeCity(currentCityReal.getLatitude().toString());
                    latLongVO.setLongitudeCity(currentCityReal.getLongitude().toString());
                    EKLocations.put(currentCityReal.getCode(), latLongVO);
                    

                    for(Iterator iterator3 = currentCityReal.getAirport().iterator(); iterator3.hasNext();)
                    {
                        Object currentAirport = iterator3.next();
                        currentAirportReal = (com.emirates.dash.location.vo.List.Region.Country.City.Airport)currentAirport;
                        latLongVO = new LatLongVO();
                        latLongVO.setCountryCode(currentCountry.getCode());
                        latLongVO.setCountry(currentCountry.getName());
                        latLongVO.setLatitudeCountry(currentCountry.getLatitude().toString());
                        latLongVO.setLongitudeCountry(currentCountry.getLongitude().toString());
                        latLongVO.setCityCode(currentCityReal.getCode());
                        latLongVO.setCity(currentCityReal.getName());
                        latLongVO.setLatitudeCity(currentCityReal.getLatitude().toString());
                        latLongVO.setLongitudeCity(currentCityReal.getLongitude().toString());
                        latLongVO.setAirportCode(currentAirportReal.getCode());
                        latLongVO.setLatitudeAirport(currentAirportReal.getLatitude().toString());
                        latLongVO.setLongitudeAirport(currentAirportReal.getLongitude().toString());

                        EKLocations.put(latLongVO.getAirportCode(), latLongVO);
                        
                    }

                }

            }

        }

        mrto.setEKLocations(EKLocations);
        mrto.setCountries(countries);
        return mrto;
    }

	public static MasterResponseDTO retrieveLocationsJSON()
    {
        MasterResponseDTO masterResponseDTO = null;
        List locationsFull = loadLocationXml();
        masterResponseDTO = abridgeLocationsXML(locationsFull);
        
        return masterResponseDTO;
    }
	
}
