package csci2020u.assignment01;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class SpamDetectorGUI {
    private SpamDetector detector;
    private JFrame frame;
    private JTable resultsTable;
    private DefaultTableModel tableModel;
    private JLabel accuracyLabel;
    private JLabel precisionLabel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SpamDetectorGUI::new);
    }

    public SpamDetectorGUI() {
        frame = new JFrame("Spam Detector");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        detector = new SpamDetector();

        JPanel panel = new JPanel();
        JButton trainButton = new JButton("Train Model");
        JButton testButton = new JButton("Run Test");
        accuracyLabel = new JLabel("Accuracy: N/A");
        precisionLabel = new JLabel("Precision: N/A");

        tableModel = new DefaultTableModel(new String[]{"Filename", "Spam Probability", "Actual Class", "Prediction"}, 0);
        resultsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultsTable);

        trainButton.addActionListener(e -> {
            detector.train();
            JOptionPane.showMessageDialog(frame, "Training complete! Now run the test.");
        });

        testButton.addActionListener(e -> {
            List<TestFile> results = detector.testModel();
            updateResultsTable(results);
        });

        panel.add(trainButton);
        panel.add(testButton);
        panel.add(accuracyLabel);
        panel.add(precisionLabel);

        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }

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
}
