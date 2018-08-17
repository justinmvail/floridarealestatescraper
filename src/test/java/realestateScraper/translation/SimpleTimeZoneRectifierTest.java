package realestateScraper.translation;

import org.junit.Assert;
import org.junit.Test;
import realestateScraper.Constants.County;
import realestateScraper.Constants.TimeZone;

import java.time.LocalTime;

public class SimpleTimeZoneRectifierTest {

    @Test
    public void rectifyTimeZoneFromCentralToEastern() {
        LocalTime centralTime = LocalTime.now();
        LocalTime easternTime = SimpleTimeZoneRectifier.rectifyTimeZone(centralTime, County.BAY, TimeZone.ET);
        Assert.assertEquals(centralTime, easternTime.minusHours(1));
    }

    @Test
    public void rectifyTimeZoneFromEasternToCentral() {
        LocalTime easternTime = LocalTime.now();
        LocalTime centralTime = SimpleTimeZoneRectifier.rectifyTimeZone(easternTime, County.BREVARD, TimeZone.CT);
        Assert.assertEquals(easternTime, centralTime.minusHours(1));
    }

    @Test
    public void rectifyTimeZoneFromCentralToCentral() {
        LocalTime centralTime = LocalTime.now();
        LocalTime rectifiedTime = SimpleTimeZoneRectifier.rectifyTimeZone(centralTime, County.BAY, TimeZone.CT);
        Assert.assertEquals(centralTime, rectifiedTime);
    }

    @Test
    public void rectifyTimeZoneFromEasterToEastern() {
        LocalTime easternTime = LocalTime.now();
        LocalTime rectifiedTime = SimpleTimeZoneRectifier.rectifyTimeZone(easternTime, County.BREVARD, TimeZone.ET);
        Assert.assertEquals(easternTime, rectifiedTime);
    }


}