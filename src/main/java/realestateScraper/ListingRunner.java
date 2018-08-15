package realestateScraper;

import realestateScraper.DomainObjects.Auction;
import realestateScraper.DomainObjects.AuctionListing;
import realestateScraper.DomainObjects.AuctionType;
import realestateScraper.DomainObjects.County;
import realestateScraper.services.RealTaxDeedScraper;
import realestateScraper.services.TaxAuctionService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ListingRunner {
    public static void main( String[] args ) throws IOException, InterruptedException {
        TaxAuctionService taxAuctionService = new RealTaxDeedScraper(false);
        Auction auction = taxAuctionService.getAuctionByDate(AuctionType.TAXDEED, County.BAY, LocalDate.parse("2018-08-07"));
        List<AuctionListing> auctionListings = taxAuctionService.getAuctionListings(auction);
        auction.setAuctionListings(auctionListings);
        System.out.println("end");
    }
}
