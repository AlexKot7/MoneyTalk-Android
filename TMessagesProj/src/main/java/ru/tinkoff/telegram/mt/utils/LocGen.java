package ru.tinkoff.telegram.mt.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author a.shishkin1
 */


public class LocGen {


    public static void main(String[] args) throws IOException {

        File projectDir = new File(System.getProperty("user.dir"));
        File genDir = new File(projectDir, "gen");
        if(!genDir.exists() || !genDir.isDirectory()) {
            throw new FileNotFoundException("put gen directory in project root folder");
        }
        try(BufferedReader br = new BufferedReader(new FileReader(new File(genDir, "strs_ru")))) {

            List<Resource> res = new ArrayList<>();
            for(String line; (line = br.readLine()) != null; ) {
                if (!line.startsWith("\"")) {
                    continue;
                }

                try {
                    String[] baseParts = line.split("=");
                    String name = baseParts[0].trim().toLowerCase().replace("\"", "").replace(".", "_");

                    String valueCandidate = baseParts[1].trim().replace("\"", "").replace(";", "").replace("&", "&amp;");
                    String value = new String(valueCandidate);
                    int i = 1;
                    while(!value.equals(valueCandidate = valueCandidate.replaceFirst("%@|…","%" + String.valueOf(i) + "\\$" + "s"))) {
                        i++;
                        value = new String(valueCandidate);
                    }

                    res.add(new Resource(name, value, "mt_gen"));
                } catch (Exception e) {
                    System.out.println(line);
                    System.out.println(e.toString());
                }


            }
            System.out.println("\n <!-- START GENERATED CODE --> \n\n");

            for(Resource r : res) {
                System.out.println(r.toString());
            }


            System.out.println("\n\n <!-- END GENERATED CODE --> \n\n");
        }

    }


    private static class Resource {

        private String name;
        private String value;
        private String prefix;

        public Resource(String name, String value, String prefix) {
            this.name = name;
            this.value = value;

            this.prefix = prefix;
        }


        public String toString() {
            return String.format("<string name=\"%s_%s\">%s</string>\n", prefix, name, value );
        }

    }


}
