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
    private static TaxAuctionService taxAuctionService = new RealTaxDeedScraper(false);
    private static FileExporter fileExporter = new GoogleCalendarCSVExporter();

    public static void main( String[] args ) throws IOException, InterruptedException {
        long startTime = System.nanoTime();
        String exportFileLocation = args[0];
        String strDate = args[1];
        County[] allCounties = County.class.getEnumConstants();
        List<Auction> allAuctions = getAllAuctions(allCounties,  LocalDate.parse(strDate), taxAuctionService);
        populateAllAuctionListings(allAuctions, taxAuctionService);
        AuctionTimeUpdater.updateAuctionTimesForTimeZone(allAuctions, TimeZone.ET);
        fileExporter.export(exportFileLocation, allAuctions);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000000;
        System.out.println("Total duration: "+ duration);
        System.out.println("Good Bye.");
    }

    private static List<Auction> getAllAuctions(County[] allCounties, LocalDate date, TaxAuctionService taxAuctionService) throws IOException, InterruptedException {
        List<Auction> allAuctions = new ArrayList<>();
        for(County county : allCounties){
            System.out.print("Getting Auctions for "+county.getCountyName()+"...   ");
            List<Auction> countyAuctionList = taxAuctionService.getAllAuctionDatesByMonth(AuctionType.TAXDEED, county, date);
            allAuctions.addAll(countyAuctionList);
            System.out.println("complete." );
        }
        return allAuctions;
    }

    private static void populateAllAuctionListings(List<Auction> allAuctions, TaxAuctionService taxAuctionService) throws IOException {
        for(Auction auction : allAuctions){
            System.out.print("Getting listings for "+auction.getCounty().getCountyName()+" auction on "+auction.getDate()+"...   ");
            List<AuctionListing> auctionListings = taxAuctionService.getAuctionListings(auction);
            auction.setAuctionListings(auctionListings);
            System.out.println("complete");
        }
    }
}
