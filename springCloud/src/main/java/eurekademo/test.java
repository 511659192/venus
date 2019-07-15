// Copyright (C) 2019 Meituan
// All rights reserved
package eurekademo;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-03-05 16:51
 **/
public class test {

    private static String firstNameJson = "['补',\"梁\",\"欧\",\"肖\",\"付\",\"凡\",\"赵\",\"钱\",\"孙\",\"李\",\"周\",\"吴\",\"郑\",\"王\",\"冯\",\"陈\",\"褚\",\"卫\",\"蒋\",\"沈\",\"韩\",\"杨\",\"朱\",\"秦\",\"尤\",\"许\",\"何\",\"吕\",\"施\",\"张\",\"孔\",\"曹\",\"严\",\"华\",\"金\",\"魏\",\"陶\",\"姜\",\"戚\",\"谢\",\"邹\",\"喻\",\"柏\",\"水\",\"窦\",\"章\",\"云\",\"苏\",\"潘\",\"葛\",\"奚\",\"范\",\"彭\",\"郎\",\"鲁\",\"韦\",\"昌\",\"马\",\"苗\",\"凤\",\"花\",\"方\",\"俞\",\"任\",\"袁\",\"柳\",\"酆\",\"鲍\",\"史\",\"唐\",\"费\",\"廉\",\"岑\",\"薛\",\"雷\",\"贺\",\"倪\",\"汤\",\"滕\",\"殷\",\"罗\",\"毕\",\"郝\",\"邬\",\"安\",\"常\",\"乐\",\"于\",\"时\",\"傅\",\"皮\",\"卞\",\"齐\",\"康\",\"伍\",\"余\",\"元\",\"卜\",\"顾\",\"孟\",\"平\",\"黄\",\"和\",\"穆\",\"萧\",\"尹\",\"姚\",\"邵\",\"湛\",\"汪\",\"祁\",\"毛\",\"禹\",\"狄\",\"米\",\"贝\",\"明\",\"臧\",\"计\",\"伏\",\"成\",\"戴\",\"谈\",\"宋\",\"茅\",\"庞\",\"熊\",\"纪\",\"舒\",\"屈\",\"项\",\"祝\",\"董\",\"粱\",\"杜\",\"阮\",\"蓝\",\"闵\",\"席\",\"季\",\"麻\",\"强\",\"贾\",\"路\",\"娄\",\"危\",\"江\",\"童\",\"颜\",\"郭\",\"梅\",\"盛\",\"林\",\"刁\",\"钟\",\"徐\",\"邱\",\"骆\",\"高\",\"夏\",\"蔡\",\"田\",\"樊\",\"胡\",\"凌\",\"霍\",\"虞\",\"万\",\"支\",\"柯\",\"昝\",\"管\",\"卢\",\"莫\",\"经\",\"房\",\"裘\",\"缪\",\"干\",\"解\",\"应\",\"宗\",\"丁\",\"宣\",\"贲\",\"邓\",\"郁\",\"单\",\"杭\",\"洪\",\"包\",\"诸\",\"左\",\"石\",\"崔\",\"吉\",\"钮\",\"龚\",\"程\",\"嵇\",\"邢\",\"滑\",\"裴\",\"陆\",\"荣\",\"翁\",\"荀\",\"羊\",\"於\",\"惠\",\"甄\",\"麴\",\"家\",\"封\",\"芮\",\"羿\",\"储\",\"靳\",\"汲\",\"邴\",\"糜\",\"松\",\"井\",\"段\",\"富\",\"巫\",\"乌\",\"焦\",\"巴\",\"弓\",\"牧\",\"隗\",\"山\",\"谷\",\"车\",\"侯\",\"宓\",\"蓬\",\"全\",\"郗\",\"班\",\"仰\",\"秋\",\"仲\",\"伊\",\"宫\",\"宁\",\"仇\",\"栾\",\"暴\",\"甘\",\"钭\",\"厉\",\"戎\",\"祖\",\"武\",\"符\",\"刘\",\"景\",\"詹\",\"束\",\"龙\",\"叶\",\"幸\",\"司\",\"韶\",\"郜\",\"黎\",\"蓟\",\"薄\",\"印\",\"宿\",\"白\",\"怀\",\"蒲\",\"邰\",\"从\",\"鄂\",\"索\",\"咸\",\"籍\",\"赖\",\"卓\",\"蔺\",\"屠\",\"蒙\",\"池\",\"乔\",\"阴\",\"欎\",\"胥\",\"能\",\"苍\",\"双\",\"闻\",\"莘\",\"党\",\"翟\",\"谭\",\"贡\",\"劳\",\"逄\",\"姬\",\"申\",\"扶\",\"堵\",\"冉\",\"宰\",\"郦\",\"雍\",\"舄\",\"璩\",\"桑\",\"桂\",\"濮\",\"牛\",\"寿\",\"通\",\"边\",\"扈\",\"燕\",\"冀\",\"郏\",\"浦\",\"尚\",\"农\",\"温\",\"别\",\"庄\",\"晏\",\"柴\",\"瞿\",\"阎\",\"充\",\"慕\",\"连\",\"茹\",\"习\",\"宦\",\"艾\",\"鱼\",\"容\",\"向\",\"古\",\"易\",\"慎\",\"戈\",\"廖\",\"庾\",\"终\",\"暨\",\"居\",\"衡\",\"步\",\"都\",\"耿\",\"满\",\"弘\",\"匡\",\"国\",\"文\",\"寇\",\"广\",\"禄\",\"阙\",\"东\",\"殴\",\"殳\",\"沃\",\"利\",\"蔚\",\"越\",\"夔\",\"隆\",\"师\",\"巩\",\"厍\",\"聂\",\"晁\",\"勾\",\"敖\",\"融\",\"冷\",\"訾\",\"辛\",\"阚\",\"那\",\"简\",\"饶\",\"空\",\"曾\",\"毋\",\"沙\",\"乜\",\"养\",\"鞠\",\"须\",\"丰\",\"巢\",\"关\",\"蒯\",\"相\",\"查\",\"後\",\"荆\",\"红\",\"游\",\"竺\",\"权\",\"逯\",\"盖\",\"益\",\"桓\",\"公\",\"万俟\",\"司马\",\"上官\",\"欧阳\",\"夏侯\",\"诸葛\",\"闻人\",\"东方\",\"赫连\",\"皇甫\",\"尉迟\",\"公羊\",\"澹台\",\"公冶\",\"宗政\",\"濮阳\",\"淳于\",\"单于\",\"太叔\",\"申屠\",\"公孙\",\"仲孙\",\"轩辕\",\"令狐\",\"钟离\",\"宇文\",\"长孙\",\"慕容\",\"鲜于\",\"闾丘\",\"司徒\",\"司空\",\"亓官\",\"司寇\",\"仉\",\"督\",\"子车\",\"颛孙\",\"端木\",\"巫马\",\"公西\",\"漆雕\",\"乐正\",\"壤驷\",\"公良\",\"拓跋\",\"夹谷\",\"宰父\",\"谷梁\",\"晋\",\"楚\",\"闫\",\"法\",\"汝\",\"鄢\",\"涂\",\"钦\",\"段干\",\"百里\",\"东郭\",\"南门\",\"呼延\",\"归\",\"海\",\"羊舌\",\"微生\",\"岳\",\"帅\",\"缑\",\"亢\",\"况\",\"后\",\"有\",\"琴\",\"梁丘\",\"左丘\",\"东门\",\"西门\",\"商\",\"牟\",\"佘\",\"佴\",\"伯\",\"赏\",\"南宫\",\"墨\",\"哈\",\"谯\",\"笪\",\"年\",\"爱\",\"阳\",\"佟\",\"第五\",\"言\",\"福\",\"百\",\"家\",\"姓\",\"终\",\"寸姓\",\"卓\",\"蔺\",\"屠\",\"蒙\",\"池\",\"乔\",\"阳\",\"郁\",\"胥\",\"能\",\"苍\",\"双\",\"闻\",\"莘\",\"党\",\"翟\",\"谭\",\"贡\",\"劳\",\"逄\",\"姬\",\"申\",\"扶\",\"堵\",\"冉\",\"宰\",\"郦\",\"雍\",\"却\",\"璩\",\"桑\",\"桂\",\"濮\",\"牛\",\"寿\",\"通\",\"边\",\"扈\",\"燕\",\"冀\",\"僪\",\"浦\",\"尚\",\"农\",\"温\",\"别\",\"庄\",\"晏\",\"柴\",\"瞿\",\"阎\",\"充\",\"慕\",\"连\",\"茹\",\"习\",\"宦\",\"艾\",\"鱼\",\"容\",\"向\",\"古\",\"易\",\"慎\",\"戈\",\"庾\",\"终\",\"暨\",\"居\",\"衡\",\"步都\",\"耿\",\"满\",\"弘\",\"匡\",\"国\",\"文\",\"寇\",\"广\",\"禄\",\"阙\",\"东欧\",\"殳\",\"沃\",\"利\",\"蔚\",\"越\",\"夔\",\"隆\",\"师\",\"巩\",\"厍\",\"聂晁\",\"勾\",\"敖\",\"融\",\"冷\",\"訾\",\"辛\",\"阚\",\"那\",\"简\",\"饶\",\"空曾\",\"毋\",\"沙\",\"乜\",\"养\",\"鞠\",\"须\",\"丰\",\"巢\",\"关\",\"蒯\",\"相查\",\"后\",\"荆\",\"红\",\"游\",\"竺\",\"权\",\"逮\",\"盍\",\"益\",\"桓\",\"公\",\"唱\",\"万俟\",\"司马\",\"上官\",\"欧阳\",\"夏侯\",\"诸葛\",\"闻人\",\"东方\",\"赫连\",\"皇甫\",\"尉迟\",\"公羊\",\"澹台\",\"公冶\",\"宗政\",\"濮阳\",\"淳于\",\"单于\",\"太叔\",\"申屠\",\"公孙\",\"仲孙\",\"轩辕\",\"令狐\",\"钟离\",\"宇文\",\"长孙\",\"慕容\",\"司徒\",\"司空\",\"召\",\"有\",\"舜\",\"丛\",\"岳\",\"寸\",\"贰\",\"皇\",\"侨\",\"彤\",\"竭\",\"端\",\"赫\",\"实\",\"甫\",\"集\",\"象\",\"翠\",\"狂\",\"辟\",\"典\",\"良\",\"函\",\"芒\",\"苦\",\"其\",\"京\",\"中\",\"夕\",\"之\",\"蹇\",\"称\",\"诺\",\"来\",\"多\",\"繁\",\"戊\",\"朴\",\"回\",\"毓\",\"税\",\"荤\",\"靖\",\"绪\",\"愈\",\"硕\",\"牢\",\"买\",\"但\",\"巧\",\"枚\",\"撒\",\"泰\",\"秘\",\"亥\",\"绍\",\"以\",\"壬\",\"森\",\"斋\",\"释\",\"奕\",\"姒\",\"朋\",\"求\",\"羽\",\"用\",\"占\",\"真\",\"穰\",\"翦\",\"闾\",\"漆\",\"贵\",\"代\",\"贯\",\"旁\",\"崇\",\"栋\",\"告\",\"休\",\"褒\",\"谏\",\"锐\",\"皋\",\"闳\",\"在\",\"歧\",\"禾\",\"示\",\"是\",\"委\",\"钊\",\"频\",\"嬴\",\"呼\",\"大\",\"威\",\"昂\",\"律\",\"冒\",\"保\",\"系\",\"抄\",\"定\",\"化\",\"莱\",\"校\",\"么\",\"抗\",\"祢\",\"綦\",\"悟\",\"宏\",\"功\",\"庚\",\"务\",\"敏\",\"捷\",\"拱\",\"兆\",\"丑\",\"丙\",\"畅\",\"苟\",\"随\",\"类\",\"卯\",\"俟\",\"友\",\"答\",\"乙\",\"允\",\"甲\",\"留\",\"尾\",\"佼\",\"玄\",\"乘\",\"裔\",\"延\",\"植\",\"环\",\"矫\",\"赛\",\"昔\",\"侍\",\"度\",\"旷\",\"遇\",\"偶\",\"前\",\"由\",\"咎\",\"塞\",\"敛\",\"受\",\"泷\",\"袭\",\"衅\",\"叔\",\"圣\",\"御\",\"夫\",\"仆\",\"镇\",\"藩\",\"邸\",\"府\",\"掌\",\"首\",\"员\",\"焉\",\"戏\",\"可\",\"智\",\"尔\",\"凭\",\"悉\",\"进\",\"笃\",\"厚\",\"仁\",\"业\",\"肇\",\"资\",\"合\",\"仍\",\"九\",\"衷\",\"哀\",\"刑\",\"俎\",\"仵\",\"圭\",\"夷\",\"徭\",\"蛮\",\"汗\",\"孛\",\"乾\",\"帖\",\"罕\",\"洛\",\"淦\",\"洋\",\"邶\",\"郸\",\"郯\",\"邗\",\"邛\",\"剑\",\"虢\",\"隋\",\"蒿\",\"茆\",\"菅\",\"苌\",\"树\",\"桐\",\"锁\",\"钟\",\"机\",\"盘\",\"铎\",\"斛\",\"玉\",\"线\",\"针\",\"箕\",\"庹\",\"绳\",\"磨\",\"蒉\",\"瓮\",\"弭\",\"刀\",\"疏\",\"牵\",\"浑\",\"恽\",\"势\",\"世\",\"仝\",\"同\",\"蚁\",\"止\",\"戢\",\"睢\",\"冼\",\"种\",\"凃肖\",\"己\",\"泣\",\"潜\",\"卷\",\"脱\",\"谬\",\"蹉\",\"赧\",\"浮\",\"顿\",\"说\",\"次\",\"错\",\"念\",\"夙\",\"斯\",\"完\",\"丹\",\"表\",\"聊\",\"源\",\"姓\",\"吾\",\"寻\",\"展\",\"出\",\"不\",\"户\",\"闭\",\"才\",\"无\",\"书\",\"学\",\"愚\",\"本\",\"性\",\"雪\",\"霜\",\"烟\",\"寒\",\"少\",\"字\",\"桥\",\"板\",\"斐\",\"独\",\"千\",\"诗\",\"嘉\",\"扬\",\"善\",\"揭\",\"祈\",\"析\",\"赤\",\"紫\",\"青\",\"柔\",\"刚\",\"奇\",\"拜\",\"佛\",\"陀\",\"弥\",\"阿\",\"素\",\"长\",\"僧\",\"隐\",\"仙\",\"隽\",\"宇\",\"祭\",\"酒\",\"淡\",\"塔\",\"琦\",\"闪\",\"始\",\"星\",\"南\",\"天\",\"接\",\"波\",\"碧\",\"速\",\"禚\",\"腾\",\"潮\",\"镜\",\"似\",\"澄\",\"潭\",\"謇\",\"纵\",\"渠\",\"奈\",\"风\",\"春\",\"濯\",\"沐\",\"茂\",\"英\",\"兰\",\"檀\",\"藤\",\"枝\",\"检\",\"生\",\"折\",\"登\",\"驹\",\"骑\",\"貊\",\"虎\",\"肥\",\"鹿\",\"雀\",\"野\",\"禽\",\"飞\",\"节\",\"宜\",\"鲜\",\"粟\",\"栗\",\"豆\",\"帛\",\"官\",\"布\",\"衣\",\"藏\",\"宝\",\"钞\",\"银\",\"门\",\"盈\",\"庆\",\"喜\",\"及\",\"普\",\"建\",\"营\",\"巨\",\"望\",\"希\",\"道\",\"载\",\"声\",\"漫\",\"犁\",\"力\",\"贸\",\"勤\",\"革\",\"改\",\"兴\",\"亓\",\"睦\",\"修\",\"信\",\"闽\",\"北\",\"守\",\"坚\",\"勇\",\"汉\",\"练\",\"尉\",\"士\",\"旅\",\"五\",\"令\",\"将\",\"旗\",\"军\",\"行\",\"奉\",\"敬\",\"恭\",\"仪\",\"母\",\"堂\",\"丘\",\"义\",\"礼\",\"慈\",\"孝\",\"理\",\"伦\",\"卿\",\"问\",\"永\",\"辉\",\"位\",\"让\",\"尧\",\"依\",\"犹\",\"介\",\"承\",\"市\",\"所\",\"苑\",\"杞\",\"剧\",\"第\",\"零\",\"谌\",\"招\",\"续\",\"达\",\"忻\",\"六\",\"鄞\",\"战\",\"迟\",\"候\",\"宛\",\"励\",\"粘\",\"萨\",\"邝\",\"覃\",\"辜\",\"初\",\"楼\",\"城\",\"区\",\"局\",\"台\",\"原\",\"考\",\"妫\",\"纳\",\"泉\",\"老\",\"清\",\"德\",\"卑\",\"过\",\"麦\",\"曲\",\"竹\",\"百\",\"福\",\"言\",\"第五\",\"佟\",\"爱\",\"年\",\"笪\",\"谯\",\"哈\",\"墨\",\"南宫\",\"赏\",\"伯\",\"佴\",\"佘\",\"牟\",\"商\",\"西门\",\"东门\",\"左丘\",\"梁丘\",\"琴\",\"后\",\"况\",\"亢\",\"缑\",\"帅\",\"微生\",\"羊舌\",\"海\",\"归\",\"呼延\",\"南门\",\"东郭\",\"百里\",\"钦\",\"鄢\",\"汝\",\"法\",\"闫\",\"楚\",\"晋\",\"谷梁\",\"宰父\",\"夹谷\",\"拓跋\",\"壤驷\",\"乐正\",\"漆雕\",\"公西\",\"巫马\",\"端木\",\"颛孙\",\"子车\",\"督\",\"仉\",\"司寇\",\"亓官\",\"鲜于\",\"锺离\",\"盖\",\"逯\",\"库\",\"郏\",\"逢\",\"阴\",\"薄\",\"厉\",\"稽\",\"闾丘\",\"公良\",\"段干\",\"开\",\"光\",\"操\",\"瑞\",\"眭\",\"泥\",\"运\",\"摩\",\"伟\",\"铁\",\"迮\",\"荔菲\",\"辗迟\"]\n";
    private static Pattern firstNamePattern = Pattern.compile("[\\u4e00-\\u9fa5]+");
    @Test
    public void getName() throws Exception {
        List<String> firstNames = Lists.newArrayList();
        File file = new File("/Users/yangmeng/Downloads/name");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        for (int i = 0; i < 188; i++) {
            String s = reader.readLine();
            Matcher matcher = firstNamePattern.matcher(s);
            while (matcher.find()) {
                firstNames.add(matcher.group());
            }
        }
        System.out.println(JSON.toJSONString(firstNames));

    }

