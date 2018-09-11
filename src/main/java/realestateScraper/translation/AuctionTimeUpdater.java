package realestateScraper.translation;

import realestateScraper.objects.Auction;
import realestateScraper.constants.TimeZone;

import java.util.List;

public class AuctionTimeUpdater {

    public static void updateAuctionTimesForTimeZone(List<Auction> auctions, TimeZone timeZone){
        for (Auction auction : auctions){
            auction.setTime(SimpleTimeZoneRectifier.rectifyTimeZone(auction.getTime(), auction.getCounty(), timeZone));
        }
    }

}
