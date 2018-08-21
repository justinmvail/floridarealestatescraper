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
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GetExcelSpreadSheetForDateTask extends AbstractParentTask {

    public static void main( String[] args ) throws IOException, InterruptedException, ExecutionException {
        final TaxAuctionService taxAuctionService = new RealTaxDeedScraper(false);
        final MlsService mlsService = new ZillowScraper(false);
        final SearchEngineResultService searchEngineResultService = new GoogleScraper();
        final FileExporter fileExporter = new XLSXFileExporter();

        startTiming();

        String strDate = args[0];
        int numberOfThreads = Integer.parseInt(args[1]);
        String exportPath = args[2];

        County[] allCounties = County.class.getEnumConstants();
        List<Auction> auctions = getAllAuctionsByDate(allCounties, LocalDate.parse(strDate), taxAuctionService, numberOfThreads);
        populateAllAuctionListings(auctions, taxAuctionService, numberOfThreads);
        AuctionTimeUpdater.updateAuctionTimesForTimeZone(auctions, TimeZone.ET);
        //We only want to use 2 threads for Zillow.  They get mad at robots.
        populateAllMlsListings(auctions, mlsService, 2);
        populateAllSearchEngineResults(auctions, searchEngineResultService, numberOfThreads);
        auctions.removeIf(auction -> auction.getAuctionListings()==null);
        auctions.sort(Comparator.comparing(Auction::getTime));
        auctions.forEach(auction -> auction.getAuctionListings().sort(Comparator.comparing(AuctionListing::getAssessedValue)));
        fileExporter.export(exportPath+strDate+".xlsx", auctions);
        stopTiming();
        System.out.println("Good Bye.");
    }
}
