package realestateScraper.services;

import com.gargoylesoftware.css.parser.CSSErrorHandler;
import com.gargoylesoftware.css.parser.CSSException;
import com.gargoylesoftware.css.parser.CSSParseException;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.IncorrectnessListener;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.FalsifyingWebConnection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.LogFactory;
import realestateScraper.DomainObjects.Auction;
import realestateScraper.DomainObjects.AuctionListing;
import realestateScraper.DomainObjects.AuctionType;
import realestateScraper.DomainObjects.County;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RealTaxDeedScraper implements TaxAuctionService{

    private final BrowserVersion browserVersion = BrowserVersion.CHROME;
    private final String calendarUrlAppension = "/index.cfm?zaction=USER&zmethod=CALENDAR";
    private final String auctionUrlAppension = "/index.cfm?zaction=AUCTION&Zmethod=PREVIEW&AUCTIONDATE=";
    private final String monthSelectorXpath = "//*[@id=\"selCalDate\"]";
    private final String auctionDateXpathWithAuctionTypePlaceHolder = "//*[text()='%s']";
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    private final boolean useFrameworkLogging;

    public RealTaxDeedScraper(boolean useFrameworkLogging){
        this.useFrameworkLogging = useFrameworkLogging;
    }

    @Override
    public List<Auction> getAllAuctionDatesByMonth(AuctionType auctionType, County county, LocalDate startingDate) throws IOException, InterruptedException {
        WebClient webClient = getRealTaxDeedWebClient(useFrameworkLogging);
        HtmlPage calendarPage = webClient.getPage(county.getUrl()+calendarUrlAppension);
        HtmlSelect monthSelector = calendarPage.getFirstByXPath(monthSelectorXpath);
        List<HtmlOption> monthYearOptions = monthSelector.getOptions();
        selectMonth(monthSelector, monthYearOptions, startingDate);
        HtmlForm form = calendarPage.getFormByName("monthChoose");
        addButtonToFormAndSubmit(calendarPage, form);
        List<HtmlElement> taxAuctionDates = calendarPage.getByXPath(String.format(auctionDateXpathWithAuctionTypePlaceHolder, auctionType.getDisplayName()));
        taxAuctionDates.removeIf((HtmlElement auctionDate) -> !(auctionDate instanceof HtmlBold));
        return getAuctionsfromHtmlElements(taxAuctionDates, auctionType, county);
    }

    @Override
    public List<Auction> getAllAuctionDatesByWeek(AuctionType auctionType, County county, LocalDate startingDate) throws IOException, InterruptedException {
        //Weeks are defined Sunday to Monday
        LocalDate endingDate = startingDate.plusDays(7);
        List<Auction> auctionList;
        auctionList = getAllAuctionDatesByMonth(auctionType, county, startingDate);
        if(!startingDate.getMonth().equals(endingDate.getMonth())){
            auctionList.addAll(getAllAuctionDatesByMonth(auctionType, county, endingDate));
        }

        //TODO:filter out days not in week
        return auctionList;
    }

    @Override
    public List<Auction> getAllAuctionDatesByDay(AuctionType auctionType, County county, LocalDate startingDate) {
        return null;
    }

    @Override
    public List<Auction> getAllAuctionDatesForRange(AuctionType auctionType, County county, LocalDate inclusiveStartingDate, LocalDate exclusiveEndingDate) {
        return null;
    }

    @Override
    public List<AuctionListing> getAuctionListings(Auction auction) throws IOException {
        WebClient webClient = getRealTaxDeedWebClient(useFrameworkLogging);
        HtmlPage htmlPage = webClient.getPage(auction.getUrl());
        HtmlDivision upcomingListingsDivision = (HtmlDivision) htmlPage.getElementById("Area_W");
        List<HtmlDivision> auctionListingDivs = upcomingListingsDivision.getByXPath("//div[contains(@class, 'AUCTION_DETAILS')]");
        List<AuctionListing> listings = new ArrayList<>();
        for(HtmlDivision div : auctionListingDivs){
            DomNodeList<DomNode> domNodeList = div.getFirstChild().getFirstChild().getChildNodes();
            AuctionListing auctionListing = new AuctionListing();
            for(DomNode domNode : domNodeList){
                if(!(domNode instanceof HtmlTableRow)) continue;
                HtmlTableRow htmlTableRow = (HtmlTableRow)domNode;
                List<HtmlTableCell> cells = htmlTableRow.getCells();
                String cellLabel = cells.get(0).asText();
                if(cellLabel.equals("Auction Type:")){
                    auctionListing.setAuctionType(AuctionType.valueOf(cells.get(1).asText().replace(" ", "")));
                }else if(cellLabel.equals("Case #:")){
                    auctionListing.setCaseNumber(cells.get(1).asText());
                }else if(cellLabel.equals("Certificate #:")){
                    auctionListing.setCertificateNumber(cells.get(1).asText());
                }else if(cellLabel.equals("Opening Bid:")){
                    auctionListing.setOpeningBid(Float.parseFloat(cells.get(1).asText().replace("$","").replace(",","")));
                }else if(cellLabel.equals("Parcel ID:")){
                    auctionListing.setParcelID(cells.get(1).asText());
                }else if(cellLabel.equals("Property Address:")) {
                    auctionListing.setPropertyAddress(cells.get(1).asText());
                }else if(cellLabel.equals("")){
                    auctionListing.setPropertyAddress(auctionListing.getPropertyAddress() + " " + cells.get(1).asText());
                }else if(cellLabel.equals("Assessed Value:")) {
                    auctionListing.setAssessedValue(Float.parseFloat(cells.get(1).asText().replace("$","").replace(",","")));
                }
            }
            listings.add(auctionListing);
        }
        return  listings;
    }

    private void selectMonth(HtmlSelect monthSelector, List<HtmlOption> monthYearOptions, LocalDate date){
        final DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MMMM YYYY");
        final String monthYearFormattedDate = date.format(monthYearFormatter);
        for(HtmlOption htmlOption : monthYearOptions){
            if(htmlOption.getText().equals(monthYearFormattedDate)){
                monthSelector.setSelectedAttribute(htmlOption, true);
                return;
            }
        }
        throw new UnsupportedOperationException("This page doesn't support that month/year combination");
    }

    private void addButtonToFormAndSubmit(HtmlPage calendarPage, HtmlForm form) throws IOException {
        HtmlElement button = (HtmlElement) calendarPage.createElement("button");
        button.setAttribute("type", "submit");
        form.appendChild(button);
        button.click();
    }

    private List<Auction> getAuctionsfromHtmlElements(List<HtmlElement> taxAuctionDates, AuctionType auctionType, County county){
        final List<Auction> auctions = new ArrayList<>();
        for(HtmlElement auctionDate : taxAuctionDates){
            String[] lines = auctionDate.getParentNode().asText().split("\\r?\\n");
            LocalDate date = LocalDate.parse(auctionDate.getParentNode().getParentNode().getAttributes().getNamedItem("dayid").getNodeValue(), dateFormatter);
            LocalTime time = LocalTime.parse(lines[2].trim().substring(0,8), timeFormatter);
            String strUrl = county.getUrl()+auctionUrlAppension+date.format(dateFormatter);
            Auction auction =  new Auction(auctionType, county, date, time, strUrl);
            auctions.add(auction);
        }
        return auctions;
    }

    private WebClient getRealTaxDeedWebClient(boolean useLogging){
        final WebClient webClient = new WebClient(browserVersion);
        webClient.setWebConnection(new FalsifyingWebConnection(webClient) {
            @Override
            public WebResponse getResponse(WebRequest webRequest) throws IOException {
                if (webRequest.getUrl().getPath().endsWith("jquery-1.6.1.min.js")) {
                    String jQuery = FileUtils.readFileToString(new File(this.getClass().getResource("/jquery-1.6.1.js").getPath()));
                    return createWebResponse(webRequest, jQuery, "application/javascript");
                }
                return super.getResponse(webRequest);
            }
        });
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        if(!useLogging) {
            LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
            java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
            java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
            webClient.setIncorrectnessListener(new IncorrectnessListener() {
                @Override
                public void notify(String arg0, Object arg1) {
                    // TODO Auto-generated method stub
                }
            });
            webClient.setCssErrorHandler(new CSSErrorHandler() {
                @Override
                public void warning(CSSParseException exception) throws CSSException {
                    // TODO Auto-generated method stub
                }
                @Override
                public void fatalError(CSSParseException exception) throws CSSException {
                    // TODO Auto-generated method stub
                }
                @Override
                public void error(CSSParseException exception) throws CSSException {
                    // TODO Auto-generated method stub
                }
            });
        }
        return webClient;
    }
}
