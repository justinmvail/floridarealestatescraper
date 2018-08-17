package realestateScraper.translation;

import realestateScraper.Constants.County;
import realestateScraper.Constants.TimeZone;

import java.time.LocalTime;

class SimpleTimeZoneRectifier {

    public static LocalTime rectifyTimeZone(LocalTime time, County county, TimeZone desirdedTimeZone){
        byte originalTimeZoneValue = (byte)county.getTimeZone().getValue();
        byte desiredTimeZoneValue = (byte)desirdedTimeZone.getValue();
        if(originalTimeZoneValue >= desiredTimeZoneValue){
            return time.plusHours(originalTimeZoneValue-desiredTimeZoneValue);
        }else{
            return time.minusHours(originalTimeZoneValue-desiredTimeZoneValue);
        }
    }
}
