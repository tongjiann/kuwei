package com.xiw.kuwei.detector;

import java.time.LocalDate;

/**
 * 背离识别结果
 */
public class DivergenceResult {

    public final Type type;

    public final int priceIndex1;

    public final int priceIndex2;

    public final int difIndex1;

    public final int difIndex2;

    public final double priceVal1;

    public final double priceVal2;

    public final double difVal1;

    public final double difVal2;

    public final double strength;

    // 👇 新增日期字段
    public final LocalDate startDate;   // 第一个价格极值点日期

    public final LocalDate endDate;     // 第二个价格极值点日期

    public DivergenceResult(Type type,
                            int priceIndex1, int priceIndex2,
                            int difIndex1, int difIndex2,
                            double priceVal1, double priceVal2,
                            double difVal1, double difVal2,
                            LocalDate startDate, LocalDate endDate) {
        this.type = type;
        this.priceIndex1 = priceIndex1;
        this.priceIndex2 = priceIndex2;
        this.difIndex1 = difIndex1;
        this.difIndex2 = difIndex2;
        this.priceVal1 = priceVal1;
        this.priceVal2 = priceVal2;
        this.difVal1 = difVal1;
        this.difVal2 = difVal2;
        this.startDate = startDate;
        this.endDate = endDate;
        this.strength = calculateStrength();
    }

    private double calculateStrength() {
        double priceDiff = Math.abs(priceVal2 - priceVal1);
        double difDiff = Math.abs(difVal2 - difVal1);
        if (priceDiff < 1e-9) return 0;
        double strength = Math.abs(priceDiff - difDiff) / priceDiff * 100;
        return Math.min(100, Math.max(0, strength));
    }

    @Override
    public String toString() {
        return String.format("%s背离: 价格[%s(%d)->%s(%d)] %.2f->%.2f, DIF[%d->%d] %.4f->%.4f, 强度=%.1f%%",
                type == Type.TOP ? "顶" : "底",
                startDate, priceIndex1, endDate, priceIndex2,
                priceVal1, priceVal2,
                difIndex1, difIndex2, difVal1, difVal2, strength);
    }

    public enum Type {TOP, BOTTOM}

}