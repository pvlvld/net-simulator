/*
 NET-Simulator -- Network simulator.
 Copyright (C) 2006 Maxim Tereshin <maxim-tereshin@yandex.ru>

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.
            
 This program is distributed in the hope that it will be useful, but 
 WITHOUT ANY WARRANTY; without even the implied warranty of 
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 General Public License for more details.
            
 You should have received a copy of the GNU General Public License along 
 with this program; if not, write to the Free Software Foundation, 
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
 */
package org.netsimulator.term;

import org.netsimulator.util.ConfigurableThreadFactory;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;

public class Terminal extends TextScreen implements CommandExecutionCompletedListener {

    private static final ExecutorService commandExecutor = 
            new ThreadPoolExecutor(20, 1000, 180L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ConfigurableThreadFactory("CommandExecutor-"));  
    
    public static final String PS1 = "=>";
    public static final String help
            = "Welcome to console of the NET-Simulator virtual device!\n\n"
            + "Type help for list of commands. To get help for the command\n"
            + "type <command> -h.\n"
            + "Press Ctrl+L to refresh the screen.\n\n";
    private final TreeMap<String, CLICommand> commands
            = new TreeMap<String, CLICommand>();
    private int x_orig = PS1.length(), y_orig = 1; // begin of edit line
    private int x_end = x_orig, y_end = y_orig; // end of edit line

    private CLICommand commandToRun = null;
    private final ArrayList<String> history = new ArrayList<String>();
    private int historyIndex = 0;

    private static final Logger logger
            = Logger.getLogger("org.netsimulator.term.Terminal");

    public Terminal() {
        super();

        history.add(0, "");

        printHelp();
        printPS1();
    }

    public void addCommand(CLICommand command) {
        commands.put(command.getName(), command);
        command.setOutputWriter(writer);
    }

    @Override
    public void clear() {
        super.clear();
        printHelp();
        printPS1();
    }

    public void printHelp() {
        try {
            writer.write(help);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Unexpected exception.", ioe);
        }
    }

    private void printPS1() {
        if (commandToRun != null) {
            return;
        }

        try {
            writer.write(PS1);
        } catch (IOException ioe) {
            logger.log(Level.SEVERE, "Unexpected exception.", ioe);
        }

        x_end = x_orig = x_cur;
        y_end = y_orig = y_cur;
    }

    private void clearLine() {
        for (int j = 0; j != this.width; j++) {
            screen[y_cur - 1][j] = ' ';
        }

        goToX(1);
        printPS1();
    }
    
    private String getClipboardContent() {
        String clipboardContent = null;
        try {
            clipboardContent = ((String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor)).trim();
        } catch (Exception e) {
            System.out.println(String.format("getClipboardContent (%s) throws:", clipboardContent));
            e.printStackTrace();
        }
        return clipboardContent;
    }

    @Override
    void keyTyped(KeyEvent e) {
        if (e.getModifiers() == KeyEvent.CTRL_MASK) {
            System.out.println("ctrl pressed");
            System.out.println(((int)e.getKeyChar()));
            switch (e.getKeyChar()) {
                case 22: // V
                    String clipboardContent = getClipboardContent();
                    if (clipboardContent != null) {
                        for (int i = 0; i < clipboardContent.length(); i++) {
                            char c = clipboardContent.charAt(i);
                            if (c == '\n') {
                            } else {
                                drawChar(c);
                            }
                        }
                    }
                    break;
                default:
                    return;
            }
        }

        switch (e.getKeyChar()) {
            case KeyEvent.VK_BACK_SPACE:
            case KeyEvent.VK_DELETE:
                return;
            case KeyEvent.VK_ENTER:
                StringBuilder cl = new StringBuilder();
                for (int i = y_orig - 1; i < y_end; i++) {
                    for (int j = x_orig - 1; j < x_end; j++) {
                        cl.append(screen[i][j]);
                    }
                }
                drawChar(e.getKeyChar());

                String buf = cl.toString().trim();
                if (buf.length() > 0) {
                    history.add(1, buf);
                    logger.log(Level.FINE, "Command {0} added to history list", buf);
                }
                historyIndex = 0;
                parseCommand(buf);
                printPS1();
                repaint();
                break;
            default:
                if (x_cur == x_end) {
                    drawChar(e.getKeyChar());
                    x_end = x_cur;
                    y_end = y_cur;
                }
                if (x_cur < x_end) {
                    stepRight();
                    drawChar(e.getKeyChar());
                }
                repaint();
        }
    }

