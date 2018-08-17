package realestateScraper;

import realestateScraper.DomainObjects.Auction;
import realestateScraper.DomainObjects.AuctionListing;
import realestateScraper.Constants.AuctionType;
import realestateScraper.Constants.County;
import realestateScraper.DomainObjects.MlsListing;
import realestateScraper.services.MlsService;
import realestateScraper.services.RealTaxDeedScraper;
import realestateScraper.services.TaxAuctionService;
import realestateScraper.services.ZillowScraper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class ListingRunner {
    public static void main( String[] args ) throws IOException, InterruptedException {
        long startTime = System.nanoTime();
        TaxAuctionService taxAuctionService = new RealTaxDeedScraper(false);
        Auction auction = taxAuctionService.getAuctionByDate(AuctionType.TAXDEED, County.BAY, LocalDate.parse("2018-08-28"));
        List<AuctionListing> auctionListings = taxAuctionService.getAuctionListings(auction);
        auction.setAuctionListings(auctionListings);

        MlsService mlsService = new ZillowScraper(false);
        for(AuctionListing auctionListing : auction.getAuctionListings()){
            MlsListing mlsListing = mlsService.getMlsListingForAuctionListing(auctionListing);
            auctionListing.setMlsListing(mlsListing);
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000000;
        System.out.println("Total duration: "+ duration);
        System.out.println("end");
    }
}
