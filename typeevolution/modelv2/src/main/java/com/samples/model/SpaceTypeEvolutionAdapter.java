package com.samples.model;

public interface SpaceTypeEvolutionAdapter {
    // returns the new object with modifications/copies of the data from the old object
    void convertType(Object srcObject, Object destObject);
}
