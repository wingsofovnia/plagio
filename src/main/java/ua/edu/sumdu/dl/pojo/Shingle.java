package ua.edu.sumdu.dl.pojo;

import java.io.Serializable;

/**
 * @author superuser
 *         Created 23-Apr-15
 */
public class Shingle implements Serializable {
    private DocumentMeta documentMeta;
    private Integer shingle;

    public Shingle(int documentId, Integer shingle) {
        this.documentMeta = new DocumentMeta(documentId);
        this.shingle = shingle;
    }

    public Shingle(int documentId, String name, Integer shingle) {
        this.documentMeta = new DocumentMeta(documentId, name);
        this.shingle = shingle;
    }

    public Shingle(int documentId, String name, boolean marked, Integer shingle) {
        this.documentMeta = new DocumentMeta(documentId, name, marked);
        this.shingle = shingle;
    }

    public Shingle(DocumentMeta documentMeta, Integer shingle) {
        this.documentMeta = documentMeta;
        this.shingle = shingle;
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

    public Integer getShingle() {
        return shingle;
    }

    public void setShingle(Integer shingle) {
        this.shingle = shingle;
    }

    public DocumentMeta getDocumentMeta() {
        return documentMeta;
    }

    public void setDocumentMeta(DocumentMeta documentMeta) {
        this.documentMeta = documentMeta;
    }
}
