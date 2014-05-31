package gui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainApplication extends JFrame implements ActionListener, WindowListener{
    private final static String[] tab={
        "Informacion", "Clientes", "Pedidos", "Ventas", "Recursos Humanos", 
        "Gastos", "Balance", "Catalogo", "Prestamos", "Balace Total", "Wizard"
    };
    
    private JTabbedPane tp;
    private JComponent[] cont;
    
    public MainApplication(){
        initcomp();
        setVisible(true);
    }
    private void initcomp(){
        addWindowListener(this);
        setTitle("Quark Industries");
        setMinimumSize(new Dimension(800,600));
        Container contentPane=getContentPane();
        contentPane.setLayout(new BorderLayout());
        
        tp=new JTabbedPane();
        cont=new JPanel[tab.length];
        for(int i=0; i<tab.length; i++){
            cont[i]=samplePanel(tab[i]);
            tp.addTab(tab[i], cont[i]);
        }
        
        JPanel bot=new JPanel(new GridLayout(1,3));
        bot.add(new Label("Contacto"));
        bot.add(new Label("Ayuda"));
        bot.add(new Label("Cerrar sesion"));
        
        contentPane.add(tp, "Center");
        contentPane.add(bot, "South");
    }
    
    private static JPanel samplePanel(String s){
        try{
            switch(s){
                case "Clientes":
                case "Ventas":
                case "Catalogo":
                    return new TableViewer(s);
                case "Informacion":
                    return new TableViewer("Empleados");  
            }
        }catch(IOException e){  }
        JPanel p=new JPanel();
        p.setLayout(new FlowLayout());
        JLabel label=new JLabel(s);
        p.add(label);
        return p;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae){
    }
    @Override
    public void windowOpened(WindowEvent we){
    }
    @Override
    public void windowClosing(WindowEvent we){
        System.exit(0);
    }
    @Override
    public void windowClosed(WindowEvent we){
    }
    @Override
    public void windowIconified(WindowEvent we){
    }
    @Override
    public void windowDeiconified(WindowEvent we){
    }
    @Override
    public void windowActivated(WindowEvent we){
    }
    @Override
    public void windowDeactivated(WindowEvent we){
    }
    
    public static void main(String[] args){
        MainApplication m=new MainApplication();
    }
}
