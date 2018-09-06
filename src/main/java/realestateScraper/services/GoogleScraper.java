package realestateScraper.services;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import java.io.IOException;

public class GoogleScraper extends HtmlUnitScraper implements SearchEngineResultService {
    @Override
    public String getUrlForSearchResults(String searchPhrase) throws IOException {
        WebClient webClient = getGoogleWebClient(false);
        HtmlPage googleHomePage = webClient.getPage("http://www.google.com");
        HtmlInput searchInput = googleHomePage.getElementByName("q");
        searchInput.setValueAttribute(searchPhrase);
        HtmlForm form = googleHomePage.getElementByName("f");
        HtmlPage googleResultsPage = addButtonToFormAndSubmit(googleHomePage, form);
        String strUrl = googleResultsPage.getUrl().toString();
        webClient.close();
        return strUrl;
    }

    private HtmlPage addButtonToFormAndSubmit(HtmlPage calendarPage, HtmlForm form) throws IOException {
        HtmlElement button = (HtmlElement) calendarPage.createElement("button");
        button.setAttribute("type", "submit");
        form.appendChild(button);
        return button.click();
    }

    private WebClient getGoogleWebClient(boolean useLogging){
        final WebClient webClient = new WebClient(BrowserVersion.CHROME);
        setScraperLogging(webClient, useLogging);
        return webClient;
    }

    public static void main(String[] args){
        GoogleScraper googleScraper = new GoogleScraper();
        String url = null;
        try {
            url = googleScraper.getUrlForSearchResults("233 SUNSET BOULEVARD E PUNTA GORDA, FL- 33982");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(url);
    }
}
