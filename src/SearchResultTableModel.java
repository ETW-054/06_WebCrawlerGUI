import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.Vector;

public class SearchResultTableModel extends DefaultTableModel {
    Class[] types = { Integer.class, String.class, String.class, Integer.class };
    String[] columns = { "No", "Web Name", "Web Url", "Weight" };

    public int getColumnCount() { return columns.length; }
    public String getColumnName(int col) { return columns[col]; }
    public Class getColumnClass(int col) { return types[col]; }
    public boolean isCellEditable(int row, int col) {
        return 1 <= col && col <= 2;
    }

    public void setValueAt(Object value, int row, int col) {
        if (!isCellEditable(row, col)) {
            @SuppressWarnings("unchecked")
            Vector<Object> rowVector = dataVector.elementAt(row);
            rowVector.setElementAt(value, col);
            fireTableCellUpdated(row, col);
        }
    }

}
