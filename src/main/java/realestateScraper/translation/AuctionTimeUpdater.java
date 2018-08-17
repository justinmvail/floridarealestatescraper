package realestateScraper.translation;

import realestateScraper.DomainObjects.Auction;
import realestateScraper.Constants.TimeZone;

import java.util.List;

public class AuctionTimeUpdater {

    public static void updateAuctionTimesForTimeZone(List<Auction> auctions, TimeZone timeZone){
        for (Auction auction : auctions){
            SimpleTimeZoneRectifier.rectifyTimeZone(auction.getTime(), auction.getCounty(), timeZone);
        }
    }

}
