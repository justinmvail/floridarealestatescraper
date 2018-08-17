package realestateScraper;

import realestateScraper.Constants.County;
import realestateScraper.Constants.TimeZone;
import realestateScraper.DomainObjects.*;
import realestateScraper.export.FileExporter;
import realestateScraper.export.GoogleCalendarCSVExporter;
import realestateScraper.services.MlsService;
import realestateScraper.services.RealTaxDeedScraper;
import realestateScraper.services.TaxAuctionService;
import realestateScraper.services.ZillowScraper;
import realestateScraper.translation.AuctionTimeUpdater;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GetExcelSpreadSheetForDateTask extends AbstractParentTask {

    public static void main( String[] args ) throws IOException, InterruptedException, ExecutionException {
        final TaxAuctionService taxAuctionService = new RealTaxDeedScraper(false);
        final MlsService mlsService = new ZillowScraper(false);
        final FileExporter emailFileExporter = new GoogleCalendarCSVExporter();

        startTiming();

        String strDate = args[0];
        int numberOfThreads = Integer.parseInt(args[1]);

        County[] allCounties = County.class.getEnumConstants();
        List<Auction> allAuctions = getAllAuctionsByDate(allCounties, LocalDate.parse(strDate), taxAuctionService, numberOfThreads);
        populateAllAuctionListings(allAuctions, taxAuctionService, numberOfThreads);
        AuctionTimeUpdater.updateAuctionTimesForTimeZone(allAuctions, TimeZone.ET);
        //We only want to use 2 threads for Zillow.  They get mad with higher load.
        populateAllMlsListings(allAuctions, mlsService, 2);

        stopTiming();
        System.out.println("Good Bye.");
    }
}
