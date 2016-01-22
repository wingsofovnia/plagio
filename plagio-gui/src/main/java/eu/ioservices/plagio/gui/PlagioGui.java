package eu.ioservices.plagio.gui;

import javax.swing.*;
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
    private static final String[] SHINGLES_COMBO_BOX_VALUES = {"Select shingle size", "2", "4", "6"};
    private JTextField libraryPathField;
    private JTextField inputPathField;
    private JTextArea outputArea;
    private JCheckBox normalizeTextCheckBox;
    private JTextField sparkMasterUrlField;
    private JButton processButton;
    private JComboBox<? extends String> shingleSizeCombo;
    private JPanel mainPanel;
    private JCheckBox updateLibraryCheckBox;

    public PlagioGui() {
        draw();
    }

    public void show() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Met".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
        JFrame frame = new JFrame("Plagio");
        frame.setContentPane(mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setBounds(100, 100, 450, 300);
    }

    public void showMessage(String title, String message, int messageType) {
        JOptionPane.showMessageDialog(this.mainPanel, Objects.requireNonNull(message), Objects.requireNonNull(title), messageType);
    }

    public void showErrorMessage(String message) {
        this.showMessage("Error", message, JOptionPane.ERROR_MESSAGE);
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

    public boolean isLibraryUpdate() {
        return this.updateLibraryCheckBox.isSelected();
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

    private void draw() {mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        libraryPathField = new JTextField();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(libraryPathField, gbc);
        inputPathField = new JTextField();
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
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
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(label1, gbc);
        final JLabel label2 = new JLabel();
        label2.setText("Input Documents Path:");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(label2, gbc);
        normalizeTextCheckBox = new JCheckBox();
        normalizeTextCheckBox.setText("Normalize Text");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(normalizeTextCheckBox, gbc);
        sparkMasterUrlField = new JTextField("spark://localhost:7077");
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
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
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(label3, gbc);
        processButton = new JButton();
        processButton.setText("Process");
        gbc = new GridBagConstraints();
        gbc.gridx = 4;
        gbc.gridy = 4;
        gbc.gridheight = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(processButton, gbc);
        shingleSizeCombo = new JComboBox<>(SHINGLES_COMBO_BOX_VALUES);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(shingleSizeCombo, gbc);
        outputArea = new JTextArea();
        outputArea.setLineWrap(true);
        JScrollPane scrollableOutputArea = new JScrollPane(outputArea);
        ((DefaultCaret) outputArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollableOutputArea, gbc);
        final JToolBar.Separator toolBar$Separator1 = new JToolBar.Separator();
        gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(toolBar$Separator1, gbc);
        updateLibraryCheckBox = new JCheckBox();
        updateLibraryCheckBox.setText("Update library");
        gbc = new GridBagConstraints();
        gbc.gridx = 2;
        gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.WEST;
        mainPanel.add(updateLibraryCheckBox, gbc);
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
