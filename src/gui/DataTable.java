package gui;

import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.Cursor;
import com.toedter.calendar.JDateChooser;
import gui.MyMenuBar.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;

public class DataTable extends JPanel{
    private class InputPanel extends JPanel{
        private class InputField extends JPanel{
            private JTextField tf;
            public InputField(String s){
                super(new FlowLayout());
                Border b=BorderFactory.createEmptyBorder();
                b=BorderFactory.createTitledBorder(b, s);
                setBorder(b);
                initcomp();
            }
            public void initcomp(){
                tf=new JTextField();
                tf.setPreferredSize(new Dimension(200, 26));
                add(tf);
            }
            public String getText(){
                return tf.getText();
            }
            public Object getData(){
                return tf.getText();
            }
        }
        private class DateInputField extends InputField{
            private JDateChooser dc;
            public DateInputField(String s){
                super(s);
            }
            @Override
            public void initcomp(){
                setDefaultLocale(Locale.getDefault());
                dc=new JDateChooser(new Date(System.currentTimeMillis()));
                dc.setDateFormatString(dateFormat);
                dc.setPreferredSize(new Dimension(200,28));
                add(dc);
            }
            @Override
            public String getText(){
                return dc.getDate().toString();
            }
            @Override
            public Date getData(){
                return new Date(dc.getDate().getTime());
            }
        }
        
        private InputField[] fields;
        private JButton recordBtn;
        private JButton importBtn;
        
        public InputPanel(){
            super(new GridLayout(0, 1, 0, 20));
            initcomp();
        }
        private void initcomp(){
            fields=new InputField[dbTable.getColumnCount()];
            int k=0;
            for(Column col: dbTable.getColumns()){
                switch(col.getType()){                        
                    case INT:
                        fields[k]=new InputField(col.getName()){
                            @Override
                            public Integer getData(){
                                return Integer.parseInt(getText());
                            }
                        };
                        break;
                    case LONG:
                        fields[k]=new InputField(col.getName()){
                            @Override
                            public Long getData(){
                                return Long.parseLong(getText());
                            }
                        };
                        break;
                    case FLOAT:
                        fields[k]=new InputField(col.getName()){
                            @Override
                            public Float getData(){
                                return Float.parseFloat(getText());
                            }
                        };
                        break;
                    case DOUBLE:
                        fields[k]=new InputField(col.getName()){
                            @Override
                            public Double getData(){
                                return Double.parseDouble(getText());
                            }
                        };
                        break;
                    case MONEY:
                        fields[k]=new InputField(col.getName()){
                            @Override
                            public Currency getData(){
                                return Currency.getInstance(getText());
                            }
                        };
                        break;
                    case SHORT_DATE_TIME:
                        fields[k]=new DateInputField(col.getName());
                        break;
                    default:
                        fields[k]=new InputField(col.getName());
                        break;
                    }
                    add(fields[k]);
                    k++;
                }
            recordBtn=new JButton(new MyAction("Capturar", null, "record", this));
            add(recordBtn);
            
            importBtn=new JButton(new MyAction("Importar", null, "importFromCsv", this));
            add(importBtn);
        }
        public void record(){
            Object[] rowData=new Object[fields.length];
                for(int i=0; i<fields.length; i++){
                    rowData[i]=fields[i].getData();
                }
                try{
                    addRow(rowData);
                }catch(IOException e){
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                }
        }
        public void importFromCsv(){
            JFileChooser fc=new JFileChooser();
            fc.setFileFilter(new FileFilter(){
                @Override
                public boolean accept(File file){
                    return file.isDirectory() || file.getName().endsWith(".csv");
                }
                @Override
                public String getDescription() {
                    return "CSV (Comma delimited)";
                }
            });
            fc.showOpenDialog(this);
            addFromCsv(fc.getSelectedFile());
        }
    }
    private class DateEditor extends AbstractCellEditor implements TableCellEditor{
        JDateChooser dc;
        public DateEditor(){
            dc=new JDateChooser();
            dc.setDateFormatString(dateFormat);
        }
        @Override
        public Object getCellEditorValue(){
            return dc.getDate();
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean bln, int i, int i1){
            dc.setDate((Date)value);
            return dc;
        }     
    }
    
    private final Table dbTable;
    private final String[] names;
    private final DataType[] types;
    private final String dateFormat = "dd MMM yyyy";
    
    private JTable table;
    private DefaultTableModel dtm;
    private TableCellListener tcl;
    private JPopupMenu cellPopup, headerPopup;
    private int indexCol;
    
