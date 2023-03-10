package com.gyr.milvusactual.common.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * byte转换工具
 *
 * @author guoyr
 */
public class ByteUtils {

    /**
     * 将byte数组数据转成float
     * @param bytes arr
     * @return float集合
     */
    public static List<Float> byteArrayToFloatList(byte[] bytes) {
        //4个byte可转为1个float
        List<Float> d = new ArrayList<>(bytes.length / 4);
        byte[] floatBuffer = new byte[4];
        for (int i = 0; i < bytes.length; i += 4) {
            System.arraycopy(bytes, i, floatBuffer, 0, floatBuffer.length);
            d.add(bytes2Float(floatBuffer));
        }
        return d;
    }

    /**
     * 将byte转成float
     * @param arr arr
     * @return float
     */
    private static float bytes2Float(byte[] arr) {
        int accum = 0;
        accum = accum | (arr[0] & 0xff) << 0;
        accum = accum | (arr[1] & 0xff) << 8;
        accum = accum | (arr[2] & 0xff) << 16;
        accum = accum | (arr[3] & 0xff) << 24;
        return Float.intBitsToFloat(accum);
    }

    /**
     * 将byte数组数据转成double
     * @param bytes arr
     * @return double集合
     */
    public static List<Double> byteArrayToDoubleList(byte[] bytes) {
        //8个byte可转为1个float
        List<Double> d = new ArrayList<>(bytes.length / 8);
        byte[] doubleBuffer = new byte[8];
        for (int i = 0; i < bytes.length; i += 8) {
            System.arraycopy(bytes, i, doubleBuffer, 0, doubleBuffer.length);
            d.add(bytes2Double(doubleBuffer));
        }
        return d;
    }

    /**
     * 将byte转成double
     * @param arr arr
     * @return double
     */
    public static double bytes2Double(byte[] arr) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }


    public static void main(String[] args) throws IOException {
        //byte长度2048,转float后512维
        String featureStr = "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";
        byte[] bytes = Base64Utils.Base2byteArray(featureStr);
        List<Float> floats = ByteUtils.byteArrayToFloatList(bytes);
        System.out.println(floats.size());
    }
}
