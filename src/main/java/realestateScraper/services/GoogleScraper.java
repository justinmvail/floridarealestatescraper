package realestateScraper.services;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import java.io.IOException;

public class GoogleScraper extends HtmlUnitScraper implements SearchEngineResultService {
    @Override
    public String getUrlForSearchResults(String searchPhrase) throws IOException {
        WebClient webClient = getGoogleWebClient(false);
        HtmlPage googleHomePage = webClient.getPage("http://www.google.com");
        HtmlInput searchInput = googleHomePage.getElementByName("q");
        searchInput.setValueAttribute(searchPhrase);
        HtmlSubmitInput submitSearchButton = googleHomePage.getElementByName("btnK");
        HtmlPage googleResultsPage = submitSearchButton.click();
        String strUrl = googleResultsPage.getUrl().toString();
        webClient.close();
        return strUrl;
    }

    private WebClient getGoogleWebClient(boolean useLogging){
        final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_52);
        setScraperLogging(webClient, useLogging);
        return webClient;
    }
}
