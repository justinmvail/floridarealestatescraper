package realestateScraper.export;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import realestateScraper.DomainObjects.Auction;
import realestateScraper.translation.SimpleTimeZoneRectifier;
import realestateScraper.DomainObjects.TimeZone;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;

public class GoogleCalendarCSVExporter implements FileExporter {

    public void export(String fileLocation, List<Auction> auctionList) throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileLocation));
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(
                "Subject",
                "Start Date",
                "Start Time",
                "All Day Event",
                "Description",
                "Private"
        ));
        for(Auction auction : auctionList) {
            LocalTime adjustedTime = SimpleTimeZoneRectifier.rectifyTimeZone(auction.getTime(), auction.getCounty(), TimeZone.ET);
            csvPrinter.printRecord(
                    auction.getCounty().getCountyName()+" Auction",
                    auction.getDate(),
                    adjustedTime,
                    false,
                    auction.getUrl(),
                    true
            );
        }
        csvPrinter.flush();
        csvPrinter.close();
    }
}
