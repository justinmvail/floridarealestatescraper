package realestateScraper.Constants;

public enum County {
    BAY("Bay County", "http://bay.realtaxdeed.com", TimeZone.CT),
    BREVARD("Brevard County", "http://brevard.realforeclose.com", TimeZone.ET),
    CHARLOTTE("Charlotte County", "http://charlotte.realforeclose.com", TimeZone.ET),
    CITRUS("Citrus County", "http://citrus.realtaxdeed.com", TimeZone.ET),
    DUVAL("Duval County", "http://duval.realtaxdeed.com", TimeZone.ET),
    ESCAMBIA("Escambia County", "http://escambia.realtaxdeed.com", TimeZone.ET),
    FLAGER("Flagler County", "http://flagler.realtaxdeed.com", TimeZone.ET),
    GILCHRIST("Gilchrist County", "http://gilchrist.realtaxdeed.com", TimeZone.ET),
    HERNANDO("Hernando County", "http://hernando.realtaxdeed.com", TimeZone.ET),
    HILLSBOROUGH("Hillsborough County", "http://hillsborough.realtaxdeed.com", TimeZone.ET),
    INDIAN("Indian River County", "http://indian-river.realtaxdeed.com", TimeZone.ET),
    LAKE("Lake County", "http://lake.realtaxdeed.com", TimeZone.ET),
    LEE("Lee County", "http://lee.realtaxdeed.com", TimeZone.ET),
    LEON("Leon County", "http://leon.realtaxdeed.com", TimeZone.ET),
    MANATEE("Manatee County","http://manatee.realforeclose.com/",TimeZone.ET),
    MARTIN("Martin County", "http://martin.realtaxdeed.com", TimeZone.ET),
    MIAMI_DADE("Miami-Dade County", "http://miami-dade.realtaxdeed.com", TimeZone.ET),
    OKALOOSA("Okaloosa County", "http://okaloosa.realtaxdeed.com", TimeZone.CT),
    OSCEOLA("Osceola County", "http://osceola.realtaxdeed.com", TimeZone.ET),
    ORANGE("Orange County", "http://orange.realtaxdeed.com", TimeZone.ET),
    PINELLAS("Pinellas County", "http://pinellas.realtaxdeed.com", TimeZone.ET),
    POLK("Polk County", "http://polk.realtaxdeed.com", TimeZone.ET),
    PUTNAM("Putnam County", "http://putnam.realtaxdeed.com", TimeZone.ET),
    SANTA_ROSA("Santa Rosa County", "http://santarosa.realtaxdeed.com", TimeZone.CT),
    VOLUSIA("Volusia County", "http://volusia.realtaxdeed.com", TimeZone.ET),
    WALTON("Walton County", "http://walton.realforeclose.com", TimeZone.CT);

    private final String countyName;
    private final String url;
    private final TimeZone timeZone;

    County(String county, String url, TimeZone timeZone){
        this.countyName = county;
        this.url = url;
        this.timeZone = timeZone;
    }

    public String getCountyName() {
        return countyName;
    }

    public String getUrl() {
        return url;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }
}
