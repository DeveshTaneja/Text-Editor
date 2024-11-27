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

    Stack<EditAction> undoStack = new Stack<>();
    Stack<EditAction> redoStack =new Stack<>();
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
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (trackingChanges) handleInsert(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (trackingChanges) handleDelete(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Not needed for plain text edits
            }

            private void handleInsert(DocumentEvent e) {
                try {
                    int offset = e.getOffset();
                    String insertedText = e.getDocument().getText(offset, e.getLength());
                    undoStack.push(new EditAction(true, offset, insertedText));
                    redoStack.clear(); // Clear redo stack after a new edit
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            private void handleDelete(DocumentEvent e) {
                try {
                    int offset = e.getOffset();
                    String deletedText = textArea.getText(offset, e.getLength());
                    undoStack.push(new EditAction(false, offset, deletedText));
                    redoStack.clear(); // Clear redo stack after a new edit
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

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

    private void undo() {
        if (!undoStack.isEmpty()) {
            EditAction action = undoStack.pop();
            redoStack.push(action);

            trackingChanges = false; // Disable tracking during undo
            try {
                if (action.isInsert) {
                    // Undo insertion by removing the text
                    textArea.replaceRange("", action.position, action.position + action.text.length());
                } else {
                    // Undo deletion by reinserting the text
                    textArea.insert(action.text, action.position);
                }
            } finally {
                trackingChanges = true; // Re-enable tracking
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Nothing to Undo");
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            EditAction action = redoStack.pop();
            undoStack.push(action);

            trackingChanges = false; // Disable tracking during redo
            try {
                if (action.isInsert) {
                    // Redo insertion by adding the text back
                    textArea.insert(action.text, action.position);
                } else {
                    // Redo deletion by removing the text again
                    textArea.replaceRange("", action.position, action.position + action.text.length());
                }
            } finally {
                trackingChanges = true; // Re-enable tracking
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Nothing to Redo");
        }
    }

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