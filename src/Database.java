import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database{
    private File databaseFile;
    private File recordsFile;
    private HashMap<Integer, Long> records = new HashMap<>();

    public Database(){
        databaseFile = new File("database.txt");
        recordsFile = new File("records.txt");
    }

    //создает базу данных
    public void createDatabase() {
        if (databaseFile.exists()) {
            delete(); // Удаление существующих файлов
        }
        try {
            // Создаем файл базы данных, если его нет
            if (databaseFile.createNewFile()) {
                System.out.println("Файл базы данных создан: " + databaseFile.getName());
            }
            if (recordsFile.createNewFile()) {
                System.out.println("Файл индекса создан: " + recordsFile.getName());
            }
        } catch (IOException e) {
            System.err.println("Ошибка при создании файла: " + e.getMessage());
        }
    }

    public void saveCreate() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(recordsFile))) {
            for (Map.Entry<Integer, Long> entry : records.entrySet()) {
                writer.write(entry.getKey() + "  " + entry.getValue() + "\n");
            }
            records.clear();
            System.out.println("Данные успешно сохранены в records.txt.");
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении файла индекса: " + e.getMessage());
        }
    }

    //открывает базу данных
    public void openDatabase() throws IOException{
        if (!databaseFile.exists()) {
            throw new FileNotFoundException("Файл database.txt не найден.");
        }
        if (!recordsFile.exists()) {
            throw new FileNotFoundException("Файл records.txt не найден.");
        }
        try (BufferedReader br = new BufferedReader(new FileReader(recordsFile.getName()))) {
            String line;
            int n=0;
            while ((line = br.readLine()) != null) {
                n+=1;
                String[] parts = line.split("\s{2,}");
                try {
                    int id = Integer.parseInt(parts[0]); // id
                    long value = Integer.parseInt(parts[1]);
                    records.put(id, value); // Добавление в хэш-таблицу
                } catch (IllegalArgumentException ex){
                    throw new IllegalArgumentException("Данные на строке " + n + " введены некорректно.");
                }
            }
        } catch (FileNotFoundException ex1) {
            throw new IOException("Ошибка при чтении файла records.txt", ex1);
        }
    }

    //удаляет базу данных
    public void delete(){
        String name = databaseFile.getName();
        if (Files.exists(Paths.get(name))){
            try{
                Files.delete(Paths.get(name));
                System.out.println("Файл: " + name + " удален.");
            } catch (IOException ex){
                System.err.println("Ошибка при удалении файла "+ name + " : " + ex.getMessage());
            }
        }
        records.clear();
        if (Files.exists(Paths.get("records.txt"))) {
            try {
                Files.delete(Paths.get("records.txt"));
                System.out.println("Файл: records.txt удален.");
            } catch (IOException ex) {
                System.err.println("Ошибка при удалении файла records.txt: " + ex.getMessage());
            }
        }
    }

    public void clearDatabase(){
        records.clear();
        try (FileWriter fileWriter = new FileWriter(databaseFile)) {
            fileWriter.write(" "); // Записываем пустую строку
            System.out.println("Содержимое файла database.txt было удалено.");
        } catch (IOException e) {
            System.err.println("Ошибка при удалении содержимого файла database.txt: " + e.getMessage());
        }

        try (FileWriter fileWriter = new FileWriter(recordsFile)) {
            fileWriter.write(" "); // Записываем пустую строку
            System.out.println("Содержимое файла records.txt было удалено.");
        } catch (IOException e) {
            System.err.println("Ошибка при удалении содержимого файла records.txt: " + e.getMessage());
        }
    }

    //сохраняет изменения в базе данных
    public void saveDatabase() throws IOException {
        try (FileWriter fileWriter = new FileWriter(recordsFile)) {
            fileWriter.write(" "); // Записываем пустую строку
        } catch (IOException e) {
            System.err.println("Ошибка при удалении содержимого файла records.txt: " + e.getMessage());
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(recordsFile))) {
            for (Map.Entry<Integer, Long> entry : records.entrySet()) {
                writer.write(entry.getKey() + "  " + entry.getValue() + "\n");
            }
            System.out.println("Данные успешно сохранены в records.txt.");
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении файла индекса: " + e.getMessage());
            throw e;
        }
    }

    //добавляет новую запись в бд с проверкой ключа на уникальность
    public boolean addNewRecord(Record record) {
        try (RandomAccessFile writer = new RandomAccessFile(databaseFile.getName(), "rw")) {
            long pos = writer.length(); // Позиция, на которую будем записывать
            if (!records.containsKey(record.GetId())) {
                writer.seek(pos); // Перемещаем указатель на конец файла
                String newData = record.toString();
                writer.writeBytes(newData + "\n");
                records.put(record.GetId(), pos);
                System.out.println("Новые данные добавлены");
                return true;
            } else {
                System.err.println("Элемент с таким ID уже существует");
                return false;
            }
        } catch (IOException e) {
            System.err.println("Ошибка при добавлении новой записи: " + e.getMessage());
            return false;
        }
    }

    //переводит строчку в record
    private Record toRecord (String str){
        String[] parts = str.split("\s{2,}");
        int n = parts.length;
        if (parts.length == 4) {
            int id;
            String name = parts[1];
            LocalDate date;
            boolean status;
            try {
                id = Integer.parseInt(parts[0]); // id
            } catch (NumberFormatException ex){
                throw new NumberFormatException("ID введен некорректно");
            }
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                date = LocalDate.parse(parts[2], formatter);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Дата введена некорректно");
            }
            if (parts[3].equalsIgnoreCase("true") || parts[3].equalsIgnoreCase("false")) {
                status = Boolean.parseBoolean(parts[3]);
            } else {
                throw new IllegalArgumentException("Статус введен некорректно");
            }
            Record rec = new Record(id,name,date,status);
            return rec;
        }
        else {
            throw new IllegalArgumentException("Данные введены некорректно."+ n);
        }
    }

    public void removeRecordById(int id) {
        if (records.containsKey(id)) {
            try (RandomAccessFile file = new RandomAccessFile(databaseFile.getName(), "rw")) {
                long pos = records.get(id); // Получаем позицию строки для удаления
                file.seek(pos);
                String line = file.readLine();
                // Перемещаем курсор в конец файла
                long length = file.length();
                byte[] restOfFile = new byte[(int)(length - (pos + line.length() + 1))];
                file.readFully(restOfFile);
                // Перемещаемся назад и записываем оставшиеся данные
                file.seek(pos);
                file.write(restOfFile);

                // Обрезаем файл до новой длины
                file.setLength(file.getFilePointer());
                records.remove(id);
                System.out.println("Элемент с ID " + id + " удален.");
            } catch (IOException e) {
                System.err.println("Ошибка при удалении записи: " + e.getMessage());
            }
        } else {
            System.out.println("Элемента с ID " + id + " не существует.");
        }
    }

    public void removeRecordByName(String name) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(databaseFile.getName(), "rw")) {
            String line;
            long pos;
            boolean found = false;

            while ((pos = file.getFilePointer()) < file.length()) {
                line = file.readLine();
                if (line == null) {
                    break; // Если строка пуста, выходим из цикла
                }
                Record rec = toRecord(line);
                if (rec.GetNameApp().equals(name)) {
                    found = true;
                    // Удаляем строку
                    long length = file.length();
                    byte[] restOfFile = new byte[(int) (length - (pos + line.length() + 1))];
                    file.readFully(restOfFile);

                    // Перемещаем указатель на место удаления
                    file.seek(pos);
                    file.write(restOfFile);
                    file.setLength(file.getFilePointer()); // Обновляем длину файла
                    records.remove(rec.GetId());
                    System.out.println("Элемент с name application: " + name + " удален.");

                    // Возвращаемся к началу, чтобы проверить оставшиеся строки
                    file.seek(pos); // Возвращаемся к позиции, где была удалена строка
                }
            }

            if (!found) {
                System.out.println("Элемент с name application: " + name + " не найден.");
            }
        } catch (IOException e) {
            System.err.println("Ошибка при удалении записи: " + e.getMessage());
        }
    }

    public String searchId(int id){
        if (records.containsKey(id)) {
            try (RandomAccessFile file = new RandomAccessFile(databaseFile.getName(), "r")) {
                long pos = records.get(id); // Получаем позицию строки
                file.seek(pos);
                String ans = file.readLine();
                return ans;
            } catch (IOException e) {
                System.err.println("Ошибка при поиске записи: " + e.getMessage());
                return null;
            }
        } else {
            System.out.println("Элемента с ID " + id + " не существует.");
            return null;
        }
    }

    public List<String> searchName(String name) {
        try (RandomAccessFile file = new RandomAccessFile(databaseFile.getName(), "r")) {
            String line;
            long pos;
            List<String> ans = new ArrayList<>();
            int c = 0;
            while ((pos = file.getFilePointer()) < file.length()) {
                line = file.readLine();
                if (line == null) {
                    break;
                }
                Record rec = toRecord(line); // Преобразуем строку в объект Record
                if (rec.GetNameApp().equals(name)) {
                    c += 1;
                    ans.add(line);
                }
            }
            if (c == 0) {
                System.out.println("Элемент с name application: " + name + " не найден.");
                return null;
            } else {
                return ans;
            }
        } catch (IOException e) {
            System.err.println("Ошибка при поиске записи: " + e.getMessage());
            return null;
        }
    }

    public void editData(int id, Record rec){
        if (records.containsKey(id)) {
            try (RandomAccessFile file = new RandomAccessFile(databaseFile.getName(), "rw")) {
                long pos = records.get(id); // Получаем позицию строки для удаления
                file.seek(pos);
                String line = file.readLine();
                String newData = rec.toString();
                int currentLineLength = line.length() + 1;
                int newLineLength = newData.length() + 1;

                // Если новая строка короче или равна текущей, просто перезаписываем
                if (newLineLength <= currentLineLength) {
                    file.seek(pos);
                    file.writeBytes(newData + "\n"); // Записываем новую строку
                    // Обрезаем файл, если новая строка короче
                    if (newLineLength < currentLineLength) {
                        long length = file.length();
                        long remainingBytes = length - (pos + currentLineLength);
                        byte[] restOfFile = new byte[(int) remainingBytes];
                        file.readFully(restOfFile);
                        file.setLength(pos + newLineLength); // Устанавливаем новую длину файла
                        file.write(restOfFile); // Записываем оставшиеся данные
                    }
                } else {
                    // Если новая строка длиннее, то нужно переместить оставшиеся данные
                    long length = file.length();
                    byte[] restOfFile = new byte[(int)(length - (pos + currentLineLength))];
                    file.readFully(restOfFile);
                    file.seek(pos);
                    file.writeBytes(newData + "\n"); // Записываем новую строку
                    file.write(restOfFile); // Записываем оставшиеся данные
                }
            } catch (IOException e) {
                System.err.println("Ошибка при редактировании записи: " + e.getMessage());
            }
        } else {
            System.out.println("Элемента с ID " + id + " не существует.");
        }
    }

    public void createBackup() {
        File backupDatabaseFile = new File("database_backup.txt");
        File backupRecordsFile = new File("records_backup.txt");

        try {
            Files.copy(databaseFile.toPath(), backupDatabaseFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(recordsFile.toPath(), backupRecordsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Резервная копия базы данных создана.");
        } catch (IOException e) {
            System.err.println("Ошибка при создании резервной копии: " + e.getMessage());
        }
    }

    // Восстановление базы данных из резервной копии
    public void restoreFromBackup() {
        records.clear();
        File backupDatabaseFile = new File("database_backup.txt");
        File backupRecordsFile = new File("records_backup.txt");

        try {
            if (backupDatabaseFile.exists() && backupRecordsFile.exists()) {
                Files.copy(backupDatabaseFile.toPath(), databaseFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(backupRecordsFile.toPath(), recordsFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                try (BufferedReader br = new BufferedReader(new FileReader(recordsFile.getName()))) {
                    String line;
                    int n=0;
                    while ((line = br.readLine()) != null) {
                        n+=1;
                        String[] parts = line.split("\s{2,}");
                        try {
                            int id = Integer.parseInt(parts[0]); // id
                            long value = Integer.parseInt(parts[1]);
                            records.put(id, value); // Добавление в хэш-таблицу
                        } catch (IllegalArgumentException ex){
                            throw new IllegalArgumentException("Данные на строке " + n + " введены некорректно.");
                        }
                    }
                } catch (FileNotFoundException ex1) {
                    throw new IOException("Ошибка при чтении файла records.txt", ex1);
                }
                System.out.println("База данных восстановлена из резервной копии.");
            } else {
                System.out.println("Резервные копии не найдены.");
            }
        } catch (IOException e) {
            System.err.println("Ошибка при восстановлении из резервной копии: " + e.getMessage());
        }
    }
}
