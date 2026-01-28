package com.example.English.teaching.center.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class SlugUtils {
    private SlugUtils(){
        throw new UnsupportedOperationException("Đây là lớp tiện ích, không được khởi tạo!");
    }

    public static String makeSlug(String input){
        if(input == null || input.isEmpty()) return "";
        
        String slug = input.toLowerCase();
        
        String temp = Normalizer.normalize(slug, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        slug = pattern.matcher(temp).replaceAll("");

        slug = slug.replaceAll("đ", "d");
        slug = slug.replaceAll("[^a-z0-9\\s]", "");
        slug = slug.replaceAll("\\s+", "-");
        slug = slug.replaceAll("^-+|-+$", "");

        return slug;
    }
}
