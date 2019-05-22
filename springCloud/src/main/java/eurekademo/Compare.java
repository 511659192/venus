// Copyright (C) 2019 Meituan
// All rights reserved
package eurekademo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

/**
 * @author yangmeng
 * @version 1.0
 * @created 2019-01-17 17:35
 **/
public class Compare {

    public static void main(String[] args) throws Exception {

        String json = "[\n" + "\t{\n" + "\t\t\"Tables_in_banmamall\" : \"bm_bill_check_result\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_activity\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_activity_rule\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_admin_address_city\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_admin_address_district\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_admin_address_prov\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_coupon_possess\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_goods\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_goods_category\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_goods_category_relation\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_goods_supplier\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_inout_contrast_record\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_inout_settlement_mt\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_inout_settlement_yd\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_order\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_order_address\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_order_express\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_order_inout_record\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_order_payment\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_order_refund\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_order_track\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_partner_address_info\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_partner_inventory\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_partner_invoice_info\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_partner_order\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_partner_order_express\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_partner_order_payment\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_partner_order_track\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_partner_spec_order\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_pickup_address\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_pickup_province\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_procurement_instorage_record\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_procurement_order\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_procurement_spec\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_procurement_track\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_purchase_record\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_recipient_info\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_return_order\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_sell_rule\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_spec\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_spec_order\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_spec_return\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_stock\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_stock_record\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_equipment_supplier\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_mall_receive_bill_record\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_mall_refund_bill_record\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"Tables_in_banmamall\" : \"bm_refund_bill_check_result_exception\"\n" + "\t}\n" + "]";

        JSONArray array = JSON.parseArray(json);
        Set<String> names1 = Sets.newHashSet();
        for (Object obj : array) {
            JSONObject jsonObject = ((JSONObject) obj);
            Object name = jsonObject.get("Tables_in_banmamall");
            names1.add(name.toString());
        }


        InputStream is = EurekaApplication.class.getClassLoader().getResourceAsStream("remainTables");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String s;
        Set<String> names2 = Sets.newHashSet();
        while (StringUtils.isNotBlank(s = reader.readLine())) {
            names2.add(s);
        }

        Set<String> names3 = Sets.newHashSet(names1);
        names1.removeAll(names2);
        System.out.println(JSON.toJSONString(names1));

        names2.removeAll(names3);
        for (String s1 : names2) {
            System.out.println(s1);
        }

//        InputStream is = EurekaApplication.class.getClassLoader().getResourceAsStream("aa");
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        String s;
//        Set<String> set1 = Sets.newHashSet();
//        while (StringUtils.isNotBlank(s = reader.readLine())) {
//            set1.add(s);
//        }
//        System.out.println(set1.size());
//
//        InputStream is2 = EurekaApplication.class.getClassLoader().getResourceAsStream("ab");
//        BufferedReader reader2 = new BufferedReader(new InputStreamReader(is2));
//        String s2;
//        Set<String> set2 = Sets.newHashSet();
//        while (StringUtils.isNotBlank(s2 = reader2.readLine())) {
//            set2.add(s2);
//        }
//        System.out.println(set2.size());
//
//        Set<String> set3 = Sets.newHashSet(set1);
//        set1.removeAll(set2);
//        System.out.println(JSON.toJSONString(set1));
//
//        set2.removeAll(set3);
//        System.out.println(set2);
    }
}