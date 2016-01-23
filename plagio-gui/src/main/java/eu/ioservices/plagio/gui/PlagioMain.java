package eu.ioservices.plagio.gui;

import eu.ioservices.plagio.Plagio;
import eu.ioservices.plagio.PlagioConfig;
import eu.ioservices.plagio.PlagioException;
import eu.ioservices.plagio.algorithm.ShinglesAlgorithm;
import eu.ioservices.plagio.model.DuplicationReport;

import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
 */
public class PlagioMain {
    public static void main(String[] args) throws Exception {
        final PlagioGui plagioGui = new PlagioGui();

        final PrintStream swingConsole = new PrintStream(plagioGui.getOutputAreaStream());
        System.setOut(swingConsole);
        System.setErr(swingConsole);

        System.out.println("# GUI initialized.");
        plagioGui.setProcessButtonActionListener(event -> {
            final ExecutorService executorService = Executors.newSingleThreadExecutor();
            executorService.execute(() -> {
                final String sparkMasterUrl = plagioGui.getSparkMasterUrl();
                if (sparkMasterUrl.length() == 0) {
                    plagioGui.showWarningMessage("Please fill Spark Master URL field.");
                    return;
                }
                final String inputPath = plagioGui.getInputPath();
                if (inputPath.length() == 0) {
                    plagioGui.showWarningMessage("Please fill Input Documents Path field.");
                    return;
                }
                final String libPath = plagioGui.getLibPath();
                if (libPath.length() == 0) {
                    plagioGui.showWarningMessage("Please fill Library Path field.");
                    return;
                }
                final int shinglesSize = plagioGui.getShinglesSize(ShinglesAlgorithm.DEFAULT_SHINGLE_SIZE);
                final boolean normalizing = plagioGui.isNormalizing();
                final boolean libraryUpdate = plagioGui.isLibraryUpdate();

                System.out.println("# Started ...");
                plagioGui.disableProcessButton();
                System.out.println("  - libraryUpdate: " + (libraryUpdate ? "enabled" : "disabled"));
                System.out.println("  - normalizing: " + (normalizing ? "enabled" : "disabled"));
                System.out.println("  - shingleSize: " + shinglesSize);
                System.out.println("  - libPath: " + libPath);
                System.out.println("  - inputPath: " + inputPath);
                System.out.println("  - spark master: " + sparkMasterUrl);

                final PlagioConfig config = new PlagioConfig();
                config.setSparkMaster(sparkMasterUrl);
                config.setLibraryPath(libPath);
                config.setShinglesSize(shinglesSize);
                config.setNormalizing(normalizing);

                try (final Plagio plagio = new Plagio(config)) {
                    final List<DuplicationReport> duplicationReports = plagio.checkDocuments(inputPath);

                    StringBuilder reportsAsStringBuilder = new StringBuilder();
                    for (DuplicationReport report : duplicationReports) {
                        reportsAsStringBuilder.append("Document = ")
                                .append(report.getMetadata().getDocumentId())
                                .append(", duplication level = ")
                                .append((int) report.getDuplicationLevel())
                                .append("%, coincidences = ")
                                .append(report.getDocCoincidences())
                                .append(";\n");
                    }
                    System.out.println();
                    System.out.println("# Results: ");
                    System.out.println(reportsAsStringBuilder.toString());
                    System.out.println("# Document analysis has been finished!");
                    if (libraryUpdate) {
                        System.out.println();
                        System.out.println("* Updating library ... ");
                        plagio.updateLibrary(inputPath);
                        System.out.println("* Updating library finished!");
                    }
                    plagioGui.showSuccessMessage("Document analysis has been finished!");
                } catch (PlagioException e) {
                    System.out.println("# Error: " + e.getMessage());
                    plagioGui.showErrorMessage(e.getMessage());
                } finally {
                    plagioGui.enableProcessButton();
                }
            });
        });

        plagioGui.show();
    }
}
