package realestateScraper.services;

import realestateScraper.DomainObjects.AuctionListing;
import realestateScraper.DomainObjects.MlsListing;

import java.io.IOException;

public interface MlsService {

    MlsListing getMlsListingForAuctionListing(AuctionListing auctionListing) throws IOException;

}
