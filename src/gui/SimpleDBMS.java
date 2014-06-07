package gui;

import gui.MyComponent.*;
import com.healthmarketscience.jackcess.*;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

public class SimpleDBMS extends JFrame{
    private final String[] menus={"File", "Edit", "View", "Tools", "Help"};
    private final SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss EEE, dd MMM yyyy");
    
    private File dbFile;
    private Database db;
    private JFileChooser fc;
    private JTabbedPane tp;
    private ChangeListener cl;
    private JLabel infoLabel, pathLabel;
    
    private boolean isFullScreen=false;
    
    public SimpleDBMS(){
        initcomp();
        setVisible(true);
    }
    private void initcomp(){
        
        //Initialize frame
        setTitle("Quark Industries");
        setMinimumSize(new Dimension(800, 600));
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent we){
                exit();
            }
        });
        
        //Initialize menuBar
        MyMenuItem[][] items={
            {new MyMenuItem("Open", "control O", "openFile", this), 
                new MyMenuItem("Save", "control S", "saveFile", this), 
                new MyMenuItem("Exit", "alt F4", "exit", this)},
            {new MyMenuItem("Undo", "control Z", "undo", this), 
                new MyMenuItem("Redo", "control Y", "redo", this), 
                new MyMenuItem("Cut", "control X", "cut", this), 
                new MyMenuItem("Copy", "control C", "copy", this), 
                new MyMenuItem("Paste", "control V", "paste", this),
                new MyMenuItem("Delete", "DELETE", "delete", this)},
            {new MyMenuItem("Full Screen", "F11", "fullScreen", this)},
            {new MyMenuItem("Options", null, "options", this)},
            {new MyMenuItem("Help Contents", "F1", "helpContents", this), 
                new MyMenuItem("About", null, "about", this)}
        };
        setJMenuBar(new MyMenuBar(menus, items));
        
        //Initialize fileChooser
        fc=new JFileChooser();
        fc.setFileFilter(new FileFilter(){
            @Override
            public boolean accept(File file){
                return file.isDirectory() || file.getName().endsWith(".mdb");
            }
            @Override
            public String getDescription(){
                return "Microsoft Access Database";
            }
        });
        
        //Initialize database
        String path="MyDataBase.mdb";
        dbFile=null;
        try{
            dbFile=new File(path);
            db=DatabaseBuilder.open(dbFile);
            fc.setCurrentDirectory(dbFile);
        }catch(IOException e){
            fc.showOpenDialog(this);
            dbFile=fc.getSelectedFile();
            try{
                db=DatabaseBuilder.open(dbFile);
            }catch(IOException e1){
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e1);
            }
        }
        
        
        //Initialize statusPanel
        infoLabel=new JLabel();
        pathLabel=new JLabel();
        JPanel status=new JPanel(new BorderLayout(30,30)){
            {
                setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                add(pathLabel, "West");
                add(infoLabel, "Center");
                add(new RealTimeLabel(1000, new Caller("getTime", SimpleDBMS.this)), "East");
            }
        };
        updatePath();
        
        //Initialize tabbedPane
        tp=new JTabbedPane();
        openDataTables(db);
        tp.addChangeListener(cl=new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent ce){
                updateInfo();
            }
        });
        
        //Add components to frame
        Container contentPane=getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(tp, "Center");
        contentPane.add(status, "South");
    }
    
    public void openDataTables(Database dataBase){
        try{
            for(String s: dataBase.getTableNames()){
                addDataTable(s, new DataTable(dataBase, s));
            }
        }catch(IOException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }
    public void addDataTable(String s, DataTable dt){
        tp.addTab(s, dt);
    }
    
    //Status bar
    public String getTime(){
        return sdf.format(new Date());
    }
    public void updatePath(){
        pathLabel.setText(dbFile.getAbsolutePath());
    }
    public void updateInfo(){
        try{
            String title = tp.getTitleAt(tp.getSelectedIndex());
            int i=db.getTable(title).getRowCount();
            infoLabel.setText(title+" "+i+" registros");
        }catch(IOException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }
    
    //File menu
    public void openFile(){
        fc.showOpenDialog(this);
        dbFile=fc.getSelectedFile();
        try{
            db.close();
            db=DatabaseBuilder.open(dbFile);
            tp.removeChangeListener(cl);
            tp.removeAll();
            openDataTables(db);
            tp.addChangeListener(cl);
            updatePath();
        }catch(IOException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }
    public void saveFile(){
        fc.showSaveDialog(this);
        updatePath();
    }
    public void exit(){
        try{
            db.close();
        }catch(IOException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
        System.exit(0);
    }
    //Edit menu
    public void undo(){
        
    }
    public void redo(){
        
    }
    public void cut(){
        
    }
    public void copy(){
        
    }
    public void paste(){
        
    }
    public void delete(){
        
    }
    //View menu
    public void fullScreen(){
        GraphicsDevice gd=getGraphicsConfiguration().getDevice();
        if(gd.isFullScreenSupported()){
            removeNotify();
            setUndecorated(!isFullScreen);
            gd.setFullScreenWindow(isFullScreen? null: this);
            if(isFullScreen){
                setVisible(true);
            }
            requestFocus(true);
            isFullScreen=!isFullScreen;
        }
    }
    //Tools menu
    public void options(){
        
    }
    //Help menu
    public void helpContents(){
        try {
            Desktop.getDesktop().open(new File("Help Contents.pdf"));
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }
    public void about(){
        JOptionPane.showMessageDialog(null, 
                "<html>2014 Quark Industries.<br>" 
                + "Designed with Java TM.<br>"
                + "License number: XXXX-XXXX-XXXX-XXXX.</html>",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args){
        try{
            UIManager.setLookAndFeel(new WindowsLookAndFeel());
        }catch(UnsupportedLookAndFeelException e){
            Logger.getLogger(SimpleDBMS.class.getName()).log(Level.SEVERE, null, e);
        }
        SimpleDBMS m=new SimpleDBMS();
    }
}