import javax.swing.*;
import java.awt.event.*;
import javax.swing.plaf.metal.*;
import javax.swing.event.*;
import java.io.*;
import java.util.Stack;

class Editor extends JFrame implements ActionListener {
    JTextArea textArea;
    JFrame frame;

    // Action class for storing undo/redo operations
    class EditAction {
        boolean isInsert; // True for insert, false for delete
        int position; // Position of the change
        String text; // Text inserted or deleted

        EditAction(boolean isInsert, int position, String text) {
            this.isInsert = isInsert;
            this.position = position;
            this.text = text;
        }
    }

    //initialize here
    boolean trackingChanges = true; // To avoid recursive calls during undo/redo

    Editor() {
        frame = new JFrame("Text Editor");

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            MetalLookAndFeel.setCurrentTheme(new OceanTheme());
        } catch (Exception e) {
            e.printStackTrace();
        }

        textArea = new JTextArea();

        // Track text changes
        //TRACK THOSE CHANGES HERE HERE

        // Menu bar
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem printItem = new JMenuItem("Print");

        newItem.addActionListener(this);
        openItem.addActionListener(this);
        saveItem.addActionListener(this);
        printItem.addActionListener(this);

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(printItem);

        // Edit menu
        JMenu editMenu = new JMenu("Edit");
        JMenuItem cutItem = new JMenuItem("Cut");
        JMenuItem copyItem = new JMenuItem("Copy");
        JMenuItem pasteItem = new JMenuItem("Paste");
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem redoItem = new JMenuItem("Redo");

        cutItem.addActionListener(this);
        copyItem.addActionListener(this);
        pasteItem.addActionListener(this);
        undoItem.addActionListener(this);
        redoItem.addActionListener(this);

        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);
        editMenu.add(undoItem);
        editMenu.add(redoItem);

        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(this);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(closeItem);

        frame.setJMenuBar(menuBar);
        frame.add(new JScrollPane(textArea));
        frame.setSize(500, 500);
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "Cut":
                textArea.cut();
                break;
            case "Copy":
                textArea.copy();
                break;
            case "Paste":
                textArea.paste();
                break;
            case "Undo":
                undo();
                break;
            case "Redo":
                redo();
                break;
            case "Save":
                saveFile();
                break;
            case "Print":
                printFile();
                break;
            case "Open":
                openFile();
                break;
            case "New":
                textArea.setText("");
                break;
            case "Close":
                frame.setVisible(false);
                break;
        }
    }

    //here undo redo

    private void saveFile() {
        JFileChooser fileChooser = new JFileChooser("f:");
        int returnValue = fileChooser.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(textArea.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage());
            }
        }
    }

    private void openFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = new File(fileChooser.getSelectedFile().getAbsolutePath());
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                textArea.read(reader, null);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame, e.getMessage());
            }
        }
    }

    private void printFile() {
        try {
            textArea.print();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Editor();
    }
}