    private void stepLeft() {
        for (int i = y_cur - 1; i < y_end; i++) {
            for (int j = x_cur - 1; j < x_end - 1; j++) {
                screen[i][j] = screen[i][j + 1];
            }
        }

        decrementXend();
    }

    private void stepRight() {
        for (int i = y_cur - 1; i < y_end; i++) {
            for (int j = x_end; j >= x_cur; j--) {
                screen[i][j] = screen[i][j - 1];
            }
        }

        incrementXend();
    }

    private void decrementXend() {
        int x = x_end - 1;

        if (x_end < x_orig) {
            if (y_end > y_orig) {
                y_end--;
                x_end = width;
            }
        } else {
            x_end = x;
        }
    }

    private void incrementXend() {
        int x = x_end + 1;

        if (x > width) {
            x_end = 1;
            if (y_end == height) {
                push();
            } else {
                y_end++;
            }
        } else {
            x_end = x;
        }
    }

    private void parseCommand(String cl) {
        if (commandToRun != null) {
            return;
        }

        String command = null;
        String argv[] = null;

        if (cl != null && (cl = cl.trim()).length() > 0) {
            StringTokenizer t = new StringTokenizer(cl);
            command = t.nextToken();

            argv = new String[t.countTokens()];

            for (int i = 0; i != argv.length; i++) {
                argv[i] = t.nextToken();
            }

            if (commands.containsKey(command)) {
                commandToRun = (CLICommand) (commands.get(command));
                commandToRun.setInvocationContext(argv, cl);
                commandToRun.addExecutionCompleteListener(this);
                commandExecutor.submit(commandToRun);
            } else {
                try {
                    writer.write("Error: Unknown command. Try help to get list of commands.\n");
                } catch (IOException ioe) {
                    logger.log(Level.SEVERE, "Unexpected exception.", ioe);
                }
            }
        }
    }

    @Override
    void keyReleased(KeyEvent e) {
    }

    @Override
    void keyPressed(KeyEvent e) {
        int x = -1;

        switch (e.getModifiers()) {
            case KeyEvent.CTRL_MASK:
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_L:
                        clear();
                        break;
                    case KeyEvent.VK_C:
                        if (commandToRun != null) {
                            commandToRun.stop();
                        }
                        break;
                    default:
                }
                break;
            default:
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_KP_UP:
                        if ((historyIndex + 1) < history.size()) {
                            historyIndex++;
                        }
                        clearLine();
                        try {
                            writer.write(history.get(historyIndex));
                            x_end = x_cur;
                            y_end = y_cur;
                        } catch (IOException ioe) {
                            logger.log(Level.SEVERE, "Unexpected exception.", ioe);
                        }
                        break;
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_KP_DOWN:
                        if ((historyIndex - 1) >= 0) {
                            historyIndex--;
                        }
                        clearLine();
                        try {
                            writer.write(history.get(historyIndex));
                            x_end = x_cur;
                            y_end = y_cur;
                        } catch (IOException ioe) {
                            logger.log(Level.SEVERE, "Unexpected exception.", ioe);
                        }
                        break;
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_KP_LEFT:
                        x = x_cur - 1;
                        if (x >= x_orig) {
                            goToX(x);
                            repaint();
                        }
                        break;
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_KP_RIGHT:
                        x = x_cur + 1;
                        if (x <= x_end) {
                            goToX(x);
                            repaint();
                        }
                        break;
                    case KeyEvent.VK_HOME:
                        goToX(x_orig);
                        repaint();
                        break;
                    case KeyEvent.VK_END:
                        goToX(x_end);
                        repaint();
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                        x = x_cur - 1;
                        if (x >= x_orig) {
                            goToX(x);
                            stepLeft();
                            repaint();
                        }
                        break;
                    case KeyEvent.VK_DELETE:
                        if (x_cur < x_end) {
                            stepLeft();
                            repaint();
                        }
                        break;
                    default:
                }
        }
    }

    public TreeMap<String, CLICommand> getCommands() {
        return commands;
    }

    @Override
    public void executionCompleted(int resultCode) {
        commandToRun.removeExecutionCompleteListener(this);
        commandToRun = null;
        printPS1();
    }

}
