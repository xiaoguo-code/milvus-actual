package com.gyr.milvusactual.common.milvus;

import com.arcsoft.face.FaceEngine;
import com.gyr.milvusactual.common.factory.MilvusPoolFactory;
import com.gyr.milvusactual.config.AlbumCollectionConfig;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.grpc.MutationResult;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.*;
import io.milvus.param.dml.InsertParam;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.AbandonedConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// milvys 工具类
@Data
@Component
@Slf4j
public class MilvusOperateUtils {

    @Autowired
    private GenericObjectPool<MilvusServiceClient> milvusServiceClientGenericObjectPool;  // 管理链接对象的池子

    /**
     * 创建集合
     *
     * @param collection
     */
    private void createCollection(String collection) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            CreateCollectionParam createCollectionReq = AlbumCollectionConfig.getCollectionTemplate(collection);
            R<RpcStatus> result = milvusServiceClient.createCollection(createCollectionReq);
            log.info("创建结果" + result.getStatus() + "0 为成功");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }


    }


    /**
     * 加载集合
     *
     * @param collection
     */
    public void loadingCollection(String collection) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<RpcStatus> rpcStatusR = milvusServiceClient.loadCollection(
                    LoadCollectionParam.newBuilder()
                            .withCollectionName(collection)
                            .build());
            log.info("加载结果" + rpcStatusR);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }


    }

    /**
     * 卸载集合
     *
     * @param collection
     */
    public void freedCollection(String collection) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<RpcStatus> rpcStatusR = milvusServiceClient.releaseCollection(
                    ReleaseCollectionParam.newBuilder()
                            .withCollectionName(collection)
                            .build());
            log.info("加载结果" + rpcStatusR);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }


    }

    /**
     * 删除一个Collection
     *
     * @param collection
     */
    private void delCollection(String collection) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<RpcStatus> book = milvusServiceClient.dropCollection(
                    DropCollectionParam.newBuilder()
                            .withCollectionName(collection)
                            .build());
            log.info("删除" + book.getStatus() + " 0 为成功");

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }


    }

    // 插入数据 和对应的字段相同
    public long insert(String collectionName, String partitionName, List<Long> userName, List<Long> userCode, List<List<Float>> feature) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field("userName", userName));
            fields.add(new InsertParam.Field("userCode", userCode));
            fields.add(new InsertParam.Field("feature", feature));
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withPartitionName(partitionName)
                    .withFields(fields)
                    .build();
            R<MutationResult> insertResult = milvusServiceClient.insert(insertParam);
            if (insertResult.getStatus() == 0) {
                return insertResult.getData().getIDs().getIntId().getData(0);
            } else {
                log.error("特征值上传失败 加入失败队列稍后重试");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return 0;

        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return 0;
    }

    /**
     * 根据向量搜索数据
     *
     * @param collection
     * @param search_vectors
     * @return
     */
    public List<?> searchByFeature(String collection, List<List<Float>> search_vectors) {
        MilvusServiceClient milvusServiceClient = null;
        try {
//            // 通过对象池管理对象
//            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
//            List<String> search_output_fields = Arrays.asList(faceMilvus.Field.USER_CODE);
//            SearchParam searchParam = SearchParam.newBuilder()
//                    .withCollectionName(collection)
//                    .withPartitionNames(Arrays.asList("one"))
//                    .withMetricType(MetricType.L2)
//                    .withOutFields(search_output_fields)
//                    .withTopK(faceMilvus.SEARCH_K)
//                    .withVectors(search_vectors)
//                    .withVectorFieldName(faceMilvus.Field.FEATURE)
//                    .withParams(faceMilvus.SEARCH_PARAM)
//                    .build();
//            R<SearchResults> respSearch = milvusServiceClient.search(searchParam);
//            if (respSearch.getStatus() == 0) {
//                SearchResultsWrapper wrapperSearch = new SearchResultsWrapper(respSearch.getData().getResults());
//                List<?> fieldData = wrapperSearch.getFieldData(faceMilvus.Field.USER_CODE, 0);
//                return fieldData;
//            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ArrayList<>();

        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return new ArrayList<>();
    }

    public static void main(String[] args) {
        String collectionName = "face_home";
        MilvusOperateUtils milvusOperateUtils = new MilvusOperateUtils();
        milvusOperateUtils.createCollection(collectionName);
        //MilvusOperateUtils.delCollection("");
    }
}
