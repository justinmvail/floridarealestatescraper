package realestateScraper.constants;

public enum AuctionType {
    TAXDEED("Tax Deed"),
    FORECLOSURE("Foreclosure");

    private final String displayName;

    AuctionType(String name){
        this.displayName = name;
    }

    public String getDisplayName() {
        return displayName;
    }
}
