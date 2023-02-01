package com.gyr.milvusactual.controller;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.gyr.milvusactual.dao.MilvusService;
import com.gyr.milvusactual.entity.*;
import com.gyr.milvusactual.pool.ThreadPoolContextManager;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.*;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.GetCollectionStatisticsParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.GetCollStatResponseWrapper;
import io.milvus.response.SearchResultsWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoyr
 * @description milvus相关接口性能测试接口
 * @date 2022/2/17 10:52
 */
@RestController
@RequestMapping("/test/performance")
@Slf4j
public class LoadRunnerTestController {


    @Autowired
    private MilvusService milvusService;

    @Autowired
    private MilvusServiceClient milvusServiceClient;


    /**
     *
     */
    @PostMapping(value = "/collection/load")
    public String loadCollection(@RequestParam String collectionName) {

        if (StringUtils.isBlank(collectionName)) {
            return "集合名不能为空！";
        }
        R<RpcStatus> rpcStatusR = milvusService.CollectionManage().loadCollection(collectionName);
        log.info("加载集合至内存,response:{}", rpcStatusR.toString());

        return rpcStatusR.toString();
    }

    /**
     *
     */
    @PostMapping(value = "/collection/release")
    public String releaseCollection(@RequestParam String collectionName) {

        if (StringUtils.isBlank(collectionName)) {
            return "集合名不能为空！";
        }
        R<RpcStatus> rpcStatusR = milvusService.CollectionManage().releaseCollection(collectionName);
        log.info("加载集合至内存,response:{}", rpcStatusR.toString());

        return rpcStatusR.toString();
    }

    /**
     *
     */
    @PostMapping(value = "/collection/search")
    public Object search(@RequestBody SearchReqParam searchReqParam) {
        String collectionName = searchReqParam.getCollectionName();
        String vectorFieldName = searchReqParam.getVectorFieldName();
        List<List<Float>> featureVector = searchReqParam.getFeatureVector();
        List<String> searchOutputFields = searchReqParam.getSearchOutputFields();


//        R<RpcStatus> rpcStatusR = milvusService.CollectionManage().loadCollection(collectionName);
//        log.info("加载集合至内存", rpcStatusR.toString());


        if (StringUtils.isBlank(collectionName)) {
            return "请输入集合名";
        }
        if (StringUtils.isBlank(vectorFieldName)) {
            return "请指定向量字段名";
        }
        if (CollectionUtil.isEmpty(featureVector)) {
            return "请输入特征向量";
        }
        if (CollectionUtil.isEmpty(searchOutputFields)) {
            return "请输入要输出的字段";
        }


        R<SearchResults> respSearch = dataSearch(searchReqParam);

        SearchResults data = respSearch.getData();
        SearchResultData results = data.getResults();

        //打印查询结果
        List<SearchResultVo> resultVos = new ArrayList<>();
        SearchResultsWrapper searchResultsWrapper = new SearchResultsWrapper(results);
        for (int i = 0; i < featureVector.size(); ++i) {
            List<SearchResultsWrapper.IDScore> scores = searchResultsWrapper.getIDScore(i);

            List<?> gridIddData = searchResultsWrapper.getFieldData("grid_id", i);
            List<?> captureTimeData = searchResultsWrapper.getFieldData("capture_time", i);

            for (int j = 0; j < scores.size(); j++) {
                SearchResultsWrapper.IDScore idScore = scores.get(j);
                log.info(Passerby.Field.FACE_ID + ":" + idScore.getLongID() + "         grid_id:" + gridIddData.get(j) +
                        "          capture_time:" + captureTimeData.get(j) + "         distance:" + idScore.getScore());
                SearchResultVo resultVo = new SearchResultVo();
                resultVo.setFaceId(idScore.getLongID());
                resultVo.setGridId((Long) gridIddData.get(j));
                resultVo.setCaptureTime((Long) captureTimeData.get(i));
                resultVo.setScore(idScore.getScore());
                resultVos.add(resultVo);
            }
        }
        return resultVos;
    }

