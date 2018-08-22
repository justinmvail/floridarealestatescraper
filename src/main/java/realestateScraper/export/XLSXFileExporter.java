package realestateScraper.export;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFHyperlink;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import realestateScraper.objects.Auction;
import realestateScraper.objects.AuctionListing;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class XLSXFileExporter implements FileExporter {
    @Override
    public void export(String fileLocation, List<Auction> auctionList) throws IOException {
        File file = new File(fileLocation);
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook();
        CreationHelper createHelper = workbook.getCreationHelper();

        XSSFCellStyle styleCurrencyFormat = workbook.createCellStyle();
        styleCurrencyFormat.setDataFormat((short)8);//Accounting format

        XSSFCellStyle hyperLinkStyle = workbook.createCellStyle();
        XSSFFont hyperLinkFont = workbook.createFont();
        hyperLinkFont.setUnderline(XSSFFont.U_SINGLE);
        hyperLinkFont.setColor(HSSFColor.BLUE.index);
        hyperLinkStyle.setFont(hyperLinkFont);


        for(Auction auction : auctionList){
            XSSFSheet sheet = workbook.createSheet(
                    auction.getCounty().getCountyName().replace(" County","")
                            + "-" + auction.getTime().toString().replace(":",""));
            //TODO: Make the columnHeaders here
            sheet.addMergedRegion(new CellRangeAddress(0,0,0,25));
            Cell auctionUrlCell = sheet.createRow(0).createCell(0);
            auctionUrlCell.setCellValue(auction.getUrl());
            createCellHyperLink(auctionUrlCell, auction.getUrl(), createHelper);
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

            int rowNumber = 3;
            for(AuctionListing auctionListing : auction.getAuctionListings()) {
                Row row = sheet.createRow(rowNumber++);
                row.createCell(0, CellType.STRING).setCellValue(auctionListing.getCaseNumber());
                row.createCell(1, CellType.STRING).setCellValue(auctionListing.getCertificateNumber());
                row.createCell(2, CellType.STRING).setCellValue(auctionListing.getParcelID());
                row.createCell(3, CellType.STRING).setCellValue(auctionListing.getPropertyAddress());
                Cell openingBid = row.createCell(4, CellType.NUMERIC);
                if (auctionListing.getOpeningBid()!=null) openingBid.setCellValue(auctionListing.getOpeningBid());
                openingBid.setCellStyle(styleCurrencyFormat);
                Cell assessedValue = row.createCell(5, CellType.NUMERIC);
                if(auctionListing.getAssessedValue()!=null)assessedValue.setCellValue(auctionListing.getAssessedValue());
                assessedValue.setCellStyle(styleCurrencyFormat);
                if(auctionListing.getMlsListing()!=null) {
                    Cell mlsEstimate = row.createCell(6, CellType.NUMERIC);
                    if(auctionListing.getMlsListing().getPriceEstimate()!=null)mlsEstimate.setCellValue(auctionListing.getMlsListing().getPriceEstimate());
                    mlsEstimate.setCellStyle(styleCurrencyFormat);
                    if(auctionListing.getMlsListing().getUrl()!=null) {
                        Cell mlsUrlCell = row.createCell(7, CellType.STRING);
                        //TODO: should not be hardcoded to Google
                        mlsUrlCell.setCellValue("Zillow");
                        createCellHyperLink(mlsUrlCell, auctionListing.getMlsListing().getUrl(), createHelper);
                        mlsUrlCell.setCellStyle(hyperLinkStyle);
                    }
                }
                if(auctionListing.getSearchEngineResultUrl()!=null) {
                    Cell searchEngineUrlCell = row.createCell(8, CellType.STRING);
                    //TODO: should not be hardcoded to Google
                    searchEngineUrlCell.setCellValue("Google");
                    createCellHyperLink(searchEngineUrlCell, auctionListing.getSearchEngineResultUrl(), createHelper);
                    searchEngineUrlCell.setCellStyle(hyperLinkStyle);
                }
                if(auctionListing.getParcelUrl()!=null) {
                    Cell searchEngineUrlCell = row.createCell(9, CellType.STRING);
                    searchEngineUrlCell.setCellValue("Parcel Info");
                    createCellHyperLink(searchEngineUrlCell, auctionListing.getParcelUrl(), createHelper);
                    searchEngineUrlCell.setCellStyle(hyperLinkStyle);
                }
            }
        }
        autoSizeColumns(workbook, 2);
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        workbook.close();
    }

    private void createCellHyperLink(Cell cell, String url, CreationHelper createHelper){
        XSSFHyperlink link = (XSSFHyperlink) createHelper.createHyperlink(HyperlinkType.URL);
        link.setAddress(url);
        cell.setHyperlink(link);
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
}
