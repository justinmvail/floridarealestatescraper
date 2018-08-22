package realestateScraper.objects;

import realestateScraper.constants.AuctionType;

public class AuctionListing {
    private AuctionType auctionType;
    private String caseNumber;
    private String certificateNumber;
    private Float openingBid;
    private String parcelID;
    private String propertyAddress;
    private Float assessedValue;
    private String parcelUrl;
    private MlsListing mlsListing;
    private String searchEngineResultUrl;

    public AuctionListing(AuctionType auctionType, String caseNumber, String certificateNumber, Float openingBid, String parcelID, String propertyAddress, float assessedValue) {
        this.auctionType = auctionType;
        this.caseNumber = caseNumber;
        this.certificateNumber = certificateNumber;
        this.openingBid = openingBid;
        this.parcelID = parcelID;
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

    public Float getOpeningBid() {
        return openingBid;
    }

    public void setOpeningBid(Float openingBid) {
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

    public Float getAssessedValue() {
        return assessedValue;
    }

    public void setAssessedValue(Float assessedValue) {
        this.assessedValue = assessedValue;
    }

    public String getParcelUrl() {
        return parcelUrl;
    }

    public void setParcelUrl(String parcelUrl) {
        this.parcelUrl = parcelUrl;
    }

    public MlsListing getMlsListing() {
        return mlsListing;
    }

    public void setMlsListing(MlsListing mlsListing) {
        this.mlsListing = mlsListing;
    }

    public String getSearchEngineResultUrl() {
        return searchEngineResultUrl;
    }

    public void setSearchEngineResultUrl(String searchEngineResultUrl) {
        this.searchEngineResultUrl = searchEngineResultUrl;
    }
}
