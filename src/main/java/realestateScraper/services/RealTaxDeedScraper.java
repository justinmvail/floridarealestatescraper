package realestateScraper.services;

import com.gargoylesoftware.css.parser.CSSErrorHandler;
import com.gargoylesoftware.css.parser.CSSException;
import com.gargoylesoftware.css.parser.CSSParseException;
import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.FalsifyingWebConnection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;
import realestateScraper.DomainObjects.Auction;
import realestateScraper.DomainObjects.AuctionListing;
import realestateScraper.DomainObjects.AuctionType;
import realestateScraper.DomainObjects.County;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class RealTaxDeedScraper implements TaxAuctionService{

    private final BrowserVersion browserVersion = BrowserVersion.CHROME;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
    private final boolean useFrameworkLogging;

    public RealTaxDeedScraper(boolean useFrameworkLogging){
        this.useFrameworkLogging = useFrameworkLogging;
    }

    @Override
    public List<Auction> getAllAuctionDatesByMonth(AuctionType auctionType, County county, LocalDate startingDate) throws IOException, InterruptedException {
        WebClient webClient = getRealTaxDeedWebClient(useFrameworkLogging);
        String calendarUrlAppension = "/index.cfm?zaction=USER&zmethod=CALENDAR";
        HtmlPage calendarPage = webClient.getPage(county.getUrl()+ calendarUrlAppension);
        String monthSelectorXpath = "//*[@id=\"selCalDate\"]";
        HtmlSelect monthSelector = calendarPage.getFirstByXPath(monthSelectorXpath);
        List<HtmlOption> monthYearOptions = monthSelector.getOptions();
        selectMonth(monthSelector, monthYearOptions, startingDate);
        HtmlForm form = calendarPage.getFormByName("monthChoose");
        calendarPage = addButtonToFormAndSubmit(calendarPage, form);
        String auctionDateXpathWithAuctionTypePlaceHolder = "//*[text()='%s']";
        List<HtmlElement> taxAuctionDates = calendarPage.getByXPath(String.format(auctionDateXpathWithAuctionTypePlaceHolder, auctionType.getDisplayName()));
        taxAuctionDates.removeIf((HtmlElement auctionDate) -> !(auctionDate instanceof HtmlBold));
        return getAuctionsfromHtmlElements(taxAuctionDates, auctionType, county);
    }

    @Override
    public List<Auction> getAllAuctionDatesByWeek(AuctionType auctionType, County county, LocalDate startingDate) throws IOException, InterruptedException {
        //TODO: improve efficiency by not loading (an) entire month(s) for one week
        LocalDate endingDate = startingDate.plusDays(7);
        List<Auction> auctionList;
        auctionList = getAllAuctionDatesByMonth(auctionType, county, startingDate);
        if(!startingDate.getMonth().equals(endingDate.getMonth())){
            auctionList.addAll(getAllAuctionDatesByMonth(auctionType, county, endingDate));
        }
        auctionList.removeIf((Auction auction) -> (auction.getDate().isBefore(startingDate)||auction.getDate().isAfter(endingDate)));
        return auctionList;
    }

    @Override
    public Auction getAuctionByDate(AuctionType auctionType, County county, LocalDate date) throws IOException, InterruptedException {
        //TODO: improve efficiency by not loading an entire month for one day
        List<Auction> auctionList = getAllAuctionDatesByMonth(auctionType, county, date);
        for(Auction auction : auctionList){
            if(auction.getDate().equals(date)){
                return auction;
            }
        }
        return null;
    }

    @Override
    public List<Auction> getAllAuctionDatesForRange(AuctionType auctionType, County county, LocalDate inclusiveStartingDate, LocalDate exclusiveEndingDate) {
        return null;
    }

    @Override
    public List<AuctionListing> getAuctionListings(Auction auction) throws IOException {
        WebClient webClient = getRealTaxDeedWebClient(useFrameworkLogging);
        HtmlPage auctionListingPage = webClient.getPage(auction.getUrl());
        List<HtmlDivision> auctionListingDivMatches = auctionListingPage.getByXPath("//div[contains(@class, 'Head_W')]");
        String divisionString = auctionListingDivMatches.get(0).asText();
        //The division string is equal to 1\r\n1 when there are no upcoming auctions - return the empty list.
        if(divisionString.equals("1\r\n1")) return new ArrayList<>();
        int totalPages = Integer.parseInt(divisionString.substring(30, 31));
        return loopOverPagesOfAuctionListings(totalPages, auctionListingPage);
    }

    private List<AuctionListing> loopOverPagesOfAuctionListings(int totalPages, HtmlPage auctionListingPage) throws IOException {
        List<AuctionListing> allUpcomingAuctionListings = new ArrayList<>();
        int currentPage = 1;
        while(currentPage<=totalPages) {
            HtmlDivision upcomingListingsDivision = (HtmlDivision) auctionListingPage.getElementById("Area_W");
            List<HtmlDivision> auctionListingDivs = upcomingListingsDivision.getByXPath("//div[contains(@class, 'AUCTION_DETAILS')]");
            ensureAuctionPageIsDoneLoading(auctionListingDivs);
            allUpcomingAuctionListings.addAll(scrapeUpcomingAuctionListings(auctionListingDivs));
            HtmlSpan nextPageSpan = (HtmlSpan) auctionListingPage.getByXPath("//*[@id=\"BID_WINDOW_CONTAINER\"]/div[3]/div[3]/span[3]").get(0);
            auctionListingPage = nextPageSpan.click();
            currentPage++;
        }
        return allUpcomingAuctionListings;
    }

    private void ensureAuctionPageIsDoneLoading(List<HtmlDivision> htmlDivisions){
        //This method continually checks to see if the page is done loading, once it is, it returns
        while(true){
            boolean allDivsLoaded = true;
            for(HtmlDivision htmlDivision : htmlDivisions){
                if(htmlDivision.getParentNode().getFirstChild().asText().equals("")) allDivsLoaded = false;
            }
            if(allDivsLoaded) return;
        }
    }

    private List<AuctionListing> scrapeUpcomingAuctionListings(List<HtmlDivision> auctionListingDivs) {
        List<AuctionListing> listings = new ArrayList<>();
        for(HtmlDivision div : auctionListingDivs) {
            String header = div.getParentNode().getFirstChild().asText();
            if(!header.startsWith("Auction Starts")) {
                //Get rid of rogue divs
                if (div.getFirstChild() == null || div.getFirstChild().getFirstChild() == null || div.getFirstChild().getFirstChild().getChildNodes() == null)
                    continue;
                //if this value is too short to be a date, ignore it
                if (header.length() < 10)
                    continue;
                //If this doesn't start with a number, ignore it.
                if (!StringUtils.isNumeric(header.substring(0, 2)))
                    continue;
            }
            DomNodeList<DomNode> domNodeList = div.getFirstChild().getFirstChild().getChildNodes();
            listings.add(scrapeSingleAuctionListing(domNodeList));
        }
        return listings;
    }

    private AuctionListing scrapeSingleAuctionListing(DomNodeList<DomNode> domNodeList){
        AuctionListing auctionListing = new AuctionListing();
        for (DomNode domNode : domNodeList) {
            //get rid of erroneous matches... we only want HtmlRows
            if (!(domNode instanceof HtmlTableRow)) continue;
            HtmlTableRow htmlTableRow = (HtmlTableRow) domNode;
            List<HtmlTableCell> cells = htmlTableRow.getCells();
            String cellLabel = cells.get(0).asText();
            if (cellLabel.equals("Auction Type:")) {
                auctionListing.setAuctionType(AuctionType.valueOf(cells.get(1).asText().replace(" ", "")));
            } else if (cellLabel.equals("Case #:")) {
                auctionListing.setCaseNumber(cells.get(1).asText());
            } else if (cellLabel.equals("Certificate #:")) {
                auctionListing.setCertificateNumber(cells.get(1).asText());
            } else if (cellLabel.equals("Opening Bid:")) {
                auctionListing.setOpeningBid(Float.parseFloat(cells.get(1).asText().replace("$", "").replace(",", "")));
            } else if (cellLabel.equals("Parcel ID:")) {
                auctionListing.setParcelID(cells.get(1).asText());
            } else if (cellLabel.equals("Property Address:")) {
                auctionListing.setPropertyAddress(cells.get(1).asText());
            } else if (cellLabel.equals("")) {
                auctionListing.setPropertyAddress(auctionListing.getPropertyAddress() + " " + cells.get(1).asText());
            } else if (cellLabel.equals("Assessed Value:")) {
                auctionListing.setAssessedValue(Float.parseFloat(cells.get(1).asText().replace("$", "").replace(",", "")));
            }
        }
        return auctionListing;
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

    private HtmlPage addButtonToFormAndSubmit(HtmlPage calendarPage, HtmlForm form) throws IOException {
        HtmlElement button = (HtmlElement) calendarPage.createElement("button");
        button.setAttribute("type", "submit");
        form.appendChild(button);
        return button.click();
    }

    private List<Auction> getAuctionsfromHtmlElements(List<HtmlElement> taxAuctionDates, AuctionType auctionType, County county){
        final List<Auction> auctions = new ArrayList<>();
        for(HtmlElement auctionDate : taxAuctionDates){
            String[] lines = auctionDate.getParentNode().asText().split("\\r?\\n");
            LocalDate date = LocalDate.parse(auctionDate.getParentNode().getParentNode().getAttributes().getNamedItem("dayid").getNodeValue(), dateFormatter);
            LocalTime time = LocalTime.parse(lines[2].trim().substring(0,8), timeFormatter);
            String auctionUrlFragment = "/index.cfm?zaction=AUCTION&Zmethod=PREVIEW&AUCTIONDATE=";
            String strUrl = county.getUrl()+ auctionUrlFragment +date.format(dateFormatter);
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
        //TODO: Is the following really necessary? can I this use NicelyResynchronizingAjaxController instead?
        //This should make all AJAX calls completely synchronous
        webClient.setAjaxController(new AjaxController(){
            @Override
            public boolean processSynchron(HtmlPage page, WebRequest request, boolean async){
                return true;
            }
        });
        if(!useLogging) {
            LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
            java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
            java.util.logging.Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);

            webClient.setIncorrectnessListener((arg0, arg1) -> {
                // intentionally left blank to override default functionality with nothing
            });
            webClient.setCssErrorHandler(new CSSErrorHandler() {
                @Override
                public void warning(CSSParseException exception) throws CSSException {
                    // intentionally left blank to override default functionality with nothing
                }
                @Override
                public void fatalError(CSSParseException exception) throws CSSException {
                    // intentionally left blank to override default functionality with nothing
                }
                @Override
                public void error(CSSParseException exception) throws CSSException {
                    // intentionally left blank to override default functionality with nothing
                }
            });
        }
        return webClient;
    }
}
