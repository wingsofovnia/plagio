package ua.edu.sumdu.dl.pojo;

import java.io.Serializable;

/**
 * @author superuser
 *         Created 20-Apr-15
 */
public class Result implements Serializable {
    private DocumentMeta metadata;
    private int coincidences;
    private double duplicationLevel;

    public Result(DocumentMeta metadata, int coincidences) {
        this.metadata = metadata;
        this.coincidences = coincidences;

        int shinglesAmount = metadata.getShinglesAmount();
        this.duplicationLevel = (double) coincidences / shinglesAmount * 100.0;
    }

    public Result(DocumentMeta metadata, int coincidences, double duplicationLevel) {
        this.metadata = metadata;
        this.coincidences = coincidences;
        this.duplicationLevel = duplicationLevel;
    }

    public DocumentMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(DocumentMeta metadata) {
        this.metadata = metadata;
    }

    public int getCoincidences() {
        return coincidences;
    }

    public void setCoincidences(int coincidences) {
        this.coincidences = coincidences;
    }

    public double getDuplicationLevel() {
        return duplicationLevel;
    }

    public void setDuplicationLevel(double duplicationLevel) {
        this.duplicationLevel = duplicationLevel;
    }

    @Override
    public String toString() {
        return "Result{" +
                "metadata=" + metadata.toString() +
                ", coincidences=" + coincidences +
                ", duplicationLevel=" + duplicationLevel +
                '}';
    }
}
