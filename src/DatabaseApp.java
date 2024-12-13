import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DatabaseApp {
    private JFrame frame;
    private JTextArea textArea;
    private Database database;
    private JTextField inputId, inputName, inputDate, inputStatus;

    public DatabaseApp() {
        database = new Database();
        initialize();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                DatabaseApp window = new DatabaseApp();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void initialize() {

        frame = new JFrame();
        frame.setBounds(100, 100, 1200, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        // Панель для кнопок, расположенных в 2 ряда
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 4, 10, 10)); // 2 ряда по 4 кнопки в каждом
        frame.getContentPane().add(BorderLayout.NORTH, panel);

        String[] columnNames = {"ID", "Название", "Дата", "Статус"};
        DefaultTableModel model = new DefaultTableModel();
        model.setColumnIdentifiers(columnNames);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(700, 300));
        frame.add(scrollPane, BorderLayout.EAST);

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane textScrollPane = new JScrollPane(textArea);
        textScrollPane.setPreferredSize(new Dimension(500, 150));
        frame.add(textScrollPane, BorderLayout.SOUTH);

        // Кнопки для взаимодействия
        addButton(panel, "Создать БД", e -> {
            database.createDatabase();
            textArea.append("База данных создана.\n");
        });
        addButton(panel, "Сохранить БД", e -> {
            database.saveCreate();
            textArea.append("Данные сохранены.\n");
        });
        addButton(panel, "Открыть БД", e -> {
            try {
                database.openDatabase();
                loadDataFromFile(model);
                textArea.append("База данных открыта.\n");
            } catch (IOException ex) {
                textArea.append("Ошибка при открытии БД: " + ex.getMessage() + "\n");
            }
        });
        addButton(panel, "Сохранить изменения", e -> {
            try {
                database.saveDatabase();
                loadDataFromFile(model);
                textArea.append("Данные сохранены.\n");
                database.createBackup();
                textArea.append("Бекап создан.\n");
            } catch (IOException ex) {
                textArea.append("Ошибка при сохранении данных: " + ex.getMessage() + "\n");
            }
        });
        addButton(panel, "Удалить БД", e -> {
            database.delete();
            loadDataFromFile(model);
            textArea.append("База данных удалена.\n");
        });
        addButton(panel, "Очистить БД", e -> {
            database.clearDatabase();
            loadDataFromFile(model);
            textArea.append("Содержимое базы данных очищено.\n");
        });
        addButton(panel, "Восстановить из Бекапа", e -> {
            database.restoreFromBackup();
            loadDataFromFile(model);
            textArea.append("База данных восстановлена из бекапа.\n");
        });

        // Панель для ввода данных новой записи
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(5, 2, 5, 5)); // Сетка для ввода данных
        frame.getContentPane().add(inputPanel, BorderLayout.CENTER);

        inputPanel.add(new JLabel("ID:"));
        inputId = new JTextField();
        inputId.setPreferredSize(new Dimension(150, 25)); // Устанавливаем размер поля ввода
        inputPanel.add(inputId);

        inputPanel.add(new JLabel("Название:"));
        inputName = new JTextField();
        inputName.setPreferredSize(new Dimension(150, 25)); // Устанавливаем размер поля ввода
        inputPanel.add(inputName);

        inputPanel.add(new JLabel("Дата (dd.MM.yyyy):"));
        inputDate = new JTextField();
        inputDate.setPreferredSize(new Dimension(150, 25)); // Устанавливаем размер поля ввода
        inputPanel.add(inputDate);

        inputPanel.add(new JLabel("Статус (true/false):"));
        inputStatus = new JTextField();
        inputStatus.setPreferredSize(new Dimension(150, 25)); // Устанавливаем размер поля ввода
        inputPanel.add(inputStatus);

        // Кнопка для добавления новой записи
        JButton btnAddRecord = new JButton("Добавить запись");
        btnAddRecord.addActionListener(e -> {
            try {
                int id = Integer.parseInt(inputId.getText());
                String name = inputName.getText();
                LocalDate date = LocalDate.parse(inputDate.getText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                boolean status = Boolean.parseBoolean(inputStatus.getText());
                Record record = new Record(id, name, date, status);
                if (!database.addNewRecord(record)) {
                    textArea.append("Ошибка при добавлении записи\n");
                }
                loadDataFromFile(model);
                // Очищаем текстовые поля после добавления
                inputId.setText("");
                inputName.setText("");
                inputDate.setText("");
                inputStatus.setText("");
            } catch (Exception ex) {
                textArea.append("Ошибка при добавлении записи: " + ex.getMessage() + "\n");
            }
        });
        inputPanel.add(btnAddRecord);

        JButton btnEditRecord = new JButton("Редактировать запись");
        btnEditRecord.addActionListener(e -> {
            try {
                int id = Integer.parseInt(inputId.getText());
                String name = inputName.getText();
                LocalDate date = LocalDate.parse(inputDate.getText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                boolean status = Boolean.parseBoolean(inputStatus.getText());
                Record updatedRecord = new Record(id, name, date, status);
                database.editData(id, updatedRecord); // Вызов метода редактирования данных
                loadDataFromFile(model);
                // Очищаем текстовые поля после добавления
                inputId.setText("");
                inputName.setText("");
                inputDate.setText("");
                inputStatus.setText("");
                textArea.append("Запись с ID " + id + " отредактирована: " + updatedRecord + "\n");
            } catch (Exception ex) {
                textArea.append("Ошибка при редактировании записи: " + ex.getMessage() + "\n");
            }
        });
        inputPanel.add(btnEditRecord);

        //кнопка поиска по ID
        JButton btnSearchById = new JButton("Поиск по ID");
        btnSearchById.addActionListener(e -> {
            try {
                int id = Integer.parseInt(JOptionPane.showInputDialog("Введите ID для поиска:"));
                String result = database.searchId(id);
                if (result != null) {
                    textArea.append("Результат поиска по ID: " + result + "\n");
                }
                else{
                    textArea.append("Запись не найдена.\n");
                }
            } catch (Exception ex) {
                textArea.append("Ошибка при поиске по ID: " + ex.getMessage() + "\n");
            }
        });
        panel.add(btnSearchById);

        //кнопка поиска по названию
        JButton btnSearchByName = new JButton("Поиск по названию");
        btnSearchByName.addActionListener(e -> {
            try {
                String name = JOptionPane.showInputDialog("Введите название для поиска:");
                List<String> result = new ArrayList<>();
                result = database.searchName(name);
                if (result != null) {
                    textArea.append("Результат поиска по названию:\n");
                    for (int i=0; i<result.size(); i++){
                        textArea.append(result.get(i)+"\n");
                    }
                }
                else{
                    textArea.append("Запись не найдена.\n");
                }
            } catch (Exception ex) {
                textArea.append("Ошибка при поиске по названию: " + ex.getMessage() + "\n");
            }
        });
        panel.add(btnSearchByName);

        //кнопка удаления по ID
        JButton btnRemoveById = new JButton("Удаление по ID");
        btnRemoveById.addActionListener(e -> {
            try {
                int id = Integer.parseInt(JOptionPane.showInputDialog("Введите ID для удаления:"));
                database.removeRecordById(id);
                loadDataFromFile(model);
                textArea.append("Запись по ID: " + id + " удалена.\n");
            } catch (Exception ex) {
                textArea.append("Ошибка при удалении по ID: " + ex.getMessage() + "\n");
            }
        });
        panel.add(btnRemoveById);

        //кнопка удаления по имени
        JButton btnRemoveByName = new JButton("Удаление по названию");
        btnRemoveByName.addActionListener(e -> {
            try {
                String name = JOptionPane.showInputDialog("Введите название для удаления:");
                database.removeRecordByName(name);
                loadDataFromFile(model);
                textArea.append("Запись по имени: " + name + " удалена.\n");
            } catch (Exception ex) {
                textArea.append("Ошибка при удалении по имени: " + ex.getMessage() + "\n");
            }
        });
        panel.add(btnRemoveByName);

        addButton(panel, "Сохранить в файл xlsx", e -> {
            try {
                String inputFilePath = "database.txt"; // Путь к вашему текстовому файлу
                String outputFilePath = "output.xlsx"; // Путь к выходному Excel файлу
                // Вызов метода записи в Excel
                TxtToExcel.writeToExcel(inputFilePath, outputFilePath);
            } catch (IOException ex) {
                textArea.append("Ошибка при сохранении данных: " + ex.getMessage() + "\n");
            }
        });
    }

    // Метод для добавления кнопки в панель
    private void addButton(JPanel panel, String text, ActionListener actionListener) {
        JButton button = new JButton(text);
        button.addActionListener(actionListener);
        panel.add(button);
    }
    File databasefile = new File("database.txt");
    public void loadDataFromFile(DefaultTableModel model) {
        model.setRowCount(0);
        try (BufferedReader br = new BufferedReader(new FileReader(databasefile.getName()))) {
            String line;
            int n=0;
            while ((line = br.readLine()) != null) {
                n+=1;
                String[] parts = line.split("\s{2,}");
                try {
                    if (parts.length==4) {
                        model.addRow(new Object[]{parts[0], parts[1], parts[2], parts[3]});
                    }

                } catch (IllegalArgumentException ex){
                    throw new IllegalArgumentException("Данные на строке " + n + " введены некорректно.");
                }
            }
        } catch (FileNotFoundException ex1) {
            textArea.append("Ошибка при чтении файла database.txt");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

