package csci2020u.assignment01;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import com.formdev.flatlaf.FlatLightLaf;

public class SpamDetectorGUI {
    private SpamDetector detector;
    private JFrame frame;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JLabel accuracyLabel;
    private JLabel precisionLabel;
    private JLabel statusLabel;
    private JProgressBar progressBar;

    public static void main(String[] args) {
        // Apply modern FlatLaf theme
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(SpamDetectorGUI::new);
    }

    public SpamDetectorGUI() {
        frame = new JFrame("Spam Detector");
        frame.setSize(850, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        detector = new SpamDetector();

        // Create main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BorderLayout());

        // Button Panel (TRAIN & TEST + Accuracy & Precision)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));

        JButton trainButton = createStyledButton("Train Model");
        JButton testButton = createStyledButton("Run Test");

        // Accuracy & Precision Labels (Placed Next to Buttons)
        accuracyLabel = new JLabel("Accuracy: N/A");
        precisionLabel = new JLabel("Precision: N/A");

        buttonPanel.add(trainButton);
        buttonPanel.add(testButton);
        buttonPanel.add(accuracyLabel);
        buttonPanel.add(precisionLabel);

        // Status Bar
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel("Welcome to Spam Detector", JLabel.LEFT);
        progressBar = new JProgressBar();
        progressBar.setVisible(false);

        statusPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(progressBar, BorderLayout.EAST);

        // Table
        tableModel = new DefaultTableModel(new String[]{"Filename", "Spam Probability", "Actual Class", "Prediction"}, 0);
        resultsTable = new JTable(tableModel);
        resultsTable.setShowGrid(true);
        resultsTable.setGridColor(Color.GRAY);
        JScrollPane scrollPane = new JScrollPane(resultsTable);

        // Training Action
        trainButton.addActionListener(e -> {
            JFileChooser directoryChooser = new JFileChooser();
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            directoryChooser.setCurrentDirectory(new File("."));

            int returnValue = directoryChooser.showOpenDialog(frame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedDir = directoryChooser.getSelectedFile();
                startLoading("Training in progress...");
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        detector.train(selectedDir);
                        return null;
                    }

                    @Override
                    protected void done() {
                        stopLoading("Training complete! Now run the test.");
                        JOptionPane.showMessageDialog(frame, "Training complete! Now run the test.");
                    }
                };
                worker.execute();
            } else {
                statusLabel.setText("Training canceled.");
            }
        });

        // Testing Action
        testButton.addActionListener(e -> {
            JFileChooser directoryChooser = new JFileChooser();
            directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            directoryChooser.setCurrentDirectory(new File("."));

            int returnValue = directoryChooser.showOpenDialog(frame);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedTestDir = directoryChooser.getSelectedFile();
                startLoading("Testing in progress...");
                SwingWorker<Void, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() {
                        List<TestFile> results = detector.testModel(selectedTestDir);
                        updateResultsTable(results);
                        return null;
                    }

                    @Override
                    protected void done() {
                        stopLoading("Testing complete!");
                    }
                };
                worker.execute();
            } else {
                statusLabel.setText("Testing canceled.");
            }
        });

        // Add components to main panel
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statusPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    /**
     * Updates the results table with new test data.
     */
    private void updateResultsTable(List<TestFile> results) {
        tableModel.setRowCount(0);
        double accuracy = detector.calcAccuracy();
        double precision = detector.calcPrecision();

        for (TestFile file : results) {
            String prediction = file.getSpamProbability() >= 0.5 ? "Spam" : "Ham";
            tableModel.addRow(new Object[]{
                    file.getFilename(),
                    file.getSpamProbRounded(),
                    file.getActualClass(),
                    prediction
            });
        }

        accuracyLabel.setText("Accuracy: " + String.format("%.2f", accuracy) + "%");
        precisionLabel.setText("Precision: " + String.format("%.2f", precision) + "%");
    }

    /**
     * Creates a styled button with hover effects.
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(41, 128, 185));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(52, 152, 219));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(41, 128, 185));
            }
        });

        return button;
    }

    /**
     * Starts loading animation with progress bar.
     */
    private void startLoading(String message) {
        statusLabel.setText(message);
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);
    }

    /**
     * Stops loading animation and updates status message.
     */
    private void stopLoading(String message) {
        statusLabel.setText(message);
        progressBar.setVisible(false);
        progressBar.setIndeterminate(false);
    }
}
