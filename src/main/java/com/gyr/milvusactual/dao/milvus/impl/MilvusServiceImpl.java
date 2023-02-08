package com.gyr.milvusactual.dao.milvus.impl;


import com.gyr.milvusactual.config.AlbumCollectionConfig;
import com.gyr.milvusactual.dao.VectorDbService;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.alias.AlterAliasParam;
import io.milvus.param.alias.CreateAliasParam;
import io.milvus.param.alias.DropAliasParam;
import io.milvus.param.collection.*;
import io.milvus.param.control.GetCompactionStateParam;
import io.milvus.param.dml.DeleteParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.DescribeIndexParam;
import io.milvus.param.partition.CreatePartitionParam;
import io.milvus.param.partition.DropPartitionParam;
import io.milvus.param.partition.HasPartitionParam;
import io.milvus.param.partition.ShowPartitionsParam;
import io.milvus.response.DescCollResponseWrapper;
import io.milvus.response.GetCollStatResponseWrapper;
import io.milvus.response.SearchResultsWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class MilvusServiceImpl implements VectorDbService {

    @Autowired
    private GenericObjectPool<MilvusServiceClient> milvusServiceClientGenericObjectPool;

    /**
     * 集合是否存在
     *
     * @param collectionName
     * @return
     */
    @Override
    public Boolean hasCollection(String collectionName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<Boolean> result = milvusServiceClient.hasCollection(
                    HasCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build());
            boolean isExist = result.getStatus() == 0 && (result.getData() != null && result.getData());
            log.info("集合{}是否存在:{}", collectionName, isExist);
            return isExist;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return false;
    }

    /**
     * 获取集合列表
     *
     * @return
     */
    @Override
    public R<ShowCollectionsResponse> showCollections() {
        return null;
    }

    /**
     * 获取集合详情
     *
     * @param collectionName
     * @return
     */
    @Override
    public DescCollResponseWrapper describeCollection(String collectionName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<DescribeCollectionResponse> result = milvusServiceClient.describeCollection(          //返回集合名称和分配信息
                    DescribeCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build());
            boolean isSuccess = result.getStatus() == 0;
            DescCollResponseWrapper wrapperDescribeCollection = new DescCollResponseWrapper(result.getData());
            log.info("集合{}详情获取结果:{}，详情信息:{}", collectionName, isSuccess, wrapperDescribeCollection);
            return wrapperDescribeCollection;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return null;
    }

    /**
     * 获取集合大小
     *
     * @param collectionName
     * @return
     */
    @Override
    public long getCollectionStatistics(String collectionName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<GetCollectionStatisticsResponse> result = milvusServiceClient.getCollectionStatistics(   // Return the statistics information of the collectionName.
                    GetCollectionStatisticsParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build());
            boolean isSuccess = result.getStatus() == 0;
            GetCollStatResponseWrapper wrapperCollectionStatistics = new GetCollStatResponseWrapper(result.getData());
            long rowCount = wrapperCollectionStatistics.getRowCount();
            log.info("集合{}大小获取结果:{}，Collection row count:{}", collectionName, isSuccess, rowCount);
            return rowCount;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return 0L;

    }

    /**
     * 创建集合
     *
     * @param collectionName
     */
    @Override
    public Boolean createCollection(String collectionName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            CreateCollectionParam createCollectionReq = AlbumCollectionConfig.getCollectionTemplate(collectionName);
            R<RpcStatus> result = milvusServiceClient.createCollection(createCollectionReq);
            result.getStatus();
            boolean isSuccess = result.getStatus() == 0;
            log.info("集合{}创建结果:{}", collectionName, isSuccess);
            return isSuccess;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return false;
    }

    /**
     * 删除集合
     *
     * @param collectionName
     * @return
     */
    @Override
    public Boolean dropCollection(String collectionName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<RpcStatus> result = milvusServiceClient.dropCollection(
                    DropCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build());
            boolean isSuccess = result.getStatus() == 0;
            log.info("集合{}删除结果:{}", collectionName, isSuccess);
            return isSuccess;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return false;
    }

    /**
     * 创建集合别名
     *
     * @param collectionName
     * @param aliasName
     */
    @Override
    public Boolean createAlias(String collectionName, String aliasName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<RpcStatus> result = milvusServiceClient.createAlias(
                    CreateAliasParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withAlias(aliasName)
                            .build());
            boolean isSuccess = result.getStatus() == 0;
            log.info("集合{}别名:{}创建结果:{}", collectionName, aliasName, isSuccess);
            return isSuccess;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return false;
    }

    /**
     * 修改集合别名
     *
     * @param collectionName
     * @param aliasName
     * @return
     */
    @Override
    public Boolean alterAlias(String collectionName, String aliasName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<RpcStatus> result = milvusServiceClient.alterAlias(
                    AlterAliasParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withAlias(aliasName)
                            .build());
            boolean isSuccess = result.getStatus() == 0;
            log.info("集合{}别名:{}修改结果:{}", collectionName, aliasName, isSuccess);
            return isSuccess;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return false;
    }

    /**
     * 删除集合别名
     *
     * @param collectionName
     * @param aliasName
     * @return
     */
    @Override
    public Boolean dropAlias(String collectionName, String aliasName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<RpcStatus> result = milvusServiceClient.dropAlias(
                    DropAliasParam.newBuilder()
                            .withAlias(collectionName)
                            .build());
            boolean isSuccess = result.getStatus() == 0;
            log.info("集合{}别名:{}删除结果:{}", collectionName, aliasName, isSuccess);
            return isSuccess;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return false;
    }

    /**
     * 加载集合到内存里
     *
     * @param collectionName
     * @return
     */
    @Override
    public Boolean loadCollection(String collectionName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<RpcStatus> result = milvusServiceClient.loadCollection(
                    LoadCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build());
            boolean isSuccess = result.getStatus() == 0;
            log.info("集合{}加载结果:{}", collectionName, isSuccess);
            return isSuccess;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return false;
    }

    /**
     * 释放集合--从内存中释放集合以减少内存使用
     *
     * @param collectionName
     * @return
     */
    @Override
    public Boolean releaseCollection(String collectionName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<RpcStatus> result = milvusServiceClient.releaseCollection(
                    ReleaseCollectionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build());
            boolean isSuccess = result.getStatus() == 0;
            log.info("集合{}卸载结果:{}", collectionName, isSuccess);
            return isSuccess;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return false;
    }

    /*----------------------------------------------------------------------*/

    /**
     * 创建分区
     *
     * @param collectionName
     * @param partitionName
     * @return
     */
    @Override
    public Boolean createPartition(String collectionName, String partitionName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<RpcStatus> result = milvusServiceClient.createPartition(
                    CreatePartitionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withPartitionName(partitionName)
                            .build());
            boolean isSuccess = result.getStatus() == 0;
            log.info("集合{}分区{}创建结果:{}", collectionName, partitionName, isSuccess);
            return isSuccess;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return false;
    }

    /**
     * 验证分区是否存在
     *
     * @param collectionName
     * @param partitionName
     * @return
     */
    @Override
    public Boolean hasPartition(String collectionName, String partitionName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<Boolean> result = milvusServiceClient.hasPartition(
                    HasPartitionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withPartitionName(partitionName)
                            .build());
            boolean isExist = result.getStatus() == 0 && (result.getData() != null && result.getData());
            log.info("集合{}分区{}是否存在:{}", collectionName, partitionName, isExist);
            return isExist;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return false;
    }

    /**
     * 获取集合的所有分区
     *
     * @param collectionName
     * @return
     */
    @Override
    public ShowPartitionsResponse showPartitions(String collectionName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<ShowPartitionsResponse> result = milvusServiceClient.showPartitions(
                    ShowPartitionsParam.newBuilder()
                            .withCollectionName(collectionName)
                            .build());
            boolean isSuccess = result.getStatus() == 0;
            log.info("集合{}分区获取结果:{},分区信息:{}", collectionName, isSuccess, result.getData());
            return isSuccess ? result.getData() : null;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return null;
    }

    /**
     * 删除指定集合中的分区
     *
     * @param collectionName
     * @param partitionName
     * @return
     */
    @Override
    public Boolean dropPartition(String collectionName, String partitionName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<RpcStatus> result = milvusServiceClient.dropPartition(
                    DropPartitionParam.newBuilder()
                            .withCollectionName(collectionName)
                            .withPartitionName(partitionName)
                            .build());
            boolean isSuccess = result.getStatus() == 0;
            log.info("集合{}分区{}删除结果:{}", collectionName, partitionName, isSuccess);
            return isSuccess;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return false;
    }


    /*----------------------------------------------------------------------*/


    // 根据向量搜索数据
    @Override
    public List<?> searchByFeature(String collection, List<List<Float>> search_vectors) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            List<String> search_output_fields = Arrays.asList(AlbumCollectionConfig.Field.ID, AlbumCollectionConfig.Field.NAME);
            SearchParam searchParam = SearchParam.newBuilder()
                    .withCollectionName(collection)
                    .withPartitionNames(AlbumCollectionConfig.getAllPartitionName())
                    .withMetricType(MetricType.L2)
                    .withOutFields(search_output_fields)
                    .withTopK(AlbumCollectionConfig.SEARCH_K)
                    .withVectors(search_vectors)
                    .withVectorFieldName(AlbumCollectionConfig.Field.FEATURE)
                    .withParams(AlbumCollectionConfig.SEARCH_PARAM)
                    .build();
            R<SearchResults> respSearch = milvusServiceClient.search(searchParam);
            if (respSearch.getStatus() == 0) {
                SearchResultsWrapper wrapperSearch = new SearchResultsWrapper(respSearch.getData().getResults());
                List<?> idData = wrapperSearch.getFieldData(AlbumCollectionConfig.Field.ID, 0);
                List<?> nameData = wrapperSearch.getFieldData(AlbumCollectionConfig.Field.NAME, 0);
                return nameData;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();

        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return new ArrayList<>();
    }

    /**
     * 插入数据
     *
     * @param collectionName
     * @param partitionName
     * @param ids
     * @param names
     * @param features
     * @return
     */
    @Override
    public Long insert(String collectionName, String partitionName, List<Long> ids, List<String> names, List<List<Float>> features) {

        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            List<InsertParam.Field> fields = new ArrayList<>();
            fields.add(new InsertParam.Field(AlbumCollectionConfig.Field.ID, ids));
            fields.add(new InsertParam.Field(AlbumCollectionConfig.Field.NAME, names));
            fields.add(new InsertParam.Field(AlbumCollectionConfig.Field.FEATURE, features));
            InsertParam insertParam = InsertParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withPartitionName(partitionName)
                    .withFields(fields)
                    .build();
            R<MutationResult> insertResult = milvusServiceClient.insert(insertParam);
//            tring result = insert.getStatus().equals(0) ? "InsertRequest successfully! Total number of " +
//                    "inserts:{" + insert.getData().getInsertCnt() + "} entities" : "InsertRequest failed!";
            if (insertResult.getStatus() == 0) {
                return insertResult.getData().getIDs().getIntId().getData(0);
            } else {
                log.error("特征值入库失败");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return null;
    }

    /**
     * 删除数据
     *
     * @param collectionName
     * @param deleteExpr
     * @return
     */
    @Override
    public Boolean delete(String collectionName, String deleteExpr) {

        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<MutationResult> result = milvusServiceClient.delete(
                    DeleteParam.newBuilder()
                            .withCollectionName(collectionName)
//                        .withPartitionName("HC")
                            .withExpr(deleteExpr)
                            .build());
            boolean isSuccess = result.getStatus() == 0;
            log.info("集合{}数据删除结果:{},deleteExpr:{}", collectionName, isSuccess, deleteExpr);
            return isSuccess;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return false;
    }

    /**
     * 压缩集合
     *
     * @param collectionName
     * @return
     */
    @Override
    public R<ManualCompactionResponse> manualCompaction(String collectionName) {
        return null;
    }

    /**
     * 检查压缩状态
     *
     * @param compactionID
     * @return
     */
    @Override
    public GetCompactionStateResponse getCompactionState(long compactionID) {

        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<GetCompactionStateResponse> result = milvusServiceClient.getCompactionState(GetCompactionStateParam.newBuilder()
                    .withCompactionID(compactionID)
                    .build());
            boolean isSuccess = result.getStatus() == 0;
            log.info("compactionID:{}的压缩状态检查结果:{}", compactionID, isSuccess);
            return result.getData();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return null;
    }

    /**
     * 创建索引
     *
     * @param collectionName 集合名
     * @return true or false
     */
    @Override
    public Boolean createIndex(String collectionName) {
        MilvusServiceClient milvusServiceClient = null;
        try {
            // 通过对象池管理对象
            milvusServiceClient = milvusServiceClientGenericObjectPool.borrowObject();
            R<RpcStatus> index = milvusServiceClient.createIndex(AlbumCollectionConfig.getCreateIndexParam());
            boolean isSuccess = index.getStatus() == 0;
            R<DescribeIndexResponse> describeIndexResp = milvusServiceClient.describeIndex(
                    DescribeIndexParam.newBuilder()
                            .withCollectionName(AlbumCollectionConfig.COLLECTION_NAME)
                            .build());
            log.info("集合{}创建索引:{}", collectionName, isSuccess);
            log.info("集合{}索引详情:{}", collectionName, describeIndexResp.toString());
            return isSuccess;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            // 回收对象到对象池
            if (milvusServiceClient != null) {
                milvusServiceClientGenericObjectPool.returnObject(milvusServiceClient);
            }
        }
        return false;
    }
}
