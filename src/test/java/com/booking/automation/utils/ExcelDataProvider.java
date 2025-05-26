package com.booking.automation.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.DataProvider;

/**
 * Utility class for reading test data from Excel files.
 * Provides data-driven testing capabilities by reading test data from Excel spreadsheets.
 * 
 * Features:
 * - Reads Excel (.xlsx) files using Apache POI
 * - Converts Excel data to TestNG data provider format
 * - Handles empty rows and cells
 * - Supports dynamic column headers
 * 
 * Expected Excel format:
 * - First row contains column headers
 * - Each subsequent row represents a test case
 * - Empty rows are automatically skipped
 */
public class ExcelDataProvider {

    /**
     * Reads test data from an Excel file and returns it as an array of Maps.
     * Each Map represents a row, with column headers as keys and cell values as String values.
     * Skips empty rows or rows with all empty cells.
     *
     * @param excelFilePath The path to the Excel file
     * @param sheetName The name of the sheet to read data from
     * @return Object[][] where each inner array contains a Map<String, String> representing a row of data
     * @throws IOException If there's an error reading the file
     * @throws IllegalArgumentException If the specified sheet is not found
     */
    @DataProvider(name = "excelData")
    public static Object[][] getTestData(String excelFilePath, String sheetName) throws IOException {
        File file = new File(excelFilePath);
        FileInputStream fis = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheet(sheetName);

        if (sheet == null) {
            workbook.close();
            fis.close();
            throw new IllegalArgumentException("Sheet with name '" + sheetName + "' not found in the workbook.");
        }

        int lastRowNum = sheet.getLastRowNum(); // Get last row number (0-based)
        int firstRowNum = sheet.getFirstRowNum(); // Get first row number (usually 0 for headers)

        if (lastRowNum < firstRowNum) {
            workbook.close();
            fis.close();
            return new Object[0][0]; // No data rows
        }

        XSSFRow headerRow = sheet.getRow(firstRowNum); // Get header row
        int lastColNum = headerRow.getLastCellNum(); // Get last column number (1-based)

        // Prepare a list to hold all data maps
        List<Map<String, String>> testDataList = new ArrayList<>();
        DataFormatter formatter = new DataFormatter(); // To format cell values as String

        // Iterate through rows, starting from the second row (data rows)
        for (int i = firstRowNum + 1; i <= lastRowNum; i++) {
            XSSFRow currentRow = sheet.getRow(i);
            if (currentRow == null || isRowEmpty(currentRow)) { // Skip empty rows
                System.out.println("Skipping row " + (i + 1) + " as it is empty");
                continue;
            }

            Map<String, String> rowData = new HashMap<>();
            boolean hasData = false; // To check if the row has any non-empty cells

            // Iterate through columns to get header and cell value
            for (int j = 0; j < lastColNum; j++) {
                XSSFCell headerCell = headerRow.getCell(j);
                XSSFCell dataCell = currentRow.getCell(j);

                String headerName = (headerCell == null) ? "" : formatter.formatCellValue(headerCell).trim();
                String cellValue = (dataCell == null) ? "" : formatter.formatCellValue(dataCell).trim();

                if (!headerName.isEmpty()) {
                    rowData.put(headerName, cellValue);
                    if (!cellValue.isEmpty()) {
                        hasData = true; // Mark row as having data if any cell is non-empty
                    }
                }
            }

            // Only add the row if it has at least one non-empty cell
            if (hasData) {
                testDataList.add(rowData);
            } else {
                System.out.println("Skipping row " + (i + 1) + " as all cells are empty");
            }
        }

        workbook.close();
        fis.close();

        // Convert list of maps to Object[][]
        Object[][] dataArray = new Object[testDataList.size()][1];
        for (int i = 0; i < testDataList.size(); i++) {
            dataArray[i][0] = testDataList.get(i);
        }
        return dataArray;
    }

    /**
     * Checks if a row is empty (all cells are null or empty strings).
     *
     * @param row The row to check
     * @return true if the row is empty, false otherwise
     */
    private static boolean isRowEmpty(XSSFRow row) {
        if (row == null) {
            return true;
        }
        DataFormatter formatter = new DataFormatter();
        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && !formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}