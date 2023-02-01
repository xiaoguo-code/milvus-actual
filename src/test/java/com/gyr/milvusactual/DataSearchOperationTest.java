package com.gyr.milvusactual;

import cn.hutool.core.collection.CollectionUtil;
import com.gyr.milvusactual.entity.Passerby;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
import io.milvus.param.Constant;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.*;
import io.milvus.param.dml.QueryParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.response.QueryResultsWrapper;
import io.milvus.response.SearchResultsWrapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

/**
 * @desc:
 * @Author: guoyr
 * @Date: 2022-02-14 23:01
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {MilvusActualApplication.class})
public class DataSearchOperationTest {


    @Autowired
    private MilvusServiceClient milvusServiceClient;

    final String collectionName = "passerby_20220216";



    /**
     * 向量相似性查询--集合需加载至内存才能做检索操作，检索完成后可从内存释放该集合
     */
    @Test
    public void mainSearchTest() {

        //加载集合至内存,也可加载指定分区至内存
        loadCollection(collectionName);

//        List<String> areaList = Arrays.asList("SM", "HL", "TA", "XA", "JM", "HC");
//        List<String> areaList = Arrays.asList("HC");
        List<String> areaList = null;

        //进行向量相似性搜索
        dataSearch(collectionName, areaList);

        //混合向量相似性搜索
//        dataHybridSearch(collectionName, areaList);

        //标量查询
//        dataQuery(collectionName, areaList);

        //释放集合
        releaseCollection(collectionName);

    }







    /**
     * 释放集合
     */
    private void releaseCollection(String collectionName) {
        R<RpcStatus> respReleaseCollection = milvusServiceClient.releaseCollection(
                ReleaseCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build());
        System.out.println("释放集合出内存，response：" + respReleaseCollection.toString());
    }

    /**
     * 加载集合
     */
    private void loadCollection(String collectionName) {
        R<RpcStatus> respLoadCollection = milvusServiceClient.loadCollection(
                LoadCollectionParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build());
        System.out.println("加载集合到内存，response：" + respLoadCollection.toString());
    }


    /**
     * 进行向量搜索
     */
    private void dataSearch(String collectionName, List<String> partitionNames) {

        final String SEARCH_PARAM = "{\"nprobe\":10}";    // Params

        //指定输出字段
        List<String> search_output_fields = Arrays.asList(Passerby.Field.FACE_ID);


        //查询参数
        List<List<Float>> search_vectors = Arrays.asList(Arrays.asList(0.1f, 0.2f));
        //时间
        Integer time = 1;

        SearchParam.Builder builder = SearchParam.newBuilder()
                .withCollectionName(collectionName)//集合名称
//                .withPartitionNames(Arrays.asList(Passerby.getPartitionName(time)))   //分区列表
                .withOutFields(search_output_fields)    //指定输出字段,向量字段不支持，会打印异常信息search doesn't support vector field as output_fields
                .withTopK(10)                    //topN
                .withMetricType(MetricType.L2)         //默认 MetricType.L2 欧几里得距离
                .withVectors(search_vectors)           //向量
                .withVectorFieldName(Passerby.Field.FACE_FEATURE)           //向量字段名
                .withExpr("")                          //布尔表达式，设置该表达式后即为向量混合搜索，如下面dataHybridSearch()方法所示
                .withParams(SEARCH_PARAM)              //因为向量索引是IVF_FLAT，nprobe表示要查询的单位数，该索引建议数值为10，该数值也大，性能越差
                .withTravelTimestamp(0L)                                    //默认
                .withGuaranteeTimestamp(Constant.GUARANTEE_EVENTUALLY_TS)  //默认
                .withRoundDecimal(-1);//默认
        if (CollectionUtil.isNotEmpty(partitionNames)) {
            builder.withPartitionNames(partitionNames);  //分区列表
        }

        R<SearchResults> respSearch = milvusServiceClient.search(builder.build());
        SearchResults data = respSearch.getData();
        SearchResultData results = data.getResults();

        //打印查询结果
        SearchResultsWrapper searchResultsWrapper = new SearchResultsWrapper(results);
        for (int i = 0; i < search_vectors.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = searchResultsWrapper.getIDScore(i);

//            List<?> fieldData = searchResultsWrapper.getFieldData(Passerby.Field.QUALITY_SCORE, i);

            for (int j = 0; j < scores.size(); j++) {

                SearchResultsWrapper.IDScore idScore = scores.get(j);
                System.out.println(Passerby.Field.FACE_ID + ":" + idScore.getLongID()  + "         distance:" + idScore.getScore());
            }
        }
    }


    /**
     * 进行混合向量搜索
     */
    private void dataHybridSearch(String collectionName, List<String> partitionNames) {

        final Integer SEARCH_K = 10;                       // TopK
        final String SEARCH_PARAM = "{\"nprobe\":10}";    // Params
//        final String exr = "face_id in [100,12,300,791,1034]";
        final String exr = "lon == 12.12345 and lat == 12.123456";

        //指定输出字段
        List<String> search_output_fields = Arrays.asList(Passerby.Field.FACE_ID, Passerby.Field.QUALITY_SCORE,"lon","lat");

        //查询参数
        List<List<Float>> search_vectors = Arrays.asList(Arrays.asList(0.1f, 0.2f));

        //将向量搜索的范围限制在标量字段face_id值<=1000范围内的向量
        SearchParam.Builder builder = SearchParam.newBuilder()
                .withCollectionName(collectionName)            //集合名称
                .withOutFields(search_output_fields)
                .withTopK(SEARCH_K)                    //topN
                .withMetricType(MetricType.L2)
                .withVectors(search_vectors)           //向量 非空
                .withVectorFieldName(Passerby.Field.FACE_FEATURE)           //向量字段名
                .withExpr(exr)
                .withParams(SEARCH_PARAM);
        if (CollectionUtil.isNotEmpty(partitionNames)) {
            builder.withPartitionNames(partitionNames);  //分区列表
        }

        R<SearchResults> respSearch = milvusServiceClient.search(builder.build());

        SearchResults data = respSearch.getData();
        SearchResultData results = data.getResults();

        //打印查询结果
        SearchResultsWrapper searchResultsWrapper = new SearchResultsWrapper(results);
        for (int i = 0; i < search_vectors.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = searchResultsWrapper.getIDScore(i);

            List<?> fieldData = searchResultsWrapper.getFieldData(Passerby.Field.QUALITY_SCORE, i);

            for (int j = 0; j < scores.size(); j++) {

                SearchResultsWrapper.IDScore idScore = scores.get(j);
                System.out.println(Passerby.Field.FACE_ID + ":" + idScore.getLongID() + "         quality_score:" + fieldData.get(j) + "         distance:" + idScore.getScore());
            }
        }
    }


    /**
     * 标量查询
     */
    private void dataQuery(String collectionName, List<String> partitionNames) {

        final Integer SEARCH_K = 10;                       // TopK
        final String SEARCH_PARAM = "{\"nprobe\":10}";    // Params
//        final String exr = "face_id in [100,12,300,791,1034]";
//        final String exr = "face_id <= 1000";
        final String exr = "face_id>=3 && face_id <= 8";

        //指定输出字段
        List<String> search_output_fields = Arrays.asList(Passerby.Field.FACE_ID, Passerby.Field.QUALITY_SCORE);

        //查询参数
        List<List<Float>> search_vectors = Arrays.asList(Arrays.asList(0.1f, 0.2f));

        //将向量搜索的范围限制在标量字段face_id值<=1000范围内的向量
        QueryParam.Builder builder = QueryParam.newBuilder()
                .withCollectionName(collectionName)            //集合名称
                .withOutFields(search_output_fields)
                .withExpr(exr);

        if (CollectionUtil.isNotEmpty(partitionNames)) {
            builder.withPartitionNames(partitionNames);  //分区列表
        }

        R<QueryResults> query = milvusServiceClient.query(builder.build());

        //打印查询结果
        QueryResults data = query.getData();
        QueryResultsWrapper queryResultsWrapper = new QueryResultsWrapper(data);
        List<?> faceIdList = queryResultsWrapper.getFieldWrapper(Passerby.Field.FACE_ID).getFieldData();
        List<?> qualityScoreList = queryResultsWrapper.getFieldWrapper(Passerby.Field.QUALITY_SCORE).getFieldData();
        for (int j = 0; j < faceIdList.size(); j++) {

            System.out.println(Passerby.Field.FACE_ID + ":" + faceIdList.get(j) + "         quality_score:" + qualityScoreList.get(j));
        }

    }


}
