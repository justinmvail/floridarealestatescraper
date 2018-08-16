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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AuctionRunner {
    private static TaxAuctionService taxAuctionService = new RealTaxDeedScraper(false);
    private static FileExporter emailfileExporter = new GoogleCalendarCSVExporter();

    public static void main( String[] args ) throws IOException, InterruptedException, ExecutionException {
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

    private static List<Auction> getAllAuctions(County[] allCounties, LocalDate date, TaxAuctionService taxAuctionService) throws IOException, InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        List<Callable<List<Auction>>> callableList = new ArrayList<>();
        List<Auction> allAuctions = new ArrayList<>();
        for(County county : allCounties){
            Callable callableTask = () -> {
                System.out.println("Getting Auctions for " + county.getCountyName() + "...   ");
                List<Auction> countyAuctionList = taxAuctionService.getAllAuctionDatesByMonth(AuctionType.TAXDEED, county, date);
                System.out.println("---"+county.getCountyName() + " auctions retrieved.");
                return countyAuctionList;
            };
            callableList.add(callableTask);
        }
        List<Future<List<Auction>>> futures = executor.invokeAll(callableList);
        for(Future<List<Auction>> future : futures){
            allAuctions.addAll(future.get());
        }
        executor.shutdown();
        return allAuctions;
    }

    private static void populateAllAuctionListings(List<Auction> allAuctions, TaxAuctionService taxAuctionService) throws IOException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        List<Callable<Object>> callableList = new ArrayList<>();
        for(Auction auction : allAuctions){
            Callable callableTask = () -> {
                System.out.println("Getting listings for "+auction.getCounty().getCountyName()+" auction on "+auction.getDate()+"...   ");
                List<AuctionListing> auctionListings = null;
                try {
                    auctionListings = taxAuctionService.getAuctionListings(auction);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                auction.setAuctionListings(auctionListings);
                System.out.println("---Retrieved listings for "+auction.getCounty().getCountyName()+" auction on "+auction.getDate());
                return null;
            };
            callableList.add(callableTask);
        }
        executor.invokeAll(callableList);
        executor.shutdown();
    }
}
