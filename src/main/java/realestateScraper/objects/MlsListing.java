package realestateScraper.objects;

public class MlsListing {
    private String url;
    private Float priceEstimate;

    public MlsListing(String url, Float priceEstimate) {
        this.url = url;
        this.priceEstimate = priceEstimate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Float getPriceEstimate() {
        return priceEstimate;
    }

    public void setPriceEstimate(Float priceEstimate) {
        this.priceEstimate = priceEstimate;
    }
}
