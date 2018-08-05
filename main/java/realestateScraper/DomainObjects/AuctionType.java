package realestateScraper.DomainObjects;

public enum AuctionType {
    TAX_DEED("Tax Deed");

    private final String displayName;

    AuctionType(String name){
        this.displayName = name;
    }

    public String getDisplayName() {
        return displayName;
    }
}
