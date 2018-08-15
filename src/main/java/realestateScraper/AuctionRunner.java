package realestateScraper;

import realestateScraper.DomainObjects.*;
import realestateScraper.export.FileExporter;
import realestateScraper.export.GoogleCalendarCSVExporter;
import realestateScraper.services.RealTaxDeedScraper;
import realestateScraper.services.TaxAuctionService;
import realestateScraper.translation.AuctionTimeUpdater;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AuctionRunner {
    public static void main( String[] args ) throws IOException, InterruptedException {
        String exportFileLocation = args[0];
        List<Auction> allAuctions = new ArrayList<>();
        TaxAuctionService taxAuctionService = new RealTaxDeedScraper(false);
        County[] allCounties = County.class.getEnumConstants();
        for(County county : allCounties){
            System.out.print("Getting Auctions for "+county.getCountyName()+"...   ");
            List<Auction> auctionList = taxAuctionService.getAllAuctionDatesByMonth(AuctionType.TAXDEED, county, LocalDate.parse("2018-08-01"));
            allAuctions.addAll(auctionList);
            System.out.println("complete." );
        }

        for(Auction auction : allAuctions){
            System.out.print("Getting listings for "+auction.getCounty().getCountyName()+" auction on "+auction.getDate()+"...   ");
            List<AuctionListing> auctionListings = taxAuctionService.getAuctionListings(auction);
            auction.setAuctionListings(auctionListings);
            System.out.println("complete");
        }

        //Update TimeZones and save.
        AuctionTimeUpdater.updateAuctionTimesByTimeZone(allAuctions, TimeZone.ET);
        FileExporter fileExporter = new GoogleCalendarCSVExporter();
        fileExporter.export(exportFileLocation, allAuctions);
    }
}
