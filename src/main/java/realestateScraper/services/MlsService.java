package realestateScraper.services;

import realestateScraper.objects.AuctionListing;
import realestateScraper.objects.MlsListing;

import java.io.IOException;

public interface MlsService {

    MlsListing getMlsListingForAuctionListing(AuctionListing auctionListing) throws IOException;

}
