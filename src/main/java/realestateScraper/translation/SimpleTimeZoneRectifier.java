package realestateScraper.translation;

import realestateScraper.constants.County;
import realestateScraper.constants.TimeZone;

import java.time.LocalTime;

class SimpleTimeZoneRectifier {

    static LocalTime rectifyTimeZone(LocalTime time, County county, TimeZone desirdedTimeZone){
        byte originalTimeZoneValue = (byte)county.getTimeZone().getValue();
        byte desiredTimeZoneValue = (byte)desirdedTimeZone.getValue();
        if(originalTimeZoneValue >= desiredTimeZoneValue){
            return time.plusHours(originalTimeZoneValue-desiredTimeZoneValue);
        }else{
            return time.minusHours(originalTimeZoneValue-desiredTimeZoneValue);
        }
    }
}
