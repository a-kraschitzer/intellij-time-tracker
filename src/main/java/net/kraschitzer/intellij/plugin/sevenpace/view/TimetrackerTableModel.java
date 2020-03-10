package net.kraschitzer.intellij.plugin.sevenpace.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class TimetrackerTableModel extends DefaultTableModel {

    public static Map<Integer, Integer> recentColumnSizeAbsWidth;
    public static Map<Integer, Integer> workItemColumnSizeAbsWidth;

    public static Vector<String> recentColumnNames;
    public static Vector<String> workItemsColumnNames;

    static {
        recentColumnSizeAbsWidth = new HashMap<>();
        recentColumnSizeAbsWidth.put(0, 20);
        recentColumnSizeAbsWidth.put(1, 40);
        recentColumnSizeAbsWidth.put(2, 20);
        recentColumnSizeAbsWidth.put(3, 70);
        recentColumnSizeAbsWidth.put(5, 80);
        recentColumnSizeAbsWidth.put(6, 70);
        recentColumnSizeAbsWidth.put(7, 130);
        recentColumnSizeAbsWidth.put(8, 130);

        recentColumnNames = new Vector<>();
        recentColumnNames.add("");
        recentColumnNames.add("id");
        recentColumnNames.add("");
        recentColumnNames.add("type");
        recentColumnNames.add("name");
        recentColumnNames.add("trackType");
        recentColumnNames.add("trackedTime");
        recentColumnNames.add("startTime");
        recentColumnNames.add("endTime");


        workItemColumnSizeAbsWidth = new HashMap<>();
        workItemColumnSizeAbsWidth.put(0, 20);
        workItemColumnSizeAbsWidth.put(1, 40);
        workItemColumnSizeAbsWidth.put(2, 20);
        workItemColumnSizeAbsWidth.put(3, 70);

        workItemsColumnNames = new Vector<>();
        workItemsColumnNames.add("");
        workItemsColumnNames.add("id");
        workItemsColumnNames.add("");
        workItemsColumnNames.add("type");
        workItemsColumnNames.add("name");
    }

    public static void adjustColumnSizes(JTable tableWorkItemsRecent, Map<Integer, Integer> sizes) {
        final TableColumnModel model = tableWorkItemsRecent.getColumnModel();
        sizes.forEach((column, size) -> {
            model.getColumn(column).setMinWidth(size);
            model.getColumn(column).setMaxWidth(size);
        });
    }

    public TimetrackerTableModel(Vector<Vector<Object>> data, Vector<String> columnNames) {
        super(data, columnNames);
    }

    @Override
    public int getRowCount() {
        return super.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return super.getColumnCount();
    }

    @Override
    public Object getValueAt(int row, int column) {
        return super.getValueAt(row, column);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
            case 2:
                return ImageIcon.class;
            case 1:
                return Integer.class;
            default:
                return String.class;
        }
    }
}