    public DataTable(Database db, String t) throws IOException{
        super(new BorderLayout(20, 20));
        dbTable=db.getTable(t);
        int k=0, n=dbTable.getColumnCount();
        names=new String[n];
        types=new DataType[n];
        for(Column column : dbTable.getColumns()){
           names[k]=column.getName();
           types[k]=column.getType();
           k++;
        }
        initcomp();
    }
    private void initcomp() throws IOException{
        cellPopup=new JPopupMenu();
        cellPopup.add(new MyMenuItem("Eliminar registro", null, "deleteRow", this));
        headerPopup=new JPopupMenu();
        headerPopup.add(new MyMenuItem("Ocultar columna", null, "hideColumn", this));
        headerPopup.add(new MyMenuItem("Mostrar columnas ocultas", null, "unhideColumns", this));
        
        dtm=new DefaultTableModel(getRowData(), names){
            @Override
            public Class getColumnClass(int col) {
                switch(types[col]){
                    case BOOLEAN:
                        return Boolean.class;
                    case INT:
                        return Integer.class;
                    case LONG:
                        return Long.class;
                    case FLOAT:
                        return Float.class;
                    case DOUBLE:
                        return Double.class;
                    case SHORT_DATE_TIME:
                        return Date.class;
                     default:
                        return String.class;
                }
            }
        };
        table=new JTable(dtm){
            {
                setFillsViewportHeight(true);
                setRowSorter(new TableRowSorter(dtm));
                setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                addMouseListener(new MouseAdapter(){
                    @Override
                    public void mouseReleased(MouseEvent me) {
                        int r=table.rowAtPoint(me.getPoint());
                        if(r>=0 && r<table.getRowCount()){
                            table.setRowSelectionInterval(r, r);
                        }else{
                            table.clearSelection();
                        }
                        int rowindex=table.getSelectedRow();
                        if(rowindex<0){
                            return;
                        }if(me.isPopupTrigger() && me.getComponent() instanceof JTable){
                            showCellPopup(me);
                        }
                    }
                });
                getTableHeader().addMouseListener(new MouseAdapter(){
                    @Override
                    public void mouseReleased(MouseEvent me) {
                        indexCol=columnAtPoint(me.getPoint());
                        if(indexCol>=0 && me.getButton()==MouseEvent.BUTTON3){
                            showHeaderPopup(me);
                        }
                    };
                });
            }
        };
        
        table.setDefaultEditor(Date.class, new DateEditor());
        table.setDefaultRenderer(Date.class, new DefaultTableCellRenderer(){
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
                return super.getTableCellRendererComponent(table, sdf.format(value), isSelected, hasFocus, row, column);
            }
        });
        
        tcl=new TableCellListener(table, new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent ae){
                TableCellListener tcl=(TableCellListener)ae.getSource();
                String columnName=tcl.getTable().getColumnName(tcl.getColumn());
                try{
                    Cursor cursor=CursorBuilder.createCursor(dbTable);
                    if(cursor.findFirstRow(Collections.singletonMap(columnName, tcl.getOldValue()))){
                        cursor.setCurrentRowValue(dbTable.getColumn(columnName), parseType(tcl.getNewValue().toString(), types[tcl.getColumn()]));
                    }
                }catch(IOException e){
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                }
            }
        });
        
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, 
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), "Center");
        add(new InputPanel(), "East");
    }
    
    //Cell popup
    public void showCellPopup(MouseEvent me){
        cellPopup.show(me.getComponent(), me.getX(), me.getY());
    }
    public void deleteRow(){
        int view=table.getSelectedRow();
        int model=table.convertRowIndexToModel(view);
        dtm.removeRow(model);
    }
    
    //Header popup
    public void showHeaderPopup(MouseEvent me){
        headerPopup.show(me.getComponent(), me.getX(), me.getY());
    }
    public void hideColumn(){
        TableColumnModel tcm = table.getColumnModel();
        tcm.removeColumn(tcm.getColumn(indexCol));
    }
    public void unhideColumns(){
        
    }
    
    public void refresh()throws IOException{
        dtm.setDataVector(getRowData(), names);
    }
    public void addRow(Object... rowData)throws IOException{
        dbTable.addRow(rowData);
        dtm.addRow(rowData);
    }
    public void addFromCsv(File csvFileToRead){
        BufferedReader br=null;
        String line;
        try{
            br=new BufferedReader(new FileReader(csvFileToRead));
            Object[] rowData=new Object[types.length];
            while((line=br.readLine())!=null){
                String[] cell=line.split(",");
                for(int i=0; i<types.length; i++){
                    rowData[i]=parseType(cell[i], types[i]);
                }
                addRow(rowData);
            }
        }catch(IOException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }finally{
            if(br!=null){
                try{
                    br.close();
                }catch(IOException e){
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }
    
    public Object parseType(String s, DataType type){
        switch(type){
            case BOOLEAN:
                return Boolean.parseBoolean(s);
            case INT:
                return Integer.parseInt(s);
            case FLOAT:
                return Float.parseFloat(s);
            case DOUBLE:
                return Double.parseDouble(s);
            case SHORT_DATE_TIME:
                return Date.parse(s);
            default:
                return s;
        }
    }
    public Object[][] getRowData()throws IOException{
        int r=dbTable.getRowCount();
        int c=dbTable.getColumnCount();
        Object[][] rowData=new Object[r][];
        int i=0;
        for(Row row: dbTable){
            rowData[i]=new Object[c];
            for(int j=0; j<c; j++){
                rowData[i][j]=row.get(names[j]);
            }
            i++;
        }
        return rowData;
    }
}