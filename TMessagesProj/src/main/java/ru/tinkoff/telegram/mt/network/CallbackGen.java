package ru.tinkoff.telegram.mt.network;

import java.util.ArrayList;
import java.util.Arrays;

import ru.tinkoff.telegram.mt.GenerationUtils;
import ru.tinkoff.telegram.mt.network.responses.ResultCode;

/**
 * @author a.shishkin1
 */


public class CallbackGen {



    public static void main(String[] args) {

        System.out.println("// <GENERATED CODE \n\n");
        ArrayList<String> all = new ArrayList<>();

        String[] actions = GenerationUtils.getPublicStaticFinalFieldsNames(Requests.class, int.class);

        all.addAll(Arrays.asList(actions));

        ResultCode[] codes = ResultCode.values();

        for(ResultCode rc : codes) {
            if(rc != ResultCode.OK) {
                all.add(rc.toString() + "_EXCEPTION");
            }
        }



        for(String str : all) {
            System.out.println("void handle" + GenerationUtils.toCamelCase(str, true) + "(BaseResult res);\n");
        }

        System.out.println("\n");
        System.out.println("class NOTIFY {");
        System.out.println("public static boolean notify(IApiCallback callback, BaseResult res) {\n");
        System.out.println("ResultCode rc = res.getResultCode();");
        System.out.println("if (rc == ResultCode.OK || res.isSpecificDispatch()) {");
        System.out.println("switch(res.getWhat()) {");
        for(String action : actions) {
            System.out.println("case Requests." + action + ":");
            System.out.println("callback.handle" + GenerationUtils.toCamelCase(action, true) + "(res);");
            System.out.println("return true;");
        }
        System.out.println("}");
        System.out.println("} else {");
        System.out.println("switch(rc) {");
        for(ResultCode rc : codes) {
            if(rc == ResultCode.OK)
                continue;
            System.out.println("case " + rc + ":");
            System.out.println("callback.handle" + GenerationUtils.toCamelCase(rc.toString() + "_EXCEPTION", true) + "(res);");
            System.out.println("return true;");
        }
        System.out.println("}");
        System.out.println("}");
        System.out.println("return false;");
        System.out.println("}");
        System.out.println("}");


        System.out.println("\n\n// GENERATED CODE/> \n\n");
    }



}
