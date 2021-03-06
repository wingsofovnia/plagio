package eu.ioservices.plagio.gui;

import eu.ioservices.plagio.algorithm.ShinglesAlgorithm;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class PlagioGui {
    enum PlagioMode {
        LIB_UPDATE_MODE("Library update only"),
        PLAGIARISM_MODE("Duplication report"),
        UPDATE_N_REPORT_MODE("Report + lib update");

        private final String display;
        private PlagioMode(String s) {
            display = s;
        }
        @Override
        public String toString() {
            return display;
        }
    }
    private static final String[] SHINGLES_COMBO_BOX_VALUES
            = {"Select shingle size. Default: " + ShinglesAlgorithm.DEFAULT_SHINGLE_SIZE,
            "2", "3", "4", "5", "6"};
    
    private JTextField libraryPathField;
    private JTextField inputPathField;
    private JTextArea outputArea;
    private JCheckBox normalizeTextCheckBox;
    private JTextField sparkMasterUrlField;
    private JButton processButton;
    private JComboBox<? extends String> shingleSizeCombo;
    private JPanel mainPanel;
    private JComboBox modeComboBox;

    public PlagioGui() {
        draw();
    }

    public void show() {
        JFrame frame = new JFrame("Plagio");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setIconImage(Toolkit.getDefaultToolkit().getImage(PlagioGui.class.getResource("/javax/swing/plaf/metal/icons/ocean/hardDrive.gif")));
        frame.pack();
        frame.setResizable(false);
        frame.setBounds(100, 100, 550, 400);

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            ((DefaultCaret) outputArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

            for(Window window : JFrame.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        frame.setVisible(true);
    }

    public void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this.mainPanel, Objects.requireNonNull(message), Objects.requireNonNull(title), messageType);
    }

    public void showErrorMessage(String message) {
        this.showMessage("Error", message, JOptionPane.ERROR_MESSAGE);
    }

    public void showWarningMessage(String message) {
        this.showMessage("Warning", message, JOptionPane.WARNING_MESSAGE);
    }

    public void showSuccessMessage(String message) {
        this.showMessage("Info", message, JOptionPane.INFORMATION_MESSAGE);
    }

    public String getLibPath() {
        return this.libraryPathField.getText();
    }

    public String getSparkMasterUrl() {
        return this.sparkMasterUrlField.getText();
    }

    public String getInputPath() {
        return this.inputPathField.getText();
    }

    public boolean isNormalizing() {
        return this.normalizeTextCheckBox.isSelected();
    }

    public PlagioMode getMode() {
        return (PlagioMode) modeComboBox.getSelectedItem();
    }

    public int getShinglesSize(int defaultValue) {
        try {
            return Integer.valueOf((String) this.shingleSizeCombo.getSelectedItem());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public OutputStream getOutputAreaStream() {
        return new TextAreaOutputStream(outputArea);
    }

    public void appendOutputAreaValue(String str) {
        this.outputArea.append(str);
    }

    public void setOutputAreaValue(String str) {
        this.outputArea.setText(str);
    }

    public void disableProcessButton() {
        this.processButton.setEnabled(false);
    }

    public void enableProcessButton() {
        this.processButton.setEnabled(true);
    }

    public void setProcessButtonActionListener(Consumer<MouseEvent> listener) {
        this.processButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent arg0) {
                listener.accept(arg0);
            }
        });
    }

    private void draw() {
        mainPanel = new JPanel();
        Border padding = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        mainPanel.setBorder(padding);
        mainPanel.setLayout(new GridBagLayout());
        libraryPathField = new JTextField();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(libraryPathField, gbc);
        inputPathField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(inputPathField, gbc);
        final JLabel label1 = new JLabel();
        label1.setText("Library Path:");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Input Documents Path:");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(label2, gbc);
        normalizeTextCheckBox = new JCheckBox();
        normalizeTextCheckBox.setText("Normalize Text");
        normalizeTextCheckBox.setSelected(true);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(normalizeTextCheckBox, gbc);
        sparkMasterUrlField = new JTextField("spark://localhost:7077");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(sparkMasterUrlField, gbc);
        final JLabel label3 = new JLabel();
        label3.setText("Spark Master URL:");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(label3, gbc);
        processButton = new JButton();
        processButton.setText("Process");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(processButton, gbc);
        shingleSizeCombo = new JComboBox<>(SHINGLES_COMBO_BOX_VALUES);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(shingleSizeCombo, gbc);
        outputArea = new JTextArea();
        outputArea.setFont(outputArea.getFont().deriveFont(11f));
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                outputArea.setCaretPosition(outputArea.getDocument().getLength());
            }
            @Override
            public void removeUpdate(DocumentEvent e) {}
            @Override
            public void changedUpdate(DocumentEvent arg0) {}
        });
        JScrollPane scrollableOutputArea = new JScrollPane(outputArea);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollableOutputArea, gbc);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(toolBar$Separator1, gbc);
        modeComboBox = new JComboBox();
        modeComboBox.setModel(new DefaultComboBoxModel<>(PlagioMode.values()));
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(modeComboBox, gbc);
    }
    /**
     * An output stream that writes its output to a javax.swing.JTextArea
     * control.
     *
     * @author Ranganath Kini
     * @see javax.swing.JTextArea
     */
    public static class TextAreaOutputStream extends OutputStream {
        private JTextArea textControl;

        /**
         * Creates a new instance of TextAreaOutputStream which writes
         * to the specified instance of javax.swing.JTextArea control.
         *
         * @param control A reference to the javax.swing.JTextArea
         *                control to which the output must be redirected
         *                to.
         */
        public TextAreaOutputStream(JTextArea control) {
            textControl = control;
        }

        /**
         * Writes the specified byte as a character to the
         * javax.swing.JTextArea.
         *
         * @param b The byte to be written as character to the
         *          JTextArea.
         */
        public void write(int b) throws IOException {
            // append the data as characters to the JTextArea control
            textControl.append(String.valueOf((char) b));
        }
    }
}
