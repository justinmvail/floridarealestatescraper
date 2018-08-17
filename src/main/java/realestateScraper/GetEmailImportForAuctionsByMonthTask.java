package realestateScraper;

import realestateScraper.Constants.County;
import realestateScraper.Constants.TimeZone;
import realestateScraper.DomainObjects.Auction;
import realestateScraper.export.FileExporter;
import realestateScraper.export.GoogleCalendarCSVExporter;
import realestateScraper.services.RealTaxDeedScraper;
import realestateScraper.services.TaxAuctionService;
import realestateScraper.translation.AuctionTimeUpdater;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GetEmailImportForAuctionsByMonthTask extends AbstractParentTask {

    public static void main( String[] args ) throws IOException, InterruptedException, ExecutionException {
        final TaxAuctionService taxAuctionService = new RealTaxDeedScraper(false);
        final FileExporter emailFileExporter = new GoogleCalendarCSVExporter();

        startTiming();

        String googleCalendarExportFileLocation = args[0];
        String strDate = args[1];
        int numberOfThreads = Integer.parseInt(args[2]);

        County[] allCounties = County.class.getEnumConstants();
        List<Auction> allAuctions = getAllAuctionsByMonth(allCounties,  LocalDate.parse(strDate), taxAuctionService, numberOfThreads);
        AuctionTimeUpdater.updateAuctionTimesForTimeZone(allAuctions, TimeZone.ET);
        emailFileExporter.export(googleCalendarExportFileLocation, allAuctions);

        stopTiming();
        System.out.println("Good Bye.");
    }
}
