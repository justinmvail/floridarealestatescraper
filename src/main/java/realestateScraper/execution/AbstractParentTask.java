package realestateScraper.execution;

import realestateScraper.constants.AuctionType;
import realestateScraper.constants.County;
import realestateScraper.objects.Auction;
import realestateScraper.objects.AuctionListing;
import realestateScraper.objects.MlsListing;
import realestateScraper.services.MlsService;
import realestateScraper.services.SearchEngineResultService;
import realestateScraper.services.TaxAuctionService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

abstract class AbstractParentTask {
    private static long startTime;

    static void startTiming(){
        startTime = System.nanoTime();
    }

    static long stopTiming(){
        long endTime = System.nanoTime();
        long duration =  (endTime - startTime) / 1000000000;
        System.out.println("Total duration: "+ duration);
        return duration;
    }

    static List<Auction> getAllAuctionsByMonth(County[] counties, LocalDate date, TaxAuctionService taxAuctionService, int numberOfThreads) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<Callable<List<Auction>>> callableList = new ArrayList<>();
        List<Auction> allAuctions = new ArrayList<>();
        for(County county : counties){
            Callable<List<Auction>> callableTask = () -> {
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
        shutDownExecutor(executor, "All Auctions by Month");
        return allAuctions;
    }

    static List<Auction> getAllAuctionsByDate(County[] counties, LocalDate date, TaxAuctionService taxAuctionService, int numberOfThreads) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<Callable<Auction>> callableList = new ArrayList<>();
        List<Auction> allAuctions = new ArrayList<>();
        for(County county : counties){
            Callable<Auction> callableTask = () -> {
                System.out.println("Getting Auctions for " + county.getCountyName() + "...   ");
                Auction auction = taxAuctionService.getAuctionByDate(AuctionType.TAXDEED, county, date);
                System.out.println("---"+county.getCountyName() + " auctions retrieved.");
                return auction;
            };
            callableList.add(callableTask);
        }
        List<Future<Auction>> futures = executor.invokeAll(callableList);
        for(Future<Auction> future : futures){
            Auction auction = future.get();
            if(auction != null) allAuctions.add(auction);
        }
        shutDownExecutor(executor, "Auctions by date");
        return allAuctions;
    }

    static void populateAllAuctionListings(List<Auction> auctions, TaxAuctionService taxAuctionService, int numberOfThreads) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<Callable<Object>> callableList = new ArrayList<>();
        for(Auction auction : auctions){
            Callable<Object> callableTask = () -> {
                System.out.println("Getting listings for "+auction.getCounty().getCountyName()+" auction on "+auction.getDate()+"...   ");
                List<AuctionListing> auctionListings = taxAuctionService.getAuctionListings(auction);
                if(!auctionListings.isEmpty()) auction.setAuctionListings(auctionListings);
                System.out.println("---Retrieved listings for "+auction.getCounty().getCountyName()+" auction on "+auction.getDate());
                return null;
            };
            callableList.add(callableTask);
        }
        executor.invokeAll(callableList);
        shutDownExecutor(executor, "All Auction Listings");
    }

    static void populateAllMlsListings(List<Auction> auctions, MlsService mlsService, int numberOfThreads) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<Callable<Object>> callableList = new ArrayList<>();
        for(Auction auction : auctions){
            if(auction.getAuctionListings()==null) continue;
            for(AuctionListing auctionListing : auction.getAuctionListings()) {
                Callable<Object> callableTask = () -> {
                    System.out.println("Getting mls listings for " + auctionListing.getPropertyAddress() + "...   ");
                    MlsListing mlsListing = mlsService.getMlsListingForAuctionListing(auctionListing);
                    auctionListing.setMlsListing(mlsListing);
                    System.out.println("---Retrieved mls listings for " + auctionListing.getPropertyAddress());
                    return null;
                };
                callableList.add(callableTask);
            }
        }
        executor.invokeAll(callableList);
        shutDownExecutor(executor, "MLS");
    }

    static void populateAllSearchEngineResults(List<Auction> auctions, SearchEngineResultService searchEngineResultService, int numberOfThreads) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        List<Callable<Object>> callableList = new ArrayList<>();
        for(Auction auction : auctions){
            if(auction.getAuctionListings()==null) continue;
            for(AuctionListing auctionListing : auction.getAuctionListings()) {
                Callable<Object> callableTask = () -> {
                    System.out.println("Getting search engine results for " + auctionListing.getPropertyAddress() + "...   ");
                    String serachEngineUrl = searchEngineResultService.getUrlForSearchResults(auctionListing.getPropertyAddress());
                    auctionListing.setSearchEngineResultUrl(serachEngineUrl);
                    System.out.println("---Retrieved search engine results for " + auctionListing.getPropertyAddress());
                    return null;
                };
                callableList.add(callableTask);
            }
        }
        executor.invokeAll(callableList);
        shutDownExecutor(executor, "MLS");
    }

    private static void shutDownExecutor(ExecutorService executor, String context){
        executor.shutdown();
        try {
            if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
                System.out.println("Forcing Shutdown of the executor service for "+context);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
