package com.student.view;

import com.student.util.Constant;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ScoreListPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final String[] headers = {"学号", "姓名", "成绩"};
    private final DefaultTableModel tableModel;
    private final JTable scoreTable;
    private final String className;

    public ScoreListPanel(String className) {
        this.className = className;
        this.setBorder(new TitledBorder(new EtchedBorder(), className + "班级成绩"));
        this.setLayout(new BorderLayout());

        // 创建表格
        tableModel = new DefaultTableModel(null, headers);
        scoreTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scoreTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        this.add(scrollPane, BorderLayout.CENTER);

        // 添加导出按钮
        JButton exportButton = new JButton("导出成绩");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(exportButton);
        this.add(buttonPanel, BorderLayout.SOUTH);

        // 加载成绩数据
        loadScoreData();

        // 添加导出按钮事件
        exportButton.addActionListener(e -> exportScore());
    }

    private void loadScoreData() {
        // 清空表格数据
        while (tableModel.getRowCount() > 0) {
            tableModel.removeRow(0);
        }

        // 加载成绩数据
        Map<String, Integer> scoreData = new HashMap<>();
        File scoreFile = new File("D:" + File.separator + "workspacemax" 
            + File.separator + "java" 
            + File.separator + "student" 
            + File.separator + "score.txt");
            
        if (scoreFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(scoreFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 3 && parts[0].equals(className)) {
                        scoreData.put(parts[1], Integer.parseInt(parts[2]));
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
            try (BufferedReader reader = new BufferedReader(new FileReader(studentFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length >= 4 && parts[0].equals(className)) {
                        String studentId = parts[1];
                        String studentName = parts[2];
                        int score = scoreData.getOrDefault(studentName, 0);
                        
                        tableModel.addRow(new String[]{
                            studentId,
                            studentName,
                            String.valueOf(score)
                        });
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void exportScore() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择保存位置");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setSelectedFile(new File(className + "成绩.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // 写入表头
                writer.println("学号,姓名,成绩");
                
                // 写入数据
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String id = (String) tableModel.getValueAt(i, 0);
                    String name = (String) tableModel.getValueAt(i, 1);
                    String score = (String) tableModel.getValueAt(i, 2);
                    writer.println(String.join(",", id, name, score));
                }
                
                JOptionPane.showMessageDialog(this, 
                    "成绩已导出到：" + file.getAbsolutePath(), 
                    "导出成功", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "导出失败：" + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
} 