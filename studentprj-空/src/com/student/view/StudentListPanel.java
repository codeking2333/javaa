package com.student.view;

import com.student.util.Constant;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class StudentListPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    String[] headers = {"\u5E8F\u53F7", "\u59D3\u540D", "\u73ED\u7EA7", "\u5C0F\u7EC4", "\u7F3A\u52E4\u6B21\u6570", "\u8BF4\u5047\u6B21\u6570", "\u6210\u7EE9"};
    private final DefaultTableModel tableModel;
    private final JTable studentTable;
    private final JTextField txtId = new JTextField();
    private final JTextField txtName = new JTextField();
    private final JComboBox<String> cmbGroup = new JComboBox<>();
    private final JButton btnEdit = new JButton("修改");
    private final JButton btnDelete = new JButton("删除");
    private final JComboBox<String> classComboBox;
    private final JComboBox<String> groupComboBox;
    private final JRadioButton rbAll, rbClass, rbGroup;
    private final JTextField txtScore = new JTextField();

    public StudentListPanel() {
        this.setBorder(new TitledBorder(new EtchedBorder(), "学生列表"));
        this.setLayout(new BorderLayout());

        // 创建顶部过滤面板
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // 添加查看选项
        ButtonGroup viewGroup = new ButtonGroup();
        rbAll = new JRadioButton("查看所有学生", true);
        rbClass = new JRadioButton("按班级查看");
        rbGroup = new JRadioButton("按小组查看");
        viewGroup.add(rbAll);
        viewGroup.add(rbClass);
        viewGroup.add(rbGroup);
        
        // 添加班级和小组选择
        JLabel lblClass = new JLabel("选择班级：");
        classComboBox = new JComboBox<>();
        JLabel lblGroup = new JLabel("选择小组：");
        groupComboBox = new JComboBox<>();
        
        // 初始状态下禁用选择框
        classComboBox.setEnabled(false);
        groupComboBox.setEnabled(false);
        
        // 添加到过滤面板
        filterPanel.add(rbAll);
        filterPanel.add(rbClass);
        filterPanel.add(rbGroup);
        filterPanel.add(lblClass);
        filterPanel.add(classComboBox);
        filterPanel.add(lblGroup);
        filterPanel.add(groupComboBox);
        
        // 加载班级列表
        loadClasses();

        // 创建表格
        tableModel = new DefaultTableModel(null, headers);
        studentTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        studentTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(studentTable);

        // 创建底部编辑面板
        JPanel btnPanel = new JPanel();
        btnPanel.add(new JLabel("学号:"));
        btnPanel.add(txtId);
        txtId.setPreferredSize(new Dimension(100, 30));
        btnPanel.add(new JLabel("姓名:"));
        btnPanel.add(txtName);
        txtName.setPreferredSize(new Dimension(100, 30));
        btnPanel.add(new JLabel("小组:"));
        btnPanel.add(cmbGroup);
        cmbGroup.setPreferredSize(new Dimension(100, 30));
        btnPanel.add(new JLabel("成绩:"));
        btnPanel.add(txtScore);
        txtScore.setPreferredSize(new Dimension(100, 30));
        btnPanel.add(btnEdit);
        btnPanel.add(btnDelete);

        // 添加事件监听
        rbAll.addActionListener(e -> {
            classComboBox.setEnabled(false);
            groupComboBox.setEnabled(false);
            loadStudentData(null, null);
        });

        rbClass.addActionListener(e -> {
            classComboBox.setEnabled(true);
            groupComboBox.setEnabled(false);
            if (classComboBox.getSelectedItem() != null) {
                loadStudentData((String)classComboBox.getSelectedItem(), null);
            }
        });

        rbGroup.addActionListener(e -> {
            classComboBox.setEnabled(true);
            groupComboBox.setEnabled(true);
            
            // 如果没有选择班级，提示用户先选择班级
            if (classComboBox.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(this, "请先选择班级", "", JOptionPane.INFORMATION_MESSAGE);
                rbClass.setSelected(true); // 切换回按班级查看
                groupComboBox.setEnabled(false);
                return;
            }
            
            // 如果选择了班级但没有选择小组
            if (groupComboBox.getSelectedItem() == null) {
                loadGroups((String)classComboBox.getSelectedItem());
                return;
            }
            
            // 如果已经选择了班级和小组
            if (classComboBox.getSelectedItem() != null && groupComboBox.getSelectedItem() != null) {
                loadStudentData((String)classComboBox.getSelectedItem(), (String)groupComboBox.getSelectedItem());
            }
        });

        classComboBox.addActionListener(e -> {
            String selectedClass = (String) classComboBox.getSelectedItem();
            if (selectedClass != null) {
                loadGroups(selectedClass);
                if (rbClass.isSelected()) {
                    loadStudentData(selectedClass, null);
                } else if (rbGroup.isSelected()) {
                    // 当切换班级时，清空小组选择并重新加载小组列表
                    groupComboBox.removeAllItems();
                    loadGroups(selectedClass);
                    // 不立即加载学生数据，等待用户选择小组
                }
            }
        });

        groupComboBox.addActionListener(e -> {
            if (rbGroup.isSelected() && 
                classComboBox.getSelectedItem() != null && 
                groupComboBox.getSelectedItem() != null) {
                loadStudentData(
                    (String)classComboBox.getSelectedItem(), 
                    (String)groupComboBox.getSelectedItem()
                );
            }
        });

        // 初始加载所有学生数据
        loadStudentData(null, null);

        // 添加表格选择事件
        studentTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow >= 0) {
                txtId.setText((String) studentTable.getValueAt(selectedRow, 0));
                txtName.setText((String) studentTable.getValueAt(selectedRow, 1));
                String className = (String) studentTable.getValueAt(selectedRow, 2);
                txtScore.setText((String) studentTable.getValueAt(selectedRow, 6));
                
                // 加载该班级的小组列表
                cmbGroup.removeAllItems();
                File groupFile = new File("D:" + File.separator + "workspacemax" 
                    + File.separator + "java" 
                    + File.separator + "student" 
                    + File.separator + "group.txt");
                    
                if (groupFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(groupFile))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split(",");
                            if (parts.length >= 2 && parts[0].equals(className)) {
                                cmbGroup.addItem(parts[1]);
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                
                // 设置当前小组
                String currentGroup = (String) studentTable.getValueAt(selectedRow, 3);
                cmbGroup.setSelectedItem(currentGroup);
            }
        });

        // 修改按钮事件
        btnEdit.addActionListener(e -> {
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "请先选择学生", "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // 获取输入的值
            String studentId = txtId.getText().trim();
            String studentName = txtName.getText().trim();
            String className = (String) studentTable.getValueAt(selectedRow, 2);
            String newGroup = (String) cmbGroup.getSelectedItem();
            String scoreText = txtScore.getText().trim();

            // 验证输入
            if (studentId.isEmpty() || studentName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "学号和姓名不能为空", "", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (newGroup == null || newGroup.isEmpty()) {
                JOptionPane.showMessageDialog(this, "请选择小组", "", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 验证成绩输入
            int score = 0;
            if (!scoreText.isEmpty()) {
                try {
                    score = Integer.parseInt(scoreText);
                    if (score < 0 || score > 100) {
                        JOptionPane.showMessageDialog(this, "成绩必须在0-100之间", "", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "成绩必须是整数", "", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // 确认修改
            int confirm = JOptionPane.showConfirmDialog(this, 
                "确认要修改这个学生的信息吗？", 
                "确认修改", 
                JOptionPane.YES_NO_OPTION);
                
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                // 更新学生文件
                File studentFile = new File("D:" + File.separator + "workspacemax" 
                    + File.separator + "java" 
                    + File.separator + "student" 
                    + File.separator + "student.txt");
                    
                List<String> allLines = new ArrayList<>();
                boolean found = false;
                
                if (studentFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(studentFile))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split(",");
                            if (parts.length >= 4 && parts[0].equals(className) 
                                && parts[1].equals((String)studentTable.getValueAt(selectedRow, 0))
                                && parts[2].equals((String)studentTable.getValueAt(selectedRow, 1))) {
                                // 更新这一行的信息
                                allLines.add(String.join(",", className, studentId, studentName, newGroup));
                                found = true;
                            } else {
                                allLines.add(line);
                            }
                        }
                    }
                }

                if (!found) {
                    JOptionPane.showMessageDialog(this, "未找到要修改的学生记录", "", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 保存学生文件
                try (PrintWriter writer = new PrintWriter(new FileWriter(studentFile))) {
                    for (String line : allLines) {
                        writer.println(line);
                    }
                }

                // 更新成绩文件
                File scoreFile = new File("D:" + File.separator + "workspacemax" 
                    + File.separator + "java" 
                    + File.separator + "student" 
                    + File.separator + "score.txt");
                    
                Map<String, Integer> scores = new HashMap<>();
                if (scoreFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(scoreFile))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split(",");
                            if (parts.length >= 3) {
                                // 如果是当前学生，使用新的姓名
                                if (parts[0].equals(className) && 
                                    parts[1].equals((String)studentTable.getValueAt(selectedRow, 1))) {
                                    scores.put(className + "," + studentName, Integer.parseInt(parts[2]));
                                } else {
                                    scores.put(parts[0] + "," + parts[1], Integer.parseInt(parts[2]));
                                }
                            }
                        }
                    }
                }

                // 更新或添加新成绩
                if (!scoreText.isEmpty()) {
                    scores.put(className + "," + studentName, score);
                }

                // 保存成绩文件
                try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFile))) {
                    for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                        String[] parts = entry.getKey().split(",");
                        writer.println(parts[0] + "," + parts[1] + "," + entry.getValue());
                    }
                }

                // 更新表格显示
                studentTable.setValueAt(studentId, selectedRow, 0);
                studentTable.setValueAt(studentName, selectedRow, 1);
                studentTable.setValueAt(newGroup, selectedRow, 3);
                if (!scoreText.isEmpty()) {
                    studentTable.setValueAt(String.valueOf(score), selectedRow, 6);
                }

                JOptionPane.showMessageDialog(this, "修改成功", "", JOptionPane.INFORMATION_MESSAGE);

                // 刷新学生列表
                if (rbAll.isSelected()) {
                    loadStudentData(null, null);
                } else if (rbClass.isSelected()) {
                    loadStudentData((String)classComboBox.getSelectedItem(), null);
                } else if (rbGroup.isSelected()) {
                    loadStudentData((String)classComboBox.getSelectedItem(), 
                        (String)groupComboBox.getSelectedItem());
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "修改��败：" + ex.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // 添加删除按钮事件
        btnDelete.addActionListener(e -> {
            int selectedRow = studentTable.getSelectedRow();
            if (selectedRow < 0) {
                JOptionPane.showMessageDialog(this, "请先选择学生", "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, "删除学生会删除他的考勤、成绩等，确认删除？", "", JOptionPane.YES_NO_OPTION) != 0) {
                return;
            }

            String studentId = (String) studentTable.getValueAt(selectedRow, 0);
            String studentName = (String) studentTable.getValueAt(selectedRow, 1);
            String className = (String) studentTable.getValueAt(selectedRow, 2);
            
            try {
                // 删除学生数据
                File studentFile = new File("D:" + File.separator + "workspacemax" 
                    + File.separator + "java" 
                    + File.separator + "student" 
                    + File.separator + "student.txt");
                    
                List<String> remainingLines = new ArrayList<>();
                if (studentFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(studentFile))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split(",");
                            // 不保存要删除的学生数据
                            if (parts.length < 4 || !parts[0].equals(className) 
                                || !parts[1].equals(studentId) || !parts[2].equals(studentName)) {
                                remainingLines.add(line);
                            }
                        }
                    }
                }

                // 重写学生文件
                try (PrintWriter writer = new PrintWriter(new FileWriter(studentFile))) {
                    for (String line : remainingLines) {
                        writer.println(line);
                    }
                }

                // 删除考勤记录
                File attendanceFile = new File("D:" + File.separator + "workspacemax" 
                    + File.separator + "java" 
                    + File.separator + "student" 
                    + File.separator + "attendance.txt");
                    
                remainingLines.clear();
                if (attendanceFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(attendanceFile))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split(",");
                            // 不保存要删除的学生的考勤记录
                            if (parts.length < 4 || !parts[0].equals(className) 
                                || !parts[1].equals(studentName)) {
                                remainingLines.add(line);
                            }
                        }
                    }

                    // 重写考勤文件
                    try (PrintWriter writer = new PrintWriter(new FileWriter(attendanceFile))) {
                        for (String line : remainingLines) {
                            writer.println(line);
                        }
                    }
                }

                // 删除成绩记录
                File scoreFile = new File("D:" + File.separator + "workspacemax" 
                    + File.separator + "java" 
                    + File.separator + "student" 
                    + File.separator + "score.txt");
                    
                remainingLines.clear();
                if (scoreFile.exists()) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(scoreFile))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split(",");
                            // 不保存要删除的学生的成绩记录
                            if (parts.length < 3 || !parts[0].equals(className) 
                                || !parts[1].equals(studentName)) {
                                remainingLines.add(line);
                            }
                        }
                    }

                    // 重写成绩文件
                    try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFile))) {
                        for (String line : remainingLines) {
                            writer.println(line);
                        }
                    }
                }

                JOptionPane.showMessageDialog(this, "删除学生成功", "", JOptionPane.INFORMATION_MESSAGE);
                
                // 刷新学生列表
                if (rbAll.isSelected()) {
                    loadStudentData(null, null);
                } else if (rbClass.isSelected()) {
                    loadStudentData((String)classComboBox.getSelectedItem(), null);
                } else if (rbGroup.isSelected()) {
                    loadStudentData((String)classComboBox.getSelectedItem(), 
                        (String)groupComboBox.getSelectedItem());
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "删除学生失败：" + ex.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // 添加回车键监听器
        KeyAdapter enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnEdit.doClick(); // 触发修改按钮的点击事件
                }
            }
        };

        // 为所有输入框添加回车键监听
        txtId.addKeyListener(enterKeyListener);
        txtName.addKeyListener(enterKeyListener);
        txtScore.addKeyListener(enterKeyListener);
        cmbGroup.getEditor().getEditorComponent().addKeyListener(enterKeyListener);

        // 添加到主面板
        this.add(filterPanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadClasses() {
        classComboBox.removeAllItems();
        File classDir = new File(Constant.FILE_PATH);
        File[] classes = classDir.listFiles(File::isDirectory);
        if (classes != null) {
            for (File classFile : classes) {
                classComboBox.addItem(classFile.getName());
            }
        }
    }

    private void loadGroups(String className) {
        groupComboBox.removeAllItems();
        File groupFile = new File("D:" + File.separator + "workspacemax" 
            + File.separator + "java" 
            + File.separator + "student" 
            + File.separator + "group.txt");
            
        if (groupFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(groupFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2 && parts[0].equals(className)) {
                        groupComboBox.addItem(parts[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadStudentData(String className, String groupName) {
        // 清空表格数据
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }

        // 先加载考勤数据
        Map<String, int[]> attendanceData = new HashMap<>();
        File attendanceFile = new File("D:" + File.separator + "workspacemax" 
            + File.separator + "java" 
            + File.separator + "student" 
            + File.separator + "attendance.txt");
            
        // 加载成绩数据
        Map<String, Integer> scoreData = new HashMap<>();  // key: 班级,学生名 value: 成绩
        File scoreFile = new File("D:" + File.separator + "workspacemax" 
            + File.separator + "java" 
            + File.separator + "student" 
            + File.separator + "score.txt");
            
        // 读取成绩数据
        if (scoreFile.exists()) {
            try (BufferedReader scoreReader = new BufferedReader(new FileReader(scoreFile))) {
                String scoreLine;
                while ((scoreLine = scoreReader.readLine()) != null) {
                    String[] scoreParts = scoreLine.split(",");
                    if (scoreParts.length >= 3) {
                        String key = scoreParts[0] + "," + scoreParts[1];
                        scoreData.put(key, Integer.parseInt(scoreParts[2]));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
            
        // 读取考勤数据
        if (attendanceFile.exists()) {
            try (BufferedReader attendanceReader = new BufferedReader(new FileReader(attendanceFile))) {
                String attendanceLine;
                while ((attendanceLine = attendanceReader.readLine()) != null) {
                    String[] parts = attendanceLine.split(",");
                    if (parts.length >= 4) {
                        String key = parts[0] + "," + parts[1];
                        int[] counts = attendanceData.computeIfAbsent(key, k -> new int[2]);
                        if ("absence".equals(parts[2])) {
                            counts[0]++;
                        } else if ("leave".equals(parts[2])) {
                            counts[1]++;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 加载学生数据
        File studentFile = new File("D:" + File.separator + "workspacemax" 
            + File.separator + "java" 
            + File.separator + "student" 
            + File.separator + "student.txt");
            
        if (studentFile.exists()) {
            try (BufferedReader studentReader = new BufferedReader(new FileReader(studentFile))) {
                String studentLine;
                while ((studentLine = studentReader.readLine()) != null) {
                    String[] parts = studentLine.split(",");
                    if (parts.length >= 4) {
                        boolean shouldAdd = false;
                        
                        if (className == null) {
                            // 查看所有学生
                            shouldAdd = true;
                        } else if (groupName == null) {
                            // 按班级查看
                            shouldAdd = parts[0].equals(className);
                        } else {
                            // 按小组查看
                            shouldAdd = parts[0].equals(className) && parts[3].equals(groupName);
                        }
                        
                        if (shouldAdd) {
                            int[] counts = attendanceData.getOrDefault(parts[0] + "," + parts[2], new int[2]);
                            int score = scoreData.getOrDefault(parts[0] + "," + parts[2], 0);
                            
                            tableModel.addRow(new String[]{
                                parts[1], // 学号
                                parts[2], // 姓名
                                parts[0], // 班级
                                parts[3], // 小组
                                String.valueOf(counts[0]), // 缺勤次数
                                String.valueOf(counts[1]), // 请假次数
                                String.valueOf(score)  // 成绩
                            });
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
