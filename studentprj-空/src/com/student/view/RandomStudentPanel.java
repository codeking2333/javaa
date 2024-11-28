package com.student.view;

import com.student.util.Constant;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class RandomStudentPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JLabel lbl2 = new JLabel("学生姓名：");
    private final JLabel lbl3 = new JLabel("学生照片：");
    private final JLabel lblPic = new JLabel("照片");
    private final JTextField txtStudent = new JTextField();
    private final JButton btnChooseStudent = new JButton("随机学生");
    private final JButton btnAbsence = new JButton("缺勤");
    private final JButton btnLeave = new JButton("请假");
    private final JButton btnAnswer = new JButton("答题");
    private final JButton btnCorrect = new JButton("回答正确");
    private final JLabel lblScore = new JLabel("当前成绩：0");
    private final JComboBox<String> classComboBox;
    private final JComboBox<String> groupComboBox;
    private Thread threadStudent = null;   // 随机抽取学生的线程
    private final List<String> studentList = new ArrayList<>();  // 存储当前可选的学生列表
    private final Random random = new Random();

    public RandomStudentPanel() {
        this.setBorder(new TitledBorder(new EtchedBorder(), "随机学生点名"));
        this.setLayout(null);

        // 添加班级和小组选择
        JLabel lblClass = new JLabel("选择班级：");
        classComboBox = new JComboBox<>();
        classComboBox.addItem("全部班级");
        loadClasses();

        JLabel lblGroup = new JLabel("选择小组：");
        groupComboBox = new JComboBox<>();
        groupComboBox.addItem("全部小组");
        groupComboBox.setEnabled(false);

        this.add(lblClass);
        this.add(classComboBox);
        this.add(lblGroup);
        this.add(groupComboBox);
        this.add(lbl2);
        this.add(lbl3);
        this.add(txtStudent);
        this.add(lblPic);
        this.add(btnChooseStudent);
        this.add(btnAbsence);
        this.add(btnLeave);
        this.add(btnAnswer);
        this.add(btnCorrect);
        this.add(lblScore);

        // 设置组件位置
        lblClass.setBounds(50, 30, 100, 30);
        classComboBox.setBounds(50, 60, 150, 30);
        lblGroup.setBounds(220, 30, 100, 30);
        groupComboBox.setBounds(220, 60, 150, 30);

        lbl2.setBounds(160, 100, 100, 30);
        txtStudent.setBounds(160, 140, 130, 30);
        txtStudent.setEditable(false);
        lblPic.setBounds(160, 180, 130, 150);
        btnChooseStudent.setBounds(160, 340, 130, 30);
        btnAbsence.setBounds(160, 380, 60, 30);
        btnLeave.setBounds(230, 380, 60, 30);
        btnAnswer.setBounds(300, 380, 60, 30);
        btnCorrect.setBounds(370, 380, 90, 30);
        lblScore.setBounds(160, 420, 200, 30);

        // 班级选择事件
        classComboBox.addActionListener(e -> {
            String selectedClass = (String) classComboBox.getSelectedItem();
            if (selectedClass != null) {
                if ("全部班级".equals(selectedClass)) {
                    groupComboBox.setEnabled(false);
                    groupComboBox.removeAllItems();
                    groupComboBox.addItem("全部小组");
                    loadAllStudents();
                } else {
                    groupComboBox.setEnabled(true);
                    loadGroups(selectedClass);
                    loadStudents(selectedClass, null);
                }
            }
        });

        // 小组选择事件
        groupComboBox.addActionListener(e -> {
            String selectedClass = (String) classComboBox.getSelectedItem();
            String selectedGroup = (String) groupComboBox.getSelectedItem();
            if (selectedClass != null && selectedGroup != null && !"全部小组".equals(selectedGroup)) {
                loadStudents(selectedClass, selectedGroup);
            } else if (selectedClass != null && !"全部班级".equals(selectedClass)) {
                loadStudents(selectedClass, null);
            }
        });

        // 初始载所有学生
        loadAllStudents();

        // 随机学生按钮事件
        btnChooseStudent.addActionListener(e -> {
            if (studentList.isEmpty()) {
                JOptionPane.showMessageDialog(this, "当前没有可选的学生", "", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (e.getActionCommand().equals("停")) {
                if (threadStudent != null) {
                    threadStudent.interrupt();
                    threadStudent = null;
                }
                btnChooseStudent.setText("随机学生");
                
                // 更新选中学生的分数显示
                String selectedClass = (String) classComboBox.getSelectedItem();
                String studentName = txtStudent.getText().trim();
                
                if ("全部班级".equals(selectedClass)) {
                    String[] parts = studentName.split("-");
                    if (parts.length >= 2) {
                        selectedClass = parts[0];
                        studentName = parts[1];
                    }
                }
                
                updateScoreDisplay(selectedClass, studentName);
            } else {
                btnChooseStudent.setText("停");
                threadStudent = new Thread(() -> {
                    try {
                        while (!Thread.interrupted()) {
                            final String randomStudent = studentList.get(random.nextInt(studentList.size()));
                            SwingUtilities.invokeLater(() -> txtStudent.setText(randomStudent));
                            Thread.sleep(50);
                        }
                    } catch (InterruptedException ex) {
                        // 线程被断，不需要处理
                    }
                });
                threadStudent.start();
            }
        });

        // 缺勤按钮事件
        btnAbsence.addActionListener(e -> {
            if (txtStudent.getText() == null || txtStudent.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请先随机选择学生", "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String selectedClass = (String) classComboBox.getSelectedItem();
            String studentName = txtStudent.getText().trim();
            
            // 如果是从"全部班级"中选择的学生，需要解析班级名
            if ("全部班级".equals(selectedClass)) {
                String[] parts = studentName.split("-");
                if (parts.length >= 2) {
                    selectedClass = parts[0];
                    studentName = parts[1];
                }
            }

            // 记录缺勤数据
            File attendanceFile = new File("D:" + File.separator + "workspacemax" 
                + File.separator + "java" 
                + File.separator + "student" 
                + File.separator + "attendance.txt");

            try {
                // 确保父目录存在
                File parentDir = attendanceFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }

                // 追加写入文件，格式：班级,学生姓名,类型(absence/leave),日期
                try (PrintWriter writer = new PrintWriter(new FileWriter(attendanceFile, true))) {
                    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    writer.println(String.join(",", selectedClass, studentName, "absence", date));
                }

                // 扣分
                updateScore(selectedClass, studentName, -3);
                JOptionPane.showMessageDialog(this, "已记录缺勤并扣3分", "", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "记录缺勤失败：" + ex.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // 请假按钮事件
        btnLeave.addActionListener(e -> {
            if (txtStudent.getText() == null || txtStudent.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请先随机选择学生", "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String selectedClass = (String) classComboBox.getSelectedItem();
            String studentName = txtStudent.getText().trim();
            
            if ("全部班级".equals(selectedClass)) {
                String[] parts = studentName.split("-");
                if (parts.length >= 2) {
                    selectedClass = parts[0];
                    studentName = parts[1];
                }
            }

            // 记录请假数据
            File attendanceFile = new File("D:" + File.separator + "workspacemax" 
                + File.separator + "java" 
                + File.separator + "student" 
                + File.separator + "attendance.txt");

            try {
                // 确保父目录存在
                File parentDir = attendanceFile.getParentFile();
                if (!parentDir.exists()) {
                    parentDir.mkdirs();
                }

                // 追加写入文件
                try (PrintWriter writer = new PrintWriter(new FileWriter(attendanceFile, true))) {
                    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    writer.println(String.join(",", selectedClass, studentName, "leave", date));
                }

                // 扣分
                updateScore(selectedClass, studentName, -3);
                JOptionPane.showMessageDialog(this, "已记录请假并扣3分", "", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                    "记录请假失败：" + ex.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // 回答正确按钮事件
        btnCorrect.addActionListener(e -> {
            if (txtStudent.getText() == null || txtStudent.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "请先随机选择学生", "", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String selectedClass = (String) classComboBox.getSelectedItem();
            String studentName = txtStudent.getText().trim();
            
            if ("全部班级".equals(selectedClass)) {
                String[] parts = studentName.split("-");
                if (parts.length >= 2) {
                    selectedClass = parts[0];
                    studentName = parts[1];
                }
            }

            // 加分
            updateScore(selectedClass, studentName, 5);
            JOptionPane.showMessageDialog(this, "已记录回答正确，加5分", "", JOptionPane.INFORMATION_MESSAGE);
        });
    }

    private void loadClasses() {
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
        groupComboBox.addItem("全部小组");
        
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

    private void loadAllStudents() {
        studentList.clear();
        File studentFile = new File("D:" + File.separator + "workspacemax" 
            + File.separator + "java" 
            + File.separator + "student" 
            + File.separator + "student.txt");
            
        if (studentFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(studentFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        studentList.add(parts[0] + "-" + parts[2]);  // 班级-姓名
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadStudents(String className, String groupName) {
        studentList.clear();
        File studentFile = new File("D:" + File.separator + "workspacemax" 
            + File.separator + "java" 
            + File.separator + "student" 
            + File.separator + "student.txt");
            
        if (studentFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(studentFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        if (parts[0].equals(className) && 
                            (groupName == null || groupName.equals(parts[3]))) {
                            studentList.add(parts[2]);  // 只添加姓名
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateScore(String className, String studentName, int scoreChange) {
        File scoreFile = new File("D:" + File.separator + "workspacemax" 
            + File.separator + "java" 
            + File.separator + "student" 
            + File.separator + "score.txt");
        
        Map<String, Integer> scores = new HashMap<>();
        
        // 读取现有成绩
        if (scoreFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(scoreFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        scores.put(parts[0] + "," + parts[1], Integer.parseInt(parts[2]));
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        // 更新成绩
        String key = className + "," + studentName;
        int currentScore = scores.getOrDefault(key, 0);
        int newScore = currentScore + scoreChange;
        scores.put(key, newScore);
        
        // 保存成绩
        try {
            if (!scoreFile.getParentFile().exists()) {
                scoreFile.getParentFile().mkdirs();
            }
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(scoreFile))) {
                for (Map.Entry<String, Integer> entry : scores.entrySet()) {
                    String[] parts = entry.getKey().split(",");
                    writer.println(parts[0] + "," + parts[1] + "," + entry.getValue());
                }
            }
            
            // 更新显示的成绩
            lblScore.setText("当前成绩：" + newScore);
            
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, 
                "更新成绩失败：" + ex.getMessage(), 
                "错误", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateScoreDisplay(String className, String studentName) {
        File scoreFile = new File("D:" + File.separator + "workspacemax" 
            + File.separator + "java" 
            + File.separator + "student" 
            + File.separator + "score.txt");
        
        if (scoreFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(scoreFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3 && parts[0].equals(className) && parts[1].equals(studentName)) {
                        lblScore.setText("当前成绩：" + parts[2]);
                        return;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        lblScore.setText("当前成绩：0");
    }
}
