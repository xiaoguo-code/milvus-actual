package com.gyr.milvusactual.service;


public interface BusinessOperationService {


    void dropCollection(String collectionName);

    void createCollection(String collectionName, String description);


}
