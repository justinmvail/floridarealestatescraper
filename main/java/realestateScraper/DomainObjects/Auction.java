package realestateScraper.DomainObjects;

import java.time.LocalDate;
import java.time.LocalTime;

public class Auction {
    private final AuctionType auctionType;
    private final County county;
    private final LocalDate date;
    private final LocalTime time;
    private final String strUrl;

    public Auction(AuctionType auctionType, County county, LocalDate date, LocalTime time, String strUrl) {
        this.auctionType = auctionType;
        this.county = county;
        this.date = date;
        this.time = time;
        this.strUrl = strUrl;
    }

    public AuctionType getAuctionType() {
        return auctionType;
    }

    public County getCounty() {
        return county;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getUrl() {
        return strUrl;
    }
}
