package com.ontimize.gui.table;

import java.awt.Component;
import java.util.Hashtable;

import javax.swing.JTable;

import com.ontimize.gui.field.TextDataField;

public class StringCellEditor extends CellEditor {

    public StringCellEditor(Hashtable parameters) {
        super(parameters.get(CellEditor.COLUMN_PARAMETER), StringCellEditor.initializeDataField(parameters));
    }

    protected static TextDataField initializeDataField(Hashtable parameters) {
        TextDataField tdf = new TextDataField(parameters);
        if (tdf.getDataField() instanceof TextDataField.EJTextField) {
            ((TextDataField.EJTextField) tdf.getDataField()).setCaretPositionOnFocusLost(false);
        }
        return tdf;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

}
