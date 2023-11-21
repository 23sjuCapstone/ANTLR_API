package com.example.antlrapi.controller;


import com.example.antlrapi.dto.SqlComponent;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

import static com.example.antlr.ParseProcessor.*;

@RestController
@RequestMapping("/antlr")
public class AntlrController {

    @GetMapping("/test")
    public String test() {
        return "success";
    }

    @PostMapping("/run")
    public ArrayList<SqlComponent> runSQL(@RequestParam String sql) {

        // 쿼리 개수 파악 1개면 키워드랑 전체 쿼리 보내주기
        // 결국 나는 components 배열을 보내주지만, 백엔드단에서 해당 크기가 1인지 아닌지로 구별
//        ArrayList<SqlComponent> components = new ArrayList<>();

        int[] queryCnt = step1(sql);
        System.out.println("Union querycnt :" + queryCnt);
//
        if (queryCnt[0] == -1) {
            ArrayList<SqlComponent> components = new ArrayList<>();
            ArrayList<String> subqueryfound = findSubquery(sql);
            System.out.println("subqueryFound : " + subqueryfound.size());

            int subquerySize = subqueryfound.size();
            for (int i = 0; i < subquerySize; i++) {
                SqlComponent sqlcmpt = step2(subqueryfound.get(i));

                sqlcmpt.setStep(i + 1);
                sqlcmpt.setSql(subqueryfound.get(i));
                components.add(i, sqlcmpt);
            }
            SqlComponent originalQuery = new SqlComponent(subquerySize + 1, "UNION", sql);
            components.add(subquerySize, originalQuery);

            for (int i = 0; i < subqueryfound.size(); i++) {
                System.out.println("subqueryFound : " + subqueryfound.get(i));
            }
            return components;
        }

        if (queryCnt[0] == 0) {
            if (queryCnt[1] == 1) {
                ArrayList<SqlComponent> components = new ArrayList<>();
                String keyword = getCommand(sql);
                components.add(0, new SqlComponent(1, keyword, sql));
                return components;
            } else {
                // 1. select where절에 서브쿼리 1개(총 쿼리가 2개인 경우)
                ArrayList<SqlComponent> components = new ArrayList<>();
                ArrayList<String> subqueryfound = findSubquery(sql);

                int subquerySize = subqueryfound.size();
                for (int i = 0; i < subquerySize; i++) {
                    SqlComponent sqlcmpt = step2(subqueryfound.get(i));

                    sqlcmpt.setStep(i + 1);
                    sqlcmpt.setSql(subqueryfound.get(i));
                    components.add(i, sqlcmpt);
                }
                // 전체 쿼리 넣어주기
                SqlComponent originalQuery = step2(sql);
                originalQuery.setStep(subquerySize + 1);
                originalQuery.setSql(sql);
                originalQuery.getCondition().setObject(subqueryfound.get(0));
                components.add(subquerySize, originalQuery);

                return components;
            }
        }
//        else if (queryCnt[0] != 0 && queryCnt != 1) {  // 복잡한 쿼리문 (queryCnt != 0 : insert update delete create .. 가 아니거나 /  queryCnt != 1 : 단일 select 가 아닐 때)
//            // 1. select where절에 서브쿼리 1개(총 쿼리가 2개인 경우)
//            ArrayList<SqlComponent> components = new ArrayList<>();
//            ArrayList<String> subqueryfound = findSubquery(sql);
//
//            int subquerySize = subqueryfound.size();
//            for(int i = 0; i < subquerySize; i++){
//                SqlComponent sqlcmpt = step2(subqueryfound.get(i));
//
//                sqlcmpt.setStep(i+1);
//                sqlcmpt.setSql(subqueryfound.get(i));
//                components.add(i, sqlcmpt);
//            }
//            // 전체 쿼리 넣어주기
//            SqlComponent originalQuery = step2(sql);
//            originalQuery.setStep(subquerySize+1);
//            originalQuery.setSql(sql);
//            originalQuery.getCondition().setObject(subqueryfound.get(0));
//            components.add(subquerySize, originalQuery);
//
//            return components;
//        }
//        else {   // 단순한 쿼리문 일 경우
//            ArrayList<SqlComponent> components = new ArrayList<>();
//            String keyword = getCommand(sql);
//            components.add(0, new SqlComponent(1, keyword, sql));
//            return components;
//        }

        return null;
    }
}
