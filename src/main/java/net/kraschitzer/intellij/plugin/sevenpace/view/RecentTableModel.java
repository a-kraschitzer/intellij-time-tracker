package net.kraschitzer.intellij.plugin.sevenpace.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Vector;

public class RecentTableModel extends DefaultTableModel {

    public RecentTableModel(Vector<Vector<Object>> data, Vector<String> columnNames) {
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
