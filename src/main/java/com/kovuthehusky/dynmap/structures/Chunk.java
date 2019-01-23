package com.kovuthehusky.dynmap.structures;

import java.io.Serializable;
import java.util.Objects;

public class Chunk implements Serializable {
    String world;
    int x;
    int z;

    Chunk(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    @Override public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        final Chunk other = (Chunk) obj;

        if (!this.world.equalsIgnoreCase(other.world))
            return false;

        if (this.x != other.x)
            return false;

        if (this.z != other.z)
            return false;

        return true;
    }

    @Override public int hashCode() {
        return Objects.hash(world, x, z);
    }
}
