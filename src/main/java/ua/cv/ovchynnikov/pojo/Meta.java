package ua.cv.ovchynnikov.pojo;

import java.io.Serializable;

/**
 * @author superuser
 *         Created 14-Aug-15
 */
public class Meta implements Serializable {
    private final String name;
    private final int shinglesAmt;
    private boolean cached = false;

    public Meta(String name, int shinglesAmt) {
        if (name == null)
            throw new IllegalArgumentException("Name is null");
        this.name = name;
        this.shinglesAmt = shinglesAmt;
    }

    public Meta(String name, int shinglesAmt, boolean cached) {
        this(name, shinglesAmt);
        this.cached = cached;
    }

    public String getName() {
        return name;
    }

    public int getShinglesAmt() {
        return shinglesAmt;
    }

    public boolean isCached() {
        return cached;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Meta)) {
            return false;
        }

        Meta meta = (Meta) o;

        if (cached != meta.cached) {
            return false;
        }
        if (shinglesAmt != meta.shinglesAmt) {
            return false;
        }
        if (!name.equals(meta.name)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + shinglesAmt;
        result = 31 * result + (cached ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Meta{" +
                "name='" + name + '\'' +
                ", shinglesAmt=" + shinglesAmt +
                ", cached=" + cached +
                '}';
    }
}