    @Test
    public void getName1() throws Exception {
        File file = new File("/Users/yangmeng/Downloads/GALAXY_53722088_20190428_173355.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String> sentScanList = Lists.newArrayList();
        outter:
        for (String deliveryActionDesc = reader.readLine(); StringUtils.isNotBlank(deliveryActionDesc); deliveryActionDesc = reader.readLine()) {
            List<DeliveryActionDesc> deliveryActionDescList = DeliveryActionDesc.parseList(deliveryActionDesc);
            for (DeliveryActionDesc desc : deliveryActionDescList) {
                // 配送行为
                if (org.apache.commons.lang3.StringUtils.equals("SENT_SCAN", desc.getAction())) {
                    String mobile = getMobile(desc);
                    String name = getName(desc);
                    if (StringUtils.isBlank(mobile)) {
                        continue;
                    }
                    if (StringUtils.isBlank(name)) {
                        name = "快递员";
                    }
                    sentScanList.add(String.format("%-15s%-10s", mobile, name));
                    continue outter;
                }
            }
            System.out.println(JSON.toJSONString(deliveryActionDescList));
        }
        System.out.println(sentScanList.size());

    }


    private static Pattern mobilePattern = Pattern.compile("[01]\\d{10}");
    private static Pattern namePattern = Pattern.compile("(?<=派件员[： ]?)[\\u4e00-\\u9fa5]+");

    public static void main(String[] args) throws IOException {

        List<String> firstNames = JSON.parseArray(firstNameJson, String.class);
        Set<String> firstNameSet = Sets.newHashSet(firstNames);


        File file = new File("/Users/yangmeng/Downloads/GALAXY_53722088_20190428_173355.txt");
        BufferedReader reader = new BufferedReader(new FileReader(file));
        List<String> sentScanList = Lists.newArrayList();
        outter:
        for (String deliveryActionDesc = reader.readLine(); StringUtils.isNotBlank(deliveryActionDesc); deliveryActionDesc = reader.readLine()) {
            List<DeliveryActionDesc> deliveryActionDescList = DeliveryActionDesc.parseList(deliveryActionDesc);
            for (DeliveryActionDesc desc : deliveryActionDescList) {
                // 配送行为
                if (org.apache.commons.lang3.StringUtils.equals("SENT_SCAN", desc.getAction())) {
                    String mobile = getMobile(desc);
                    String name = getName(desc);
                    if (StringUtils.isBlank(mobile)) {
                        continue;
                    }
                    if (StringUtils.isBlank(name)) {
                        name = "快递员";
                    }
                    sentScanList.add(String.format("%-15s%-10s", mobile, name));
                    continue outter;
                }
            }
        }
        System.out.println(sentScanList.size());
//        System.out.println(JSON.toJSONString(sentScanList));
        int i = 0;
        List<String> errorList = Lists.newArrayList();
        for (String str : sentScanList) {
            i++;
            String name = str.substring(15).trim();
            String mobile = str.substring(0, 11).trim();
            if (StringUtils.isBlank(name)) {
                errorList.add(str);
                continue;
            }
            if (StringUtils.isBlank(mobile)) {
                errorList.add(str);
                continue;
            }
            if (name.length() > 3) {
                errorList.add(str);
                continue;
            }
            if (name.length() >= 2) {
                String substring = name.substring(0, 2);
                if (firstNameSet.contains(substring)) {
                    continue;
                }
            }

            String substring = name.substring(0, 1);
            if (firstNameSet.contains(substring)) {
                continue;
            }
            errorList.add(str);
        }

        System.out.println(i);
        System.out.println(errorList.size());
        System.out.println(JSON.toJSONString(errorList));
    }

    private static String getMobile(DeliveryActionDesc desc) {
        Matcher mobileMatcher = mobilePattern.matcher(desc.getActionDesc());
        if (mobileMatcher.find()) {
            return mobileMatcher.group();
        }
        return "";
    }

    private static String getName(DeliveryActionDesc desc) {
        Matcher nameMatcher = namePattern.matcher(desc.getActionDesc());
        if (nameMatcher.find()) {
            return nameMatcher.group();
        }
        return "";
    }


    static class DeliveryActionDesc {
        private static final String STARTER = "$";
        private static final String SPLITTER = "|";

        private int timeStamp;
        private String address;
        private String actionDesc;
        private String action;

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder(STARTER);
            stringBuilder.append(timeStamp).append("|");
            stringBuilder.append(address).append("|");
            stringBuilder.append(actionDesc).append("|");
            stringBuilder.append(action);
            return stringBuilder.toString();
        }

        public static DeliveryActionDesc parse(String actionDesc){
            DeliveryActionDesc result = null;
            List<String> dataList = Splitter.on(SPLITTER).splitToList(actionDesc);
            if(dataList.size() == 4){
                result = new DeliveryActionDesc();
                result.setTimeStamp(NumberUtils.toInt(dataList.get(0)));
                result.setAddress(dataList.get(1));
                result.setActionDesc(dataList.get(2));
                result.setAction(dataList.get(3));
            }
            return result;
        }

        public static List<DeliveryActionDesc> parseList(String actionDescs){
            List<DeliveryActionDesc> resultList = Lists.newArrayList();
            if(StringUtils.isNotBlank(actionDescs)) {
                List<String> dataList = Splitter.on(STARTER).omitEmptyStrings().trimResults().splitToList(actionDescs);
                for (String data : dataList) {
                    DeliveryActionDesc deliveryActionDesc = parse(data);
                    if (deliveryActionDesc != null) {
                        resultList.add(deliveryActionDesc);
                    }
                }
            }
            return resultList;
        }

        public static String parseAction_desc(List<DeliveryActionDesc> actionDescList){
            StringBuilder action_desc = new StringBuilder();
            for (DeliveryActionDesc deliveryActionDesc : actionDescList) {
                action_desc.append(deliveryActionDesc.toString());
            }
            return action_desc.toString();
        }

        public static DeliveryActionDesc buildDeliveryActionDesc(int timeStamp, String address, String actionDesc, String action) {
            DeliveryActionDesc deliveryActionDesc = new DeliveryActionDesc();
            deliveryActionDesc.setTimeStamp(timeStamp);
            deliveryActionDesc.setAddress(address);
            deliveryActionDesc.setActionDesc(actionDesc);
            deliveryActionDesc.setAction(action);
            return deliveryActionDesc;
        }

        public int getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(int timeStamp) {
            this.timeStamp = timeStamp;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getActionDesc() {
            return actionDesc;
        }

        public void setActionDesc(String actionDesc) {
            this.actionDesc = actionDesc;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}