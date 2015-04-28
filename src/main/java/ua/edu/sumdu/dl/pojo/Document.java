package ua.edu.sumdu.dl.pojo;

import java.io.Serializable;

/**
 * @author superuser
 *         Created 23-Apr-15
 */
public class Document implements Serializable {
    private DocumentMeta documentMeta;
    private Integer[] shingles;

    public Document(int documentId, String name) {
        this.documentMeta = new DocumentMeta(documentId, name);
    }

    public Document(int documentId, String name, Integer[] shingles) {
        this.documentMeta = new DocumentMeta(documentId, name);
        this.shingles = shingles;
    }

    public Document(int documentId, String name, boolean marked, Integer[] shingles) {
        this.documentMeta = new DocumentMeta(documentId, name, marked, shingles.length);
        this.shingles = shingles;
    }

    public Document(DocumentMeta documentMeta, Integer[] shingles) {
        this.documentMeta = documentMeta;
        this.documentMeta.setShinglesAmount(shingles.length);
        this.shingles = shingles;
    }

    public int getDocumentId() {
        return documentMeta.getDocumentId();
    }

    public String getName() {
        return documentMeta.getName();
    }

    public int getShinglesAmount() {
        return documentMeta.getShinglesAmount();
    }

    public void setShinglesAmount(int shinglesAmount) {
        documentMeta.setShinglesAmount(shinglesAmount);
    }

    public void setDocumentId(int documentId) {
        documentMeta.setDocumentId(documentId);
    }

    public boolean isMarked() {
        return documentMeta.isMarked();
    }

    public void setMarked(boolean marked) {
        documentMeta.setMarked(marked);
    }

    public void setName(String name) {
        documentMeta.setName(name);
    }

    public Integer[] getShingles() {
        return shingles;
    }

    public void setShingles(Integer[] shingles) {
        this.shingles = shingles;
    }

    public DocumentMeta getDocumentMeta() {
        return documentMeta;
    }

    public void setDocumentMeta(DocumentMeta documentMeta) {
        this.documentMeta = documentMeta;
    }
}
