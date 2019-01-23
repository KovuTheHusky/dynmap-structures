package com.kovuthehusky.dynmap.structures;

import java.io.Serializable;
import java.util.Objects;

public class Marker implements Serializable {
    String id;
    String world;
    int x;
    int z;

    Marker(String id, String world, int x, int z) {
        this.id = id;
        this.world = world;
        this.x = x;
        this.z = z;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        final Marker other = (Marker) obj;

        if (!this.id.equalsIgnoreCase(other.id))
            return false;

        if (!this.world.equalsIgnoreCase(other.world))
            return false;

        if (this.x != other.x)
            return false;

        if (this.z != other.z)
            return false;

        return true;
    }

    @Override public int hashCode() {
        return Objects.hash(id, world, x, z);
    }
}
