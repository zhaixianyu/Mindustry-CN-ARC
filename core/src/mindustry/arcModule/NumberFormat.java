package mindustry.arcModule;

import arc.util.Strings;

import static arc.util.Strings.fixed;
import static arc.util.Strings.format;

public class NumberFormat {
    private static final String defaultFormat = "@[gray]@[]";

    public static String formatInteger(long number){
        return formatInteger(number, 2, defaultFormat);
    }

    public static String formatFloat(float number){
        return formatFloat(number, 2, defaultFormat);
    }

    public static String autoFixed(float number){
        return autoFixed(number, 2, defaultFormat);
    }

    public static String formatInteger(long number, int maxDeci, String format){

        String unit;
        number = Math.abs(number);
        double fnumber;

        if (number >= 1_000_000_000_000L) {//直接比较float和long可能有精度问题，大概?
            int exponent = (int) Math.log10(number);
            float mantissa = (float) (number / Math.pow(10, exponent));
            return format(format, format("@E@", Strings.autoFixed(mantissa, 1), exponent), "");
        } else if (number >= 1_000_000_000L) {
            fnumber = number / 1E9;
            unit = "B";
        } else if (number >= 1_000_000L) {
            fnumber = number / 1E6;
            unit = "M";
        } else if (number >= 1_000L) {
            fnumber = number / 1E3;
            unit = "K";
        }
        else return format(format, number, "");
        return format(format, fixed((float) fnumber, maxDeci - (int) Math.log10(fnumber)), unit);
    }

    public static String formatFloat(float number, int maxDeci, String format){

        if(number == Float.POSITIVE_INFINITY) return "Inf";
        if(number == Float.NEGATIVE_INFINITY) return "-Inf";

        String unit = "";
        number = Math.abs(number);
        if (number >= 1E12f) {
            int exponent = (int) Math.log10(number);
            float mantissa = (float) (number / Math.pow(10, exponent));
            return format(format, format("@E@", Strings.autoFixed(mantissa, 1), exponent), "");
        } else if (number >= 1E9f) {
            number /= 1E9f;
            unit = "B";
        } else if (number >= 1E6f) {
            number /= 1E6f;
            unit = "M";
        } else if (number >= 1E3f) {
            number /= 1E3f;
            unit = "K";
        }
        if (number < 0.00001f) return format(format, Float.toString(number), unit);
        return format(format, fixed(number, maxDeci - (int) Math.log10(number)), unit);
    }

    public static String autoFixed(float number, int maxDeci, String format) {
        return formatFloat(number, maxDeci, format)//实际上就是把最后面的0都删了
                .replaceAll("(?<=\\.\\d{0,5})0+(?!\\d)|\\.0+(?!\\d)", "");
    }



    public static String percentFormat(String prefix, float cur, float max) {
        // 最通用的情况
        return percentFormat(prefix, cur, Math.abs(cur) > 0.001f, max,  cur / max < 0.9f, 2);
    }

    public static String percentFormat(String prefix, float cur, float max, int arcFixed) {
        // 用于处理血量等需要精细显示的
        return percentFormat(prefix, cur, cur > 0.0001f, max, cur / max < 0.9f, arcFixed);
    }

    public static String percentFormat(String prefix, float cur, boolean showMin, float max, boolean showPercent, int arcFixed) {
        return percentFormat(prefix, cur,showMin, max, showMin & showPercent ? buildPercent(cur, max) : "", arcFixed);
    }

    public static String percentFormat(String prefix, float cur, float max, String format) {
        // 用于有自定义百分比需求的，如热量相关
        return percentFormat(prefix, cur, cur > 0.0001f, max, format, 2);
    }

    public static String percentFormat(String prefix, float cur, boolean showMin, float max, String format, int arcFixed) {
        // 只不处理showMax
        return percentFormat(prefix, cur, showMin, max, Math.abs(cur/max - 1) > 0.01f, format, arcFixed);
    }

    public static String percentFormat(String prefix, float cur, boolean showMin, float max, boolean showMax, String format, int arcFixed) {
        // 用于建筑等的bar显示，保持统一格式
        StringBuilder text = new StringBuilder(prefix).append(" ");
        text.append(showMin ? formatFloat(cur) : "\uE815");
        if (showMax) text.append("/").append(formatFloat(max));
        text.append(format);
        return text.toString();
    }

    private static String buildPercent(float cur, float max) {
        return buildPercent(100 * cur / max);
    }

    public static String buildPercent(float percent) {
        //return " [lightgray]| " + UI.arcFixed(percent, 2) + "%";
        return " [lightgray]| " + (int)percent + "%";
    }
}
