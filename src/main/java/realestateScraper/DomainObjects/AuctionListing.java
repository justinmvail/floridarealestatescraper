package realestateScraper.DomainObjects;

import realestateScraper.Constants.AuctionType;

public class    AuctionListing {
    private AuctionType auctionType;
    private String caseNumber;
    private String certificateNumber;
    private float openingBid;
    private String parcelID;
    private String propertyAddress;
    private float assessedValue;
    private MlsListing mlsListing;

    public AuctionListing(AuctionType auctionType, String caseNumber, String certificateNumber, float openingBid, String parcelID, String propertyAddress, float assessedValue) {
        this.auctionType = auctionType;
        this.caseNumber = caseNumber;
        this.certificateNumber = certificateNumber;
        this.openingBid = openingBid;
        this.parcelID = parcelID;
        this.propertyAddress = propertyAddress;
        this.assessedValue = assessedValue;
    }

    public AuctionListing() {}

    public AuctionType getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(AuctionType auctionType) {
        this.auctionType = auctionType;
    }

    public String getCaseNumber() {
        return caseNumber;
    }

    public void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public float getOpeningBid() {
        return openingBid;
    }

    public void setOpeningBid(float openingBid) {
        this.openingBid = openingBid;
    }

    public String getParcelID() {
        return parcelID;
    }

    public void setParcelID(String parcelID) {
        this.parcelID = parcelID;
    }

    public String getPropertyAddress() {
        return propertyAddress;
    }

    public void setPropertyAddress(String propertyAddress) {
        this.propertyAddress = propertyAddress;
    }

    public float getAssessedValue() {
        return assessedValue;
    }

    public void setAssessedValue(float assessedValue) {
        this.assessedValue = assessedValue;
    }

    public MlsListing getMlsListing() {
        return mlsListing;
    }

    public void setMlsListing(MlsListing mlsListing) {
        this.mlsListing = mlsListing;
    }
}
