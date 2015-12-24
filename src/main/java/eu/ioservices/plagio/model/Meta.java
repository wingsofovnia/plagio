package eu.ioservices.plagio.model;

import java.io.Serializable;

/**
 * Meta class describes information about document: doc's name, amount of parsed shingles and cached flag, that shows
 * whether the document has been loaded from cache.
 * <br/>
 * Meta objects are {@link java.io.Serializable} because are used in distributed environment and may be replicated
 * through Spark nodes.
 *
 * @author &lt;<a href="mailto:illia.ovchynnikov@gmail.com">illia.ovchynnikov@gmail.com</a>&gt;
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
