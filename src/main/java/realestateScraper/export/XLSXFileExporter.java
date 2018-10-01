package realestateScraper.export;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import realestateScraper.objects.Auction;
import realestateScraper.objects.AuctionListing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class XLSXFileExporter implements FileExporter {

    private XSSFCellStyle currencyStyle;
    private XSSFCellStyle hyperLinkStyle;
    private CreationHelper creationHelper;

    @Override
    public void export(String fileLocation, List<Auction> auctionList) throws IOException {
        File file = new File(fileLocation);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook();
        creationHelper = workbook.getCreationHelper();
        currencyStyle = getCurrencyCellStyle(workbook);
        hyperLinkStyle = getHyperLinkCellStyle(workbook);
        populateWorkbook(auctionList, workbook);
        autoSizeColumns(workbook, 2);
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        workbook.close();
    }

    private XSSFCellStyle getCurrencyCellStyle(XSSFWorkbook workbook){
        XSSFCellStyle styleCurrencyFormat = workbook.createCellStyle();
        styleCurrencyFormat.setDataFormat((short)8);//Accounting format
        return  styleCurrencyFormat;
    }

    private XSSFCellStyle getHyperLinkCellStyle(XSSFWorkbook workbook){
        XSSFCellStyle hyperLinkStyle = workbook.createCellStyle();
        workbook.createCellStyle();
        XSSFFont hyperLinkFont = workbook.createFont();
        hyperLinkFont.setUnderline(XSSFFont.U_SINGLE);
        hyperLinkFont.setColor(HSSFColor.BLUE.index);
        hyperLinkStyle.setFont(hyperLinkFont);
        return  hyperLinkStyle;
    }

    private void createCellHyperLink(Cell cell, String url, CreationHelper createHelper){
        XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
        link.setAddress(url);
        cell.setHyperlink(link);
    }

    private void populateWorkbook(List<Auction> auctionList, XSSFWorkbook workbook){
        for(Auction auction : auctionList){
            XSSFSheet sheet = setUpSheet(workbook, auction, creationHelper);
            int rowNumber = 3;
            for(AuctionListing auctionListing : auction.getAuctionListings()) {
                Row row = sheet.createRow(rowNumber++);
                row.createCell(0, CellType.STRING).setCellValue(auctionListing.getCaseNumber());
                row.createCell(1, CellType.STRING).setCellValue(auctionListing.getCertificateNumber());
                row.createCell(2, CellType.STRING).setCellValue(auctionListing.getParcelID());
                row.createCell(3, CellType.STRING).setCellValue(auctionListing.getPropertyAddress());
                createAndPopulateCurrencyCell(row, 4, auctionListing.getOpeningBid());
                createAndPopulateCurrencyCell(row, 5, auctionListing.getAssessedValue());
                if(auctionListing.getMlsListing()!=null) {
                    createAndPopulateCurrencyCell(row, 6, auctionListing.getMlsListing().getPriceEstimate());
                    createAndPopulateHyperLinkCell(row, 7, auctionListing.getMlsListing().getUrl(), "Zillow");
                }
                createAndPopulateHyperLinkCell(row, 8, auctionListing.getSearchEngineResultUrl(), "Google");
                createAndPopulateHyperLinkCell(row, 9, auctionListing.getParcelUrl(), auction.getCounty().getCountyName()+" Property Appraiser");
            }
        }
    }

    private void createAndPopulateCurrencyCell(Row row, int index, Float value){
        if(value != null) {
            Cell currencyCell = row.createCell(index, CellType.NUMERIC);
            currencyCell.setCellValue(value);
            currencyCell.setCellStyle(currencyStyle);
        }
    }

    private void createAndPopulateHyperLinkCell(Row row, int index, String url, String displayValue){
        if(url!=null && isUrlValid(url)){
            Cell hyperLinkCell = row.createCell(index, CellType.STRING);
            hyperLinkCell.setCellValue(displayValue);
            createCellHyperLink(hyperLinkCell, url, creationHelper);
            hyperLinkCell.setCellStyle(hyperLinkStyle);
        }
    }

    private String generateSheetName(Auction auction){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(auction.getCounty().getCountyName().replace(" County",""));
        stringBuilder.append("-");
        stringBuilder.append(auction.getTime().toString().replace(":",""));
        stringBuilder.append("-");
        stringBuilder.append(auction.getDate().toString().substring(5));
        return stringBuilder.toString();
    }

    private XSSFSheet setUpSheet(XSSFWorkbook workbook, Auction auction, CreationHelper creationHelper){
        XSSFSheet sheet = workbook.createSheet(
                generateSheetName(auction));
        //TODO: Make the columnHeaders here
        sheet.addMergedRegion(new CellRangeAddress(0,0,0,25));
        Cell auctionUrlCell = sheet.createRow(0).createCell(0);
        auctionUrlCell.setCellValue(auction.getUrl());
        createCellHyperLink(auctionUrlCell, auction.getUrl(), creationHelper);
        auctionUrlCell.setCellStyle(hyperLinkStyle);

        //Create table headers
        Row headerRow = sheet.createRow(2);
        headerRow.createCell(0, CellType.STRING).setCellValue("Case #");
        headerRow.createCell(1, CellType.STRING).setCellValue("Certificate #");
        headerRow.createCell(2, CellType.STRING).setCellValue("Parcel ID");
        headerRow.createCell(3, CellType.STRING).setCellValue("Address");
        headerRow.createCell(4, CellType.STRING).setCellValue("Opening Bid");
        headerRow.createCell(5, CellType.STRING).setCellValue("Assessed Value");
        headerRow.createCell(6, CellType.STRING).setCellValue("MLS Estimate");
        headerRow.createCell(7, CellType.STRING).setCellValue("MLS Link");
        headerRow.createCell(8, CellType.STRING).setCellValue("Search Engine");
        headerRow.createCell(9, CellType.STRING).setCellValue("Parcel Link");
        return sheet;
    }

    private void autoSizeColumns(Workbook workbook, int headerIndex) {
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            if (sheet.getPhysicalNumberOfRows() > 0) {
                Row row = sheet.getRow(headerIndex);
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    int columnIndex = cell.getColumnIndex();
                    sheet.autoSizeColumn(columnIndex);
                }
            }
        }
    }

    private boolean isUrlValid(String strUrl){
        URL url;
        try {
            url = new URL(strUrl);// this would check for the protocol
            url.toURI();
        } catch (MalformedURLException e) {
            System.out.println(strUrl + " can't convert malformed url");
            return false;
        } catch (URISyntaxException e) {
            System.out.println(strUrl + " can't convert url due to syntax");
            return false;
        }
        return true;
    }
}

