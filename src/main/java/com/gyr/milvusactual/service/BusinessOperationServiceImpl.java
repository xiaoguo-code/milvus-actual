package com.gyr.milvusactual.service;


import com.gyr.milvusactual.dao.VectorDbService;
import com.gyr.milvusactual.config.AlbumCollectionConfig;
import io.milvus.grpc.DataType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BusinessOperationServiceImpl implements BusinessOperationService {

    @Autowired
    private VectorDbService vectorDbService;

    @Override
    public void dropCollection(String collectionName) {
        Boolean response = vectorDbService.dropCollection(collectionName);
        System.out.println("删除集合，response：" + response);
    }

    /**
     * 创建集合
     *
     * @param collectionName
     * @param description
     */
    @Override
    public void createCollection(String collectionName, String description) {


        //判断集合是否存在
        Boolean response = vectorDbService.hasCollection(collectionName);
        System.out.println("集合是否存在，response：" + response);

        if (Boolean.FALSE.equals(response)) {
            //创建集合
            Boolean collection = vectorDbService.createCollection(collectionName);
            System.out.println("创建集合，response：" + collection.toString());
        }
    }

    @Override
    public void createPartition(String collectionName, String partitionName) {
        //判断分区是否存在,不存在则创建
        Boolean hasPartition = vectorDbService.hasPartition(collectionName, partitionName);
        //判断不存在则创建
        if (Boolean.FALSE.equals(hasPartition)) {
            Boolean partition = vectorDbService.createPartition(collectionName, partitionName);
            System.out.println("创建指定集合分区:" + partitionName + "，response：" + partition);
        }
    }

    /**
     * 集合表结构
     *
     * @return
     */
    private CreateCollectionParam getCreateCollectionParam(String collectionName, String description) {

        //id
        FieldType fieldType1 = FieldType.newBuilder()
                .withName(AlbumCollectionConfig.Field.ID)               //创建的字段名称
                .withDataType(DataType.Int64)     //创建的数据类型
                .withPrimaryKey(true)             //是否作为主键
                .withAutoID(false)                //是否自动ID（主键）分配
                .withDescription("id")
                .build();
        //name
        FieldType fieldType2 = FieldType.newBuilder()
                .withName(AlbumCollectionConfig.Field.NAME)
                .withDataType(DataType.VarChar)
                .withMaxLength(21)
                .withDescription("name")
                .build();
        //feature
        FieldType fieldType3 = FieldType.newBuilder()
                .withName(AlbumCollectionConfig.Field.FEATURE)
                .withDataType(DataType.FloatVector)  //浮点向量字段
                .withDimension(AlbumCollectionConfig.FEATURE_DIM)
                .withDescription("feature")//向量维度，这里表示一个名为feature的二维浮点向量字段
                .build();

        //集合对象
        return CreateCollectionParam.newBuilder()
                //集合名称
                .withCollectionName(collectionName)
                //集合描述
                .withDescription(description)
                //分片数量
                .withShardsNum(AlbumCollectionConfig.SHARDS_NUM)
                //添加字段或者withFieldTypes(fieldTypes)
                .addFieldType(fieldType1)
                .addFieldType(fieldType2)
                .addFieldType(fieldType3)
                .build();
    }


}