    @RequestMapping(value = "/vector/insert")
    public void insert() {


    }


    /**
     *
     */
    @PostMapping(value = "/collection/size")
    public String sizeCollection(@RequestParam String collectionName) {

        if (StringUtils.isBlank(collectionName)) {
            return "集合名不能为空！";
        }
        long l = collectionStatistics(collectionName);

        return "集合大小：" + l;
    }


    /**
     * 造向量数据数据
     */
    @PostMapping(value = "/collection/mock/vector")
    public String mockData(@RequestBody MockReqParam mockReqParam) {
        Integer dim = mockReqParam.getDim();
        long num = mockReqParam.getNum() != null ? mockReqParam.getNum() : 0L;
        Long addNum = mockReqParam.getAddNum();
        Long gridNum = mockReqParam.getGridNum();
        String collectionName = mockReqParam.getCollectionName();
        String partitionName = mockReqParam.getPartitionName();

        Random ran = new Random();
        long beginTime = DateUtil.beginOfDay(new Date()).getTime();
        long endTime = DateUtil.endOfDay(DateUtil.beginOfDay(new Date())).getTime();
        long todayTime = endTime - beginTime;


        //创建集合
        CreateCollectionParam createCollectionReq = getCreateCollectionParam(collectionName, "库性能测试", dim);
        milvusService.CollectionManage().createCollection(createCollectionReq);
        //创建分区
        milvusService.partitionManage().createPartition(collectionName, partitionName);
        //创建索引
        createIndex(collectionName);

        new Thread(() -> {
            long begin = System.currentTimeMillis();
            insertData(dim, num, addNum, gridNum, collectionName, partitionName, ran, beginTime, (int) todayTime);
            log.info("总耗时：{}", (System.currentTimeMillis() - begin));
        }, "insertData").start();

        return "正在入库中......";
    }

    private void insertData(Integer dim, long num, Long addNum, Long gridNum, String collectionName, String partitionName, Random ran, long beginTime, int todayTime) {
        int count = 1;
        //入数据
        List<FaceTest> faceTestList = new ArrayList<>();
        for (long i = num; i < addNum; i++) {
            List<Float> normalization = getNormalizationVector(dim);

            long time = ran.nextInt(todayTime);

            FaceTest face = new FaceTest();
            face.setFaceId(i);
            face.setGridId((i % gridNum));
            face.setCaptureTime(new Date(beginTime + time).getTime());
            face.setFeature(normalization);
            faceTestList.add(face);
            if (count == 1000 || i == (addNum - 1)) {
                count = 1;
                List<FaceTest> clone = ObjectUtil.clone(faceTestList);
                int size = ThreadPoolContextManager.insertPoolQueue.remainingCapacity();
                if (size > 0) {
                    ThreadPoolContextManager.insertThreadPool.execute(() -> {
                        log.info("insertThreadPool, 队列长度剩余: " + size);
                        //入库
                        long start = System.currentTimeMillis();
                        R<MutationResult> respInsert = dataInsert(collectionName, partitionName, clone);
                        long end = System.currentTimeMillis();
                        if (respInsert.getData() != null && respInsert.getStatus() == 0) {
                            log.info("插入成功,,耗时：{}", (end - start));
                        } else {
                            log.error("插入失败,response:{}", respInsert.toString());
                        }
                    });
                } else {
                    log.warn("推送队列已满，丢弃:{}", JSONUtil.toJsonStr(clone));
                }
                faceTestList.clear();
            } else {
                count++;
            }
        }
    }


