package com.gyr.milvusactual.service;


import com.gyr.milvusactual.dao.VectorDbService;
import com.gyr.milvusactual.entity.PasserbyCollectionConfig;
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
        R<RpcStatus> response = vectorDbService.collectionManage().dropCollection(collectionName);
        System.out.println("删除集合，response：" + response.toString());
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
        R<Boolean> response = vectorDbService.collectionManage().hasCollection(collectionName);
        System.out.println("集合是否存在，response：" + response);

        if (Boolean.FALSE.equals(response.getData())) {
            //创建集合
            CreateCollectionParam createCollectionReq = getCreateCollectionParam(collectionName, description);
            R<RpcStatus> respCreateCollection = vectorDbService.collectionManage().createCollection(createCollectionReq);
            System.out.println("创建集合，response：" + respCreateCollection.toString());
        }
    }

    @Override
    public void createPartition(String collectionName, String partitionName) {
        //判断分区是否存在,不存在则创建
        R<Boolean> respHasPartition = vectorDbService.partitionManage().hasPartition(collectionName, partitionName);
        //判断不存在则创建
        if (Boolean.FALSE.equals(respHasPartition.getData())) {
            R<RpcStatus> respCreatePartition = vectorDbService.partitionManage().createPartition(collectionName, partitionName);
            System.out.println("创建指定集合分区:" + partitionName + "，response：" + respCreatePartition.toString());
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
                .withName(PasserbyCollectionConfig.Field.ID)               //创建的字段名称
                .withDataType(DataType.Int64)     //创建的数据类型
                .withPrimaryKey(true)             //是否作为主键
                .withAutoID(false)                //是否自动ID（主键）分配
                .withDescription("id")
                .build();
        //name
        FieldType fieldType2 = FieldType.newBuilder()
                .withName(PasserbyCollectionConfig.Field.NAME)
                .withDataType(DataType.VarChar)
                .withMaxLength(21)
                .withDescription("name")
                .build();
        //feature
        FieldType fieldType3 = FieldType.newBuilder()
                .withName(PasserbyCollectionConfig.Field.FEATURE)
                .withDataType(DataType.FloatVector)  //浮点向量字段
                .withDimension(PasserbyCollectionConfig.FEATURE_DIM)
                .withDescription("feature")//向量维度，这里表示一个名为feature的二维浮点向量字段
                .build();

        //集合对象
        return CreateCollectionParam.newBuilder()
                //集合名称
                .withCollectionName(collectionName)
                //集合描述
                .withDescription(description)
                //分片数量
                .withShardsNum(PasserbyCollectionConfig.SHARDS_NUM)
                //添加字段或者withFieldTypes(fieldTypes)
                .addFieldType(fieldType1)
                .addFieldType(fieldType2)
                .addFieldType(fieldType3)
                .build();
    }


}
