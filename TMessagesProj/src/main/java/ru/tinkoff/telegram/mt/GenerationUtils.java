package ru.tinkoff.telegram.mt;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * @author a.shishkin1
 */


public class GenerationUtils {


    public static String toUpperCase(String string) {
        StringBuilder sb = new StringBuilder();
        char[] chars = string.toCharArray();
        boolean needInsert = false;
        for(int i = 0; i < chars.length; i++) {
            char c = chars[i];
            needInsert = Character.isUpperCase(c);
            if(needInsert) {
                sb.append("_");
            }
            sb.append(Character.toUpperCase(c));
        }

        return sb.toString();
    }

    public static String toCamelCase(String str, boolean firstUpper) {
        char[] chars = new char[str.length()];
        boolean needUpper = firstUpper;
        int k = 0;
        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if(c == '_') {
                needUpper = true;
                continue;
            }
            if(needUpper) {
                chars[k] = Character.toUpperCase(c);
            } else {
                chars[k] = Character.toLowerCase(c);
            }
            k++;
            needUpper = false;
        }
        String candidate = new String(chars);
        return candidate.trim();
    }

    public static String[] getPublicStaticFinalFieldsNames(Class from, Class what) {
        Field[] declaredFields = from.getDeclaredFields();
        List<Field> staticFields = new ArrayList<Field>();
        for (Field field : declaredFields) {
            int mod = field.getModifiers();
            if (Modifier.isStatic(mod) && Modifier.isFinal(mod) && Modifier.isPublic(mod) && field.getType() == what) {
                staticFields.add(field);
            }
        }
        String[] all = new String[staticFields.size()];
        for (int i = 0; i < staticFields.size(); i++) {
            Field field = staticFields.get(i);
            all[i] = field.getName();
        }
        return all;
    }
}
