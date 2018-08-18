package realestateScraper.objects;

public class MlsListing {
    private String url;
    private float priceEstimate;

    public MlsListing(String url, float priceEstimate) {
        this.url = url;
        this.priceEstimate = priceEstimate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public float getPriceEstimate() {
        return priceEstimate;
    }

    public void setPriceEstimate(float priceEstimate) {
        this.priceEstimate = priceEstimate;
    }
}
