package realestateScraper.execution;

import realestateScraper.constants.County;
import realestateScraper.constants.TimeZone;
import realestateScraper.export.FileExporter;
import realestateScraper.export.XLSXFileExporter;
import realestateScraper.objects.*;
import realestateScraper.services.GoogleScraper;
import realestateScraper.services.MlsService;
import realestateScraper.services.RealTaxDeedScraper;
import realestateScraper.services.SearchEngineResultService;
import realestateScraper.services.TaxAuctionService;
import realestateScraper.services.ZillowScraper;
import realestateScraper.translation.AuctionTimeUpdater;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class GetExcelSpreadSheetForDateTask extends AbstractParentTask {

    public static void main( String[] args ) throws IOException, InterruptedException, ExecutionException {

        startTiming();

        final TaxAuctionService taxAuctionService = new RealTaxDeedScraper(false);
        final MlsService mlsService = new ZillowScraper(false);
        final SearchEngineResultService searchEngineResultService = new GoogleScraper();
        final FileExporter fileExporter = new XLSXFileExporter();

        String strStartDate = args[0];
        String strStopDate = args[1];
        float minimumValue = Float.parseFloat(args[2]);
        int numberOfThreads = Integer.parseInt(args[3]);
        String exportPath = args[4];

        List<LocalDate> dates =  getDatesInRange(LocalDate.parse(strStartDate), LocalDate.parse(strStopDate));
        County[] allCounties = County.class.getEnumConstants();
        List<Auction> auctions = new ArrayList<>();

        for(LocalDate date : dates){
            auctions.addAll(getAllAuctionsByDate(allCounties, date, taxAuctionService, numberOfThreads));
        }

        populateAllAuctionListings(auctions, taxAuctionService, numberOfThreads);
        AuctionTimeUpdater.updateAuctionTimesForTimeZone(auctions, TimeZone.ET);
        //We only want to use 2 threads for Zillow.  They get mad at robots.
        populateAllMlsListings(auctions, mlsService, 2);
        populateAllSearchEngineResults(auctions, searchEngineResultService, numberOfThreads);
        auctions.removeIf(auction -> auction.getAuctionListings()==null);
        auctions.sort(getViableListingComparator(minimumValue));
        auctions.forEach(auction -> auction.getAuctionListings().sort(getAssessedValueComparator()));
        fileExporter.export(getExportFilePath(exportPath, strStartDate, strStopDate), auctions);
        stopTiming();
        System.out.println("Good Bye.");
    }

    private static List<LocalDate> getDatesInRange(LocalDate startDate, LocalDate endDate){
        List<LocalDate> dates = new ArrayList<>();
        LocalDate nextDate = startDate;
        while(nextDate.isBefore(endDate) || nextDate.isEqual(endDate)){
            dates.add(nextDate);
            nextDate = nextDate.plusDays(1);
        }
        return dates;
    }

    private static String getExportFilePath(String exportPath, String strStartDate, String strStopDate){
        return   exportPath
                +strStartDate
                +" to "
                +strStopDate
                +" -ran on- "
                +LocalDateTime.now()
                .toString()
                .substring(0, 16)
                .replace(":","")
                .replace("T"," at ")
                +".xlsx";
    }

    private static Comparator<Auction> getViableListingComparator(float minimumValue){
        return (auctionOne, auctionTwo) -> {
            int auctionOneViableListingCount = getCountOfViableAuctions(auctionOne, minimumValue);
            int auctionTwoViableListingCount = getCountOfViableAuctions(auctionTwo, minimumValue);
            return auctionTwoViableListingCount-auctionOneViableListingCount;
        };
    }

    private static int getCountOfViableAuctions(Auction auction, float minimumValue){
        Set<AuctionListing> viableAuctionListings = new HashSet<>();
        for (AuctionListing auctionListing : auction.getAuctionListings()){
            MlsListing mlsListing = auctionListing.getMlsListing();
            if(mlsListing != null && mlsListing.getPriceEstimate() != null && mlsListing.getPriceEstimate()>=minimumValue){
                viableAuctionListings.add(auctionListing);
            }
        }
        return viableAuctionListings.size();
    }

    private static Comparator<AuctionListing> getAssessedValueComparator(){
        return (auctionListingOne, auctionListingTwo) -> {
            if(auctionListingOne.getAssessedValue() == null && auctionListingTwo.getAssessedValue() == null){
                return 0;
            }if(auctionListingOne.getAssessedValue() == null){
                return -1;
            }if(auctionListingTwo.getAssessedValue() == null){
                return 1;
            }else if(auctionListingOne.getAssessedValue() < auctionListingTwo.getAssessedValue()){
                return -1;
            }else if(auctionListingTwo.getAssessedValue() < auctionListingOne.getAssessedValue()){
                return 1;
            }else{
                return 0;
            }
        };
    }
}
