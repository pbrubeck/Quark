package gui;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class MyMenuBar extends JMenuBar {
    public static class MyMenuItem extends JMenuItem{
        private Method method;
        private Object target;
        private Object[] params;
        public MyMenuItem(String label, String keyStroke, String methodName, Object obj, Object... args){
            target=obj;
            params=args;
            Class<?>[] types=new Class[args.length];
            for(int i=0; i<args.length; i++){
                types[i]=args[i].getClass();
            }
            try{
                method=obj.getClass().getMethod(methodName, types);
            }catch(NoSuchMethodException | SecurityException e) {
                System.err.println(e);
            }
            AbstractAction a=(new AbstractAction(label){
                @Override
                public void actionPerformed(ActionEvent ae){
                    invokeMethod();
                }
            });
            if(keyStroke!=null? !keyStroke.isEmpty(): false){
                a.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getAWTKeyStroke(keyStroke));
            }
            setAction(a);
        }
        private void invokeMethod(){
            try{
                method.invoke(target, params);
            }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
                System.err.println(e);
            }
        }
    }
    public static class MyMenu extends JMenu{
        private final JMenuItem[] item;
        public MyMenu(String s, JMenuItem... options) {
            super(s);
            item=new JMenuItem[options.length];
            for(int i=0;  i<options.length; i++) {
                item[i]=options[i];
                add(item[i]);
            }
        }
    }
    
    private final MyMenu[] menu;
    public MyMenuBar(String[] s, JMenuItem[][] options){
        menu=new MyMenu[s.length];
        for (int i=0; i<s.length; i++) {
            menu[i]=new MyMenu(s[i], options[i]);
            add(menu[i]);
        }
    }
}