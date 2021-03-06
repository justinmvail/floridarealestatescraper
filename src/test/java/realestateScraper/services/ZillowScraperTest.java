package realestateScraper.services;

import org.junit.Test;
import realestateScraper.objects.AuctionListing;
import realestateScraper.objects.MlsListing;

import java.io.IOException;

import static org.junit.Assert.*;

public class ZillowScraperTest {
    //TODO: these aren't great tests.  They rely on Zillow state not changing
    //rewrite these tests to fake the results of zillow.
    @Test
    public void getMlsListingForAuctionListingTypeOne() throws Exception {
        ZillowScraper zillowScraper = new ZillowScraper(false);
        AuctionListing auctionListing = new AuctionListing();
        auctionListing.setPropertyAddress("665 ROBINSON AVE E CRESTVIEW, FL- 32536");
        MlsListing mlsListing = null;
        try {
            mlsListing = zillowScraper.getMlsListingForAuctionListing(auctionListing);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals((Float)107824f, mlsListing.getPriceEstimate());
    }

    @Test
    public void getMlsListingForAuctionListingTypeTwo() throws Exception {
        ZillowScraper zillowScraper = new ZillowScraper(false);
        AuctionListing auctionListing = new AuctionListing();
        auctionListing.setPropertyAddress("103 BEAL PKWY SE FT WALTON BEACH, FL- 32548");
        MlsListing mlsListing = null;
        try {
            mlsListing = zillowScraper.getMlsListingForAuctionListing(auctionListing);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertEquals((Float)190258f, mlsListing.getPriceEstimate());
    }

    @Test
    public void getMlsListingForAuctionListingWithBadString() throws Exception {
        ZillowScraper zillowScraper = new ZillowScraper(false);
        AuctionListing auctionListing = new AuctionListing();
        auctionListing.setPropertyAddress("Completely wrong address format 1231");
        MlsListing mlsListing = null;
        try {
            mlsListing = zillowScraper.getMlsListingForAuctionListing(auctionListing);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertNull(mlsListing);
    }

}