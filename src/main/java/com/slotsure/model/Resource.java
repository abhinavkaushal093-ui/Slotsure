package com.slotsure.model;

public record Resource(
    long id,
    String name,
    String resourceType,
    String location,
    int capacity,
    boolean active
) {
    @Override
    public String toString() {
        return name + " · " + location + " · " + capacity + " seats";
    }
} 
