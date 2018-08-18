package realestateScraper.export;

import realestateScraper.objects.Auction;

import java.io.IOException;
import java.util.List;

public interface FileExporter {
    void export(String fileLocation, List<Auction> auctionList) throws IOException;
}
