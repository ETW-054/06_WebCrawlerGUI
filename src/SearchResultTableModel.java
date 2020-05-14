import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.Vector;

public class SearchResultTableModel extends DefaultTableModel {
    Class[] types = { Integer.class, String.class, String.class, Integer.class };
    String[] columns = { "No", "Web Name", "Web Url", "Weight" };
    //Object[][] data = {{ 1, null, null, null }};

    public int getColumnCount() { return columns.length; }
    //public int getRowCount() { return data.length; }
    //public Object getValueAt(int row, int col) { return col == 0 ? row + 1 : data[row][col]; }
    public String getColumnName(int col) { return columns[col]; }
    public Class getColumnClass(int col) { return types[col]; }
    public boolean isCellEditable(int row, int col) { return false; }


    public void setValueAt(Object value, int row, int col) {
        //data[row][col] = (col == 0 ? row : value);
        @SuppressWarnings("unchecked")
        Vector<Object> rowVector = dataVector.elementAt(row);
        if (col == 0) { rowVector.setElementAt(row, col); }
        else { rowVector.setElementAt(value, col); }
        fireTableCellUpdated(row, col);
    }

}
