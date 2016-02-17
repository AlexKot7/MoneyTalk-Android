package ru.tinkoff.telegram.mt.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.tinkoff.telegram.mt.GenerationUtils;

/**
 * @author a.shishkin1
 */


public class RequestsGen {

    private static int REQUEST_ACTION_OFFSET = 900;

    public static void main(String[] args) throws IOException {

        System.out.println("\n// <GENERATED CODE \n\n");
        File projectDir = new File(System.getProperty("user.dir"));
        File genDir = new File(projectDir, "gen");
        if(!genDir.exists() || !genDir.isDirectory()) {
            throw new FileNotFoundException("put gen directory in project root folder");
        }
        try(BufferedReader br = new BufferedReader(new FileReader(new File(genDir, "requests")))) {
            List<String> endpoints = new ArrayList<>();
            List<ClassData> cds  = new ArrayList<>();
            for(String line; (line = br.readLine()) != null; ) {
                int parametersStart = line.indexOf("(");
                String endPointCandidate = line.substring(0, parametersStart);
                String endPoint = endPointCandidate.trim();
                endpoints.add(endPoint);
                String paramsString = line.substring(parametersStart + 1, line.indexOf(")"));
                String[] paramsCandidates = paramsString.length() == 0 ? new String[0] : paramsString.split(",");
                String[] params = new String[paramsCandidates.length];
                for(int i = 0; i < paramsCandidates.length; i++) {
                    params[i] = paramsCandidates[i].trim();
                }
                cds.add(new ClassData(endPoint, params));
            }


            for(int i = 0; i < endpoints.size(); i++) {
                System.out.println("public static final int " + GenerationUtils.toUpperCase(endpoints.get(i)) + " = " + (i + REQUEST_ACTION_OFFSET) + ";");
            }
            System.out.print("\n");
            for(ClassData cd : cds) {
                System.out.println(cd);

            }

            System.out.println("\n\n// GENERATED CODE/> \n\n");
        }

    }

    private static class ClassData {
        private final String endPoint;
        private final String[] parameters;

        public ClassData(String endPoint, String[] parameters) {
            this.endPoint = endPoint;
            this.parameters = parameters;
        }

        @Override
        public String toString() {
            String endPointToUpperCase = GenerationUtils.toUpperCase(endPoint);
            StringBuilder sb = new StringBuilder();
            sb.append("public static class REQUEST_");
            sb.append(endPointToUpperCase).append(" {\n\n");
            sb.append("public static Network.RequestBuilder prepare(");
            for(int i = 0; i < parameters.length; i++) {
                sb.append("String ").append(parameters[i]);
                sb.append(i == parameters.length - 1 ? "" : ", ");
            }
            sb.append(") {\n");

            sb.append("Network.RequestBuilder result = new Network.RequestBuilder(getAction(), getEndPoint());\n");

            if(parameters.length > 0) {
//                sb.append("Map<String, String> result = new HashMap();\n");
                for (int i = 0; i < parameters.length; i++) {
                    sb.append("result.addParameter(\"").append(parameters[i]).
                    append("\", ").append(parameters[i]).append(");\n");
                }
            }
            sb.append("return result;\n}\n\n");
            sb.append("private static String getEndPoint() {\n").append("return \"").append(endPoint).append("\";")
                    .append("\n}\n\n");

            sb.append("private static int getAction() {\n").append("return ").append(endPointToUpperCase).append(";")
                    .append("\n}\n\n");



            sb.append("}\n");
            return sb.toString();
        }
    }













}
