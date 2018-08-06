package realestateScraper.translation;

import realestateScraper.DomainObjects.Auction;
import realestateScraper.DomainObjects.TimeZone;

import java.util.List;

public class AuctionTimeUpdater {

    public static void updateAuctionTimesByTimeZone(List<Auction> auctions, TimeZone timeZone){
        for (Auction auction : auctions){
            SimpleTimeZoneRectifier.rectifyTimeZone(auction.getTime(), auction.getCounty(), timeZone);
        }
    }

}
