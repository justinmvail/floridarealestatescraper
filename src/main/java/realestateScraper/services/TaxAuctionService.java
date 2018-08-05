package realestateScraper.services;

import realestateScraper.DomainObjects.Auction;
import realestateScraper.DomainObjects.AuctionListing;
import realestateScraper.DomainObjects.AuctionType;
import realestateScraper.DomainObjects.County;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public interface TaxAuctionService {


    List<Auction> getAllAuctionDatesByMonth(AuctionType auctionType, County county, LocalDate startingDate) throws IOException, InterruptedException;

    List<Auction> getAllAuctionDatesByWeek(AuctionType auctionType, County county, LocalDate startingDate) throws IOException, InterruptedException;

    List<Auction> getAllAuctionDatesByDay(AuctionType auctionType, County county, LocalDate startingDate);

    List<Auction> getAllAuctionDatesForRange(AuctionType auctionType, County county, LocalDate inclusiveStartingDate, LocalDate exclusiveEndingDate);

    List<AuctionListing> getAuctionListings(Auction auction);
}
