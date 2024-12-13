import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TxtToExcel {
    // Метод для записи данных в Excel
    public static void writeToExcel(String inputFilePath, String outputFilePath) throws IOException {
        Workbook workbook = new XSSFWorkbook(); // Создаем новый Excel файл
        Sheet sheet = workbook.createSheet("Sheet1");
        try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath))) {
            String line;
            int rowNum = 0;
            // Чтение строк из файла и добавление в Excel
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("  ");
                if (tokens.length == 4) {
                    Row row = sheet.createRow(rowNum++);
                    createCell(row, 0, tokens[0]); // ID
                    createCell(row, 1, tokens[1]); // Name
                    createCell(row, 2, parseDate(tokens[2])); // Date
                    createCell(row, 3, Boolean.parseBoolean(tokens[3])); // Boolean value
                }
            }
        }
        // Запись в файл Excel
        try (FileOutputStream fileOut = new FileOutputStream(outputFilePath)) {
            workbook.write(fileOut);
        }
        System.out.println("Файл успешно создан!");
    }
    // Статический метод для создания ячеек с учетом разных типов данных
    private static void createCell(Row row, int column, Object value) {
        Cell cell = row.createCell(column);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof LocalDate) {
            cell.setCellValue(((LocalDate) value).toString());
        }
    }
    // Метод для преобразования строки в объект LocalDate
    private static LocalDate parseDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(dateString, formatter);
    }

}
