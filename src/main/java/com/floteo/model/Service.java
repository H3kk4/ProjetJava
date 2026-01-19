package com.floteo.model;

public record Service(long id, String name) {
    @Override public String toString() { return name; }
}
