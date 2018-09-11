package realestateScraper.services;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import org.apache.commons.lang3.StringUtils;
import realestateScraper.objects.AuctionListing;
import realestateScraper.objects.MlsListing;

import java.io.IOException;
import java.util.List;

public class ZillowScraper extends HtmlUnitScraper implements MlsService {

    private final boolean useFrameworkLogging;

    public ZillowScraper(boolean useFrameworkLogging){
        this.useFrameworkLogging = useFrameworkLogging;
    }

    @Override
    public MlsListing getMlsListingForAuctionListing(AuctionListing auctionListing) throws IOException {
        if(isBadInput(auctionListing)) return null;
        WebClient webClient = getZillowWebClient(useFrameworkLogging);
        HtmlPage zillowHomePage = webClient.getPage("https://www.zillow.com");
        hasZillowCaughtMe(zillowHomePage);
        HtmlPage resultsPage = searchZillowFor(zillowHomePage, auctionListing);
        return getMlsListing(resultsPage);
    }

    private boolean isBadInput(AuctionListing auctionListing){
        if(auctionListing.getPropertyAddress()==null) return true;
        if(auctionListing.getPropertyAddress().trim().equals("UNKNOWN")) return true;
        return false;
    }

    private WebClient getZillowWebClient(boolean useLogging){
        final WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        setScraperLogging(webClient, useLogging);
        return webClient;
    }

    private void hasZillowCaughtMe(HtmlPage zillowHomePage){
        //If Zillow is prompting us for a captcha, we are screwed.
        List<HtmlDivision> errorDivs = zillowHomePage.getByXPath("//div[@class='error-text-content']");
        if (errorDivs.size()>0){
            HtmlDivision errorDiv = errorDivs.get(0);
            if(errorDiv.getFirstChild().getNextSibling().asText().trim().equals("Please verify you're a human to continue.")){
                System.out.println("Zillow caught me!!!");
                throw new UnsupportedOperationException("Zillow caught me!!!");
            }
        }
    }

    private HtmlPage searchZillowFor(HtmlPage zillowHomePage, AuctionListing auctionListing) throws IOException {
        HtmlInput searchInput = (HtmlInput) zillowHomePage.getByXPath("//*[@id=\"citystatezip\"]").get(0);
        searchInput.setValueAttribute(auctionListing.getPropertyAddress());
        HtmlForm form = zillowHomePage.getFormByName("formSearchBar");
        HtmlElement button = (HtmlElement) zillowHomePage.createElement("button");
        button.setAttribute("type", "submit");
        form.appendChild(button);
        return button.click();
    }

    private MlsListing getMlsListing(HtmlPage resultsPage){
        if (resultsPage.getTitleText().contains("Real Estate")) return null; //We didn't find a match.
        //I have no idea why this happens.  Zillow has (at least) two distinct ways of constructing the DOM.  Mode
        //1 and 2 are the rectify that.
        List<HtmlSpan> modeOneSpans = resultsPage.getByXPath("//span[@class='zsg-tooltip-launch zsg-tooltip-launch_keyword']");
        List<HtmlSpan> modeTwoSpans = resultsPage.getByXPath("//span[@class='zsg-tooltip-launch_keyword']");
        String mlsUrl = resultsPage.getUrl().toString();
        Float zestimate = null;
        if(modeOneSpans.size()>=2)zestimate = extractZestimate(modeOneSpans.get(1).getNextSibling().getNextSibling().asText());
        if(modeTwoSpans.size()>=2 && zestimate==null) zestimate = extractZestimate(modeTwoSpans.get(1).getParentNode().getParentNode().asText().split(":")[1]);
        return new MlsListing(mlsUrl, zestimate);
    }

    private Float extractZestimate(String strZestimatePrice){
        strZestimatePrice = strZestimatePrice
                .trim()
                .replace("$", "")
                .replace(",", "");
        Float zestimate;
        if (StringUtils.isNumeric(strZestimatePrice)) zestimate = Float.parseFloat(strZestimatePrice);
        else zestimate = null;
        return zestimate;
    }
}
