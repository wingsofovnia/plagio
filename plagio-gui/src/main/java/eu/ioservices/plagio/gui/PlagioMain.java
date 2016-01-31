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
                final PlagioGui.PlagioMode mode = plagioGui.getMode();

                System.out.println("# Started ...");
                plagioGui.disableProcessButton();
                System.out.println("  - mode: " + mode.toString());
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
                    if (mode == PlagioGui.PlagioMode.LIB_UPDATE_MODE) {
                        plagio.updateLibrary(inputPath);
                    } else {
                        boolean libraryUpdate = mode == PlagioGui.PlagioMode.UPDATE_N_REPORT_MODE;
                        final List<DuplicationReport> duplicationReports = plagio.checkDocuments(inputPath, libraryUpdate);
                        System.out.println();
                        System.out.println("# Results: ");
                        duplicationReports.stream().forEach(r -> {
                            System.out.println(String.format("Document \"%s\" (shingles = %d, coincides = %d) :: plagiarism = %.2f%% (%d/%d)",
                                    r.getMetadata().getDocumentId(),
                                    r.getMetadata().getTotalShingles(),
                                    r.getDocCoincidences(),
                                    r.getDuplicationLevel(),
                                    r.getMetadata().getTotalShingles(),
                                    r.getDocCoincidences()));
                        });
                    }
                    System.out.println("# Document analysis has been finished!");
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