    /**
     * 进行混合向量搜索
     */
    private R<SearchResults> dataSearch(SearchReqParam searchReqParam) {
        String collectionName = searchReqParam.getCollectionName();
        String vectorFieldName = searchReqParam.getVectorFieldName();
        List<List<Float>> featureVector = searchReqParam.getFeatureVector();
        List<String> searchOutputFields = searchReqParam.getSearchOutputFields();
        Integer topK = searchReqParam.getTopK();
        MetricType metricType = searchReqParam.getMetricType();
        String exr = searchReqParam.getExr();
        String searchParam = searchReqParam.getSearchParam();
        List<String> partitionNames = searchReqParam.getPartitionName();

        int topN = topK == null || topK <= 0 ? 100 : topK;

        //将向量搜索的范围限制在标量字段face_id值<=1000范围内的向量
        SearchParam.Builder builder = SearchParam.newBuilder();
        //集合名称
        builder.withCollectionName(collectionName);
        //向量
        builder.withVectors(featureVector);
        //向量字段名
        builder.withVectorFieldName(vectorFieldName);
        //输出字段
        builder.withOutFields(searchOutputFields);
        //topk
        builder.withTopK(topN);

        if (StringUtils.isNotBlank(exr)) {
            //布尔表达式
            builder.withExpr(exr);
        }

        //指标类型
        if (metricType != null) {
            builder.withMetricType(metricType);
        }
        if (StringUtils.isNotBlank(searchParam)) {
            builder.withParams(searchParam);
        }
        //分区列表
        if (CollectionUtil.isNotEmpty(partitionNames)) {
            builder.withPartitionNames(partitionNames);
        }

        R<SearchResults> respSearch = milvusServiceClient.search(builder.build());

        return respSearch;
    }


    private long collectionStatistics(String collectionName) {
        //查看集合大小
        R<GetCollectionStatisticsResponse> respCollectionStatistics = milvusServiceClient.getCollectionStatistics(
                GetCollectionStatisticsParam.newBuilder()
                        .withCollectionName(collectionName)
                        .build());
        GetCollStatResponseWrapper wrapperCollectionStatistics = new GetCollStatResponseWrapper(respCollectionStatistics.getData());
        System.out.println("集合行数: " + wrapperCollectionStatistics.getRowCount());
        return wrapperCollectionStatistics.getRowCount();
    }

    /**
     * 创建索引
     *
     * @param collectionName
     */
    private void createIndex(String collectionName) {


        /*注：
            Milvus 支持的大多数向量索引类型都使用近似最近邻搜索（ANNS）。
            与通常非常耗时的准确检索相比，ANNS 的核心思想不再局限于返回最准确的结果，而只是搜索目标的邻居。
             IVF_FLAT  基于量化的索引    1、高速查询2、需要尽可能高的召回率

            相似度指标：在 Milvus 中，相似性度量用于衡量向量之间的相似性。选择一个好的距离度量有助于显着提高分类和聚类性能。
            浮点嵌入的选择以下两种指标：
            L2:欧几里得距离 (L2)
            IP:内积 (IP)
            。。。
         */
        //创建索引
        final String INDEX_PARAM = "{\"nlist\":880}";     //索引构建的参数nlist（集群单元数），表示每个segment下的单元数,建议值：4*sqrt(n),n指segment最多包含多少条数据

        R<RpcStatus> index = milvusServiceClient.createIndex(
                CreateIndexParam.newBuilder()
                        .withCollectionName(collectionName)
                        .withFieldName("face_feature")         //字段名
                        .withIndexType(IndexType.IVF_FLAT)        //索引类型，
                        .withMetricType(MetricType.L2)    //设置指标类型，距离的计算方式
                        .withExtraParam(INDEX_PARAM)      //外加参数
                        .withSyncMode(Boolean.FALSE)      //同步模式，默认为true
                        .withSyncWaitingInterval(500L)    //同步等待间隔，默认500毫秒
                        .withSyncWaitingTimeout(600L)      //同步等待超时，默认600秒
                        .build());

        System.out.println("创建指定集合索引，response：" + index.toString());
    }

