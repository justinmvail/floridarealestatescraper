package realestateScraper;

import realestateScraper.DomainObjects.Auction;
import realestateScraper.DomainObjects.AuctionType;
import realestateScraper.DomainObjects.County;
import realestateScraper.DomainObjects.TimeZone;
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
        List<Auction> allAuctions = new ArrayList<>();
        TaxAuctionService taxAuctionService = new RealTaxDeedScraper(false);
        County[] allCounties = County.class.getEnumConstants();
        for(County county : allCounties){
            System.out.print(county.getCountyName()+" has started...   ");
            List<Auction> auctionList = taxAuctionService.getAllAuctionDatesByMonth(AuctionType.TAXDEED, county, LocalDate.parse("2018-08-01"));
            allAuctions.addAll(auctionList);
            System.out.println("complete." );
        }
        AuctionTimeUpdater.updateAuctionTimesByTimeZone(allAuctions, TimeZone.ET);
        Auction auction = allAuctions.get(0);


        FileExporter fileExporter = new GoogleCalendarCSVExporter();
        fileExporter.export(args[0], allAuctions);
    }
}
