package com.smartcomplaint.ui.components;

import com.smartcomplaint.model.ComplaintPriority;
import com.smartcomplaint.model.ComplaintStatus;
import com.smartcomplaint.model.SLAStatus;
import com.smartcomplaint.ui.Theme;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import java.awt.*;

public class ModernTable extends JTable {

    public ModernTable(TableModel model) {
        super(model);
        initStyle();
    }

    public ModernTable() {
        super();
        initStyle();
    }

    private void initStyle() {
        setRowHeight(38);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setSelectionBackground(Theme.PRIMARY_LIGHT);
        setSelectionForeground(Theme.PRIMARY_DARK);
        setFont(Theme.FONT_BODY);

        // Header style
        JTableHeader header = getTableHeader();
        header.setFont(Theme.FONT_HEADER);
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(Theme.TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR));

        // Default cell renderer
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (value instanceof ComplaintStatus) {
                    return StatusBadge.forStatus((ComplaintStatus) value);
                } else if (value instanceof SLAStatus) {
                    return StatusBadge.forSLA((SLAStatus) value);
                } else if (value instanceof ComplaintPriority) {
                    return StatusBadge.forPriority((ComplaintPriority) value);
                }

                JLabel lbl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
                if (!isSelected) {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    lbl.setForeground(Theme.TEXT_PRIMARY);
                }
                return lbl;
            }
        });
    }
}
