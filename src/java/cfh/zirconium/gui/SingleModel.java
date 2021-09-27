package cfh.zirconium.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import javax.swing.table.AbstractTableModel;

import cfh.zirconium.Program;
import cfh.zirconium.net.Single;
import cfh.zirconium.net.Station;

/** TableModel for single station details. */
@SuppressWarnings("serial")
public class SingleModel extends AbstractTableModel {
    
    private static final Column[] COLUMNS = {
            new Column("name", 85, Single::toString),
            new Column("#", 50, Single::drones),
            new Column("Δ", 50, Single::delta),
            new Column("parent", 60, Single::parentID),
            new Column("Σ", 55, Single::total),
    };
    
    private final List<Single> stations = new ArrayList<>();
    
    /** Program changed. */
    void program(Program program) {
        stations.clear();
        if (program != null) {
            program
            .stations()
            .stream()
            .flatMap(Station::stations)
            .sorted(Comparator.comparing(Single::pos))
            .forEach(stations::add);
        }
        fireTableDataChanged();
    }
    
    /** Return the station at row line. */
    public Single station(int row) {
        return stations.get(row);
    }
    
    /** Column size. */
    public int size(int col) {
        return COLUMNS[col].size;
    }
    
    @Override
    public int getRowCount() {
        return stations.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }
    
    @Override
    public String getColumnName(int col) {
        return COLUMNS[col].name;
    }

    @Override
    public Object getValueAt(int row, int col) {
        Single single = stations.get(row);
        return COLUMNS[col].getter.apply(single);
    }

    //==============================================================================================
    
    private static class Column {
        private final String name;
        private final int size;
        private final Function<Single, Object> getter;
        Column(String name, int size, Function<Single, Object> getter) {
            this.name = Objects.requireNonNull(name);
            this.size = size;
            this.getter = Objects.requireNonNull(getter);
        }
    }
}
