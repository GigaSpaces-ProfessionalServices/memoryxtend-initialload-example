package com.samples.model;

import java.time.LocalDateTime;

public class SpaceTypeEvolutionAdapterImpl implements SpaceTypeEvolutionAdapter {
    @Override
    public void convertType(Object srcObject, Object destObject) {

        if (srcObject instanceof com.samples.model.v1.Data) {
            com.samples.model.v1.Data v1Data = (com.samples.model.v1.Data) srcObject;
            com.samples.model.v2.Data v2Data = (com.samples.model.v2.Data) destObject;

            v2Data.setId(v1Data.getId());
            v2Data.setMessage(v1Data.getMessage());
            v2Data.setProcessed(v1Data.getProcessed());

            // add value for new field
            v2Data.setTimestamp(LocalDateTime.now());
        }
    }
}
