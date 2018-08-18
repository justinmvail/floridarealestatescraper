package realestateScraper.export;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import realestateScraper.objects.Auction;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
            csvPrinter.printRecord(
                    auction.getCounty().getCountyName()+" Auction",
                    auction.getDate(),
                    auction.getTime(),
                    false,
                    auction.getUrl(),
                    true
            );
        }
        csvPrinter.flush();
        csvPrinter.close();
    }
}