    /**
     * 数据插入
     */
    private R<MutationResult> dataInsert(String collectionName, String partitionName, List<FaceTest> faceTestList) {

        //创建集合是的face_id
        List<Long> face_id_array = new ArrayList<>();
        List<Long> grid_id_array = new ArrayList<>();
        List<Long> capture_time_array = new ArrayList<>();
        //feature
        List<List<Float>> feature_array = new ArrayList<>();

        for (FaceTest face : faceTestList) {
            face_id_array.add(face.getFaceId());

            grid_id_array.add(face.getGridId());
            capture_time_array.add(face.getCaptureTime());
            //向量
            feature_array.add(face.getFeature());
        }

        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("face_id", DataType.Int64, face_id_array));
        fields.add(new InsertParam.Field("grid_id", DataType.Int64, grid_id_array));
        fields.add(new InsertParam.Field("capture_time", DataType.Int64, capture_time_array));
        fields.add(new InsertParam.Field("face_feature", DataType.FloatVector, feature_array));
        //入库对象构建
        InsertParam insertParam = InsertParam.newBuilder()
                .withCollectionName(collectionName)
                .withPartitionName(partitionName)
                .withFields(fields)
                .build();
        R<MutationResult> respInsert = milvusServiceClient.insert(insertParam);
        return respInsert;
    }

    /**
     * 归一化向量
     *
     * @param dim
     * @return
     */
    private static List<Float> getNormalizationVector(long dim) {
        Random ran = new Random();
        //随机生成向量
        List<Integer> vector = new ArrayList<>();
        for (long j = 0; j < dim; j++) {
            vector.add(ran.nextInt());
        }
        //平方和开根号求模长
        double length = Math.sqrt(vector.stream().mapToDouble(x -> Math.pow(x, 2)).sum());

        //归一化
        List<Float> normalization = vector.stream().map(x -> {
            String s = String.valueOf(x / length);
            return Float.valueOf(s);
        }).collect(Collectors.toList());

        return normalization;
    }


    /**
     * 集合表结构
     *
     * @return
     */
    private CreateCollectionParam getCreateCollectionParam(String collectionName, String description, Integer dim) {

        //face_id
        FieldType fieldType1 = FieldType.newBuilder()
                .withName("face_id")               //创建的字段名称
                .withDataType(DataType.Int64)     //创建的数据类型
                .withPrimaryKey(true)             //是否作为主键
                .withAutoID(false)                //是否自动ID（主键）分配
                .withDescription("face_id")
                .build();
        //gridId
        FieldType fieldType2 = FieldType.newBuilder()
                .withName("grid_id")
                .withDataType(DataType.Int64)
                .withDescription("grid_id")
                .build();

        //captureTime
        FieldType fieldType3 = FieldType.newBuilder()
                .withName("capture_time")
                .withDataType(DataType.Int64)
                .withDescription("capture_time")
                .build();

        //feature
        FieldType fieldType4 = FieldType.newBuilder()
                .withName("face_feature")
                .withDataType(DataType.FloatVector)  //浮点向量字段
                .withDimension(dim)
                .withDescription("face_feature")//向量维度，这里表示一个名为feature的二维浮点向量字段
                .build();

        //集合对象
        CreateCollectionParam createCollectionReq = CreateCollectionParam.newBuilder()
                .withCollectionName(collectionName)             //集合名称
                .withDescription(description)           //集合描述
                .withShardsNum(2)                      //分片数量，这里表示双分片
                .addFieldType(fieldType1)              //添加字段
                .addFieldType(fieldType2)
                .addFieldType(fieldType3)
                .addFieldType(fieldType4)               //或者withFieldTypes(fieldTypes)
                .build();

        return createCollectionReq;
    }


    public static void main(String[] args) {
        SearchReqParam searchReqParam = new SearchReqParam();
        searchReqParam.setSearchParam("{\"nprobe\":10}");
        searchReqParam.setExr("111");
        searchReqParam.setCollectionName("passerby_20220218");
        searchReqParam.setPartitionName(Arrays.asList("face"));
        searchReqParam.setFeatureVector(Arrays.asList(Arrays.asList(0.1f, 0.2f)));
        searchReqParam.setVectorFieldName("face_feature");
        searchReqParam.setSearchOutputFields(Arrays.asList("faceId", "gridId", "captureTime"));
        searchReqParam.setMetricType(MetricType.L2);
        searchReqParam.setTopK(10);
        JSONUtil.toJsonStr(searchReqParam);
    }

}
