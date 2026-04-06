package com.example.English.teaching.center.utils;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public class HtmlSanitizerUtils {
// Filter content for CKEditor (posts, course descriptions)
    public static String sanitizeRichText(String unsafeHtml){
        if(unsafeHtml == null || unsafeHtml.trim().isEmpty())
            return unsafeHtml;

        return Jsoup.clean(unsafeHtml, Safelist.relaxed());
    }

// Strip all HTML(for names, plain text comments, short bios)
    public static String sanitizePlainText(String unsafeText){
        if(unsafeText == null || unsafeText.trim().isEmpty())
            return unsafeText;
        
        return Jsoup.clean(unsafeText, Safelist.none());
    }
}
