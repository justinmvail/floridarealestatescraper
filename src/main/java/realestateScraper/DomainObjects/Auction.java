package realestateScraper.DomainObjects;

import realestateScraper.Constants.AuctionType;
import realestateScraper.Constants.County;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class Auction {
    private final AuctionType auctionType;
    private final County county;
    private final LocalDate date;
    private final LocalTime time;
    private final String strUrl;
    private List<AuctionListing> auctionListings;

    public Auction(AuctionType auctionType, County county, LocalDate date, LocalTime time, String strUrl) {
        this.auctionType = auctionType;
        this.county = county;
        this.date = date;
        this.time = time;
        this.strUrl = strUrl;
    }

    public Auction(AuctionType auctionType, County county, LocalDate date, LocalTime time, String strUrl, List<AuctionListing> auctionListings) {
        this.auctionType = auctionType;
        this.county = county;
        this.date = date;
        this.time = time;
        this.strUrl = strUrl;
        this.auctionListings = auctionListings;
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

    public String getStrUrl() {
        return strUrl;
    }

    public List<AuctionListing> getAuctionListings() {
        return auctionListings;
    }

    public void setAuctionListings(List<AuctionListing> auctionListings) {
        this.auctionListings = auctionListings;
    }

}
