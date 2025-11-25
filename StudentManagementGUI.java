import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;


class Student implements Serializable {
    private String name;
    private int rollNumber;
    private String grade;

    public Student(String name, int rollNumber, String grade) {
        this.name = name;
        this.rollNumber = rollNumber;
        this.grade = grade;
    }

    public String getName() {
        return name;
    }
    public int getRollNumber() {
        return rollNumber;
    }
    public String getGrade() {
        return grade;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setGrade(String grade) {
        this.grade = grade;
    }

    @Override
    public String toString() {
        return "Name: " + name + " | Roll No: " + rollNumber + " | Grade: " + grade;
    }
}


class StudentManagementSystem {
    private ArrayList<Student> students;
    private final String FILE_NAME = "students_gui.dat";

    public StudentManagementSystem() {
        students = new ArrayList<>();
        loadData();
    }

    public ArrayList<Student> getStudents() {
        return students;
    }

    public boolean addStudent(Student s) {
        if (searchStudent(s.getRollNumber()) != null) return false;
        students.add(s);
        saveData();
        return true;
    }

    public boolean removeStudent(int rollNumber) {
        Student student = searchStudent(rollNumber);
        if (student != null) {
            students.remove(student);
            saveData();
            return true;
        }
        return false;
    }

    public Student searchStudent(int rollNumber) {
        for (Student s : students) {
            if (s.getRollNumber() == rollNumber)
                return s;
        }
        return null;
    }

    public boolean updateStudent(int rollNumber, String newName, String newGrade) {
        Student s = searchStudent(rollNumber);
        if (s != null) {
            s.setName(newName);
            s.setGrade(newGrade);
            saveData();
            return true;
        }
        return false;
    }

    private void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            out.writeObject(students);
        } catch (Exception e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            students = (ArrayList<Student>) in.readObject();
        } catch (FileNotFoundException fnf) {
            students = new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
            students = new ArrayList<>();
        }
    }
}


public class StudentManagementGUI extends JFrame {
    private StudentManagementSystem sms;
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField nameField;
    private JTextField rollField;
    private JTextField gradeField;

    public StudentManagementGUI() {
        sms = new StudentManagementSystem();
        setTitle("Student Management System - GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Top panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Student Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Roll No:"), gbc);
        gbc.gridx = 1; rollField = new JTextField(10);
        formPanel.add(rollField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Grade:"), gbc);
        gbc.gridx = 1; gradeField = new JTextField(5);
        formPanel.add(gradeField, gbc);

        // Buttons
        JButton addBtn = new JButton("Add");
        JButton updateBtn = new JButton("Update");
        JButton removeBtn = new JButton("Remove");
        JButton searchBtn = new JButton("Search");
        JButton clearBtn = new JButton("Clear Fields");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(removeBtn);
        btnPanel.add(searchBtn);
        btnPanel.add(clearBtn);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.NORTH);

        // Center panel
        String[] columns = {"Roll No", "Name", "Grade"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Student Records"));
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
        bottomPanel.add(new JLabel("Enter Roll No to quick search:"));
        JTextField quickSearchField = new JTextField(10);
        JButton quickSearchBtn = new JButton("Go");
        bottomPanel.add(quickSearchField);
        bottomPanel.add(quickSearchBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshTable();

        // Action listeners
        addBtn.addActionListener(e -> addStudentAction());
        removeBtn.addActionListener(e -> removeStudentAction());
        updateBtn.addActionListener(e -> updateStudentAction());
        searchBtn.addActionListener(e -> searchStudentAction());
        clearBtn.addActionListener(e -> clearFields());

        quickSearchBtn.addActionListener(e -> {
            String input = quickSearchField.getText().trim();
            if (input.isEmpty())
                return;
            try {
                int roll = Integer.parseInt(input);
                Student s = sms.searchStudent(roll);
                if (s != null) {
                    selectRowByRoll(roll);
                    nameField.setText(s.getName());
                    rollField.setText(String.valueOf(s.getRollNumber()));
                    gradeField.setText(s.getGrade());
                } else {
                    JOptionPane.showMessageDialog(this, "Student not found", "Search", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid roll number", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0) {
                        rollField.setText(tableModel.getValueAt(row, 0).toString());
                        nameField.setText(tableModel.getValueAt(row, 1).toString());
                        gradeField.setText(tableModel.getValueAt(row, 2).toString());
                    }
                }
            }
        });

        table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteRow");
        table.getActionMap().put("deleteRow", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    int confirm = JOptionPane.showConfirmDialog(StudentManagementGUI.this, "Delete selected student?", "Confirm", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        int roll = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
                        sms.removeStudent(roll);
                        refreshTable();
                    }
                }
            }
        });

        setVisible(true);
    }

    private void addStudentAction() {
        String name = nameField.getText().trim();
        String rollText = rollField.getText().trim();
        String grade = gradeField.getText().trim();

        if (name.isEmpty() || rollText.isEmpty() || grade.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int roll;
        try {
            roll = Integer.parseInt(rollText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Roll number must be numeric", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Student s = new Student(name, roll, grade);
        boolean ok = sms.addStudent(s);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Roll number already exists", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        refreshTable();
        clearFields();
    }

    private void removeStudentAction() {
        String rollText = rollField.getText().trim();
        if (rollText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter roll number to remove", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            int roll = Integer.parseInt(rollText);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to remove roll " + roll + "?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                boolean removed = sms.removeStudent(roll);
                if (removed) {
                    refreshTable();
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Student not found", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid roll number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStudentAction() {
        String name = nameField.getText().trim();
        String rollText = rollField.getText().trim();
        String grade = gradeField.getText().trim();

        if (name.isEmpty() || rollText.isEmpty() || grade.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int roll = Integer.parseInt(rollText);
            boolean updated = sms.updateStudent(roll, name, grade);
            if (updated) {
                refreshTable();
                clearFields();
            } else {
                JOptionPane.showMessageDialog(this, "Student not found", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid roll number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchStudentAction() {
        String rollText = rollField.getText().trim();
        if (rollText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter roll number to search", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            int roll = Integer.parseInt(rollText);
            Student s = sms.searchStudent(roll);
            if (s != null) {
                JOptionPane.showMessageDialog(this, s.toString(), "Found", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Student not found", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid roll number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        nameField.setText("");
        rollField.setText("");
        gradeField.setText("");
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Student s : sms.getStudents()) {
            Object[] row = { s.getRollNumber(), s.getName(), s.getGrade() };
            tableModel.addRow(row);
        }
    }

    private void selectRowByRoll(int roll) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            int r = Integer.parseInt(tableModel.getValueAt(i, 0).toString());
            if (r == roll) {
                table.setRowSelectionInterval(i, i);
                Rectangle rect = table.getCellRect(i, 0, true);
                table.scrollRectToVisible(rect);
                return;
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new StudentManagementGUI());
    }
}
