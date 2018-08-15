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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionRunner {
    private static TaxAuctionService taxAuctionService = new RealTaxDeedScraper(false);
    private static FileExporter emailfileExporter = new GoogleCalendarCSVExporter();

    public static void main( String[] args ) throws IOException, InterruptedException {
        long startTime = System.nanoTime();
        String googleCalendarExportFileLocation = args[0];
        String strDate = args[1];
        County[] allCounties = County.class.getEnumConstants();
        List<Auction> allAuctions = getAllAuctions(allCounties,  LocalDate.parse(strDate), taxAuctionService);
        populateAllAuctionListings(allAuctions, taxAuctionService);
        AuctionTimeUpdater.updateAuctionTimesForTimeZone(allAuctions, TimeZone.ET);
        emailfileExporter.export(googleCalendarExportFileLocation, allAuctions);
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
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for(Auction auction : allAuctions){
            Runnable runnableTask = () -> {
                System.out.print("Getting listings for "+auction.getCounty().getCountyName()+" auction on "+auction.getDate()+"...   ");
                List<AuctionListing> auctionListings = null;
                try {
                    auctionListings = taxAuctionService.getAuctionListings(auction);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                auction.setAuctionListings(auctionListings);
                System.out.println("complete");
            };
            executor.submit(runnableTask);
        }
    }
}
