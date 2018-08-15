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
        List<Auction> auctionList = taxAuctionService.getAllAuctionDatesByMonth(AuctionType.TAXDEED, County.MIAMI_DADE, LocalDate.parse("2018-08-01"));
        Auction auction = auctionList.get(0);
        List<AuctionListing> auctionListings = taxAuctionService.getAuctionListings(auction);
        auction.setAuctionListings(auctionListings);
        System.out.println("end");
    }
}
