package net.kraschitzer.intellij.plugin.time_tracker.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class DateDeserializer extends JsonDeserializer<LocalDateTime> {

    private static Map<Pattern, String> dateFormats;
    private static Map<Pattern, String> timeFormats;

    static {
        dateFormats = new LinkedHashMap<>();
        dateFormats.put(Pattern.compile("^\\d{4}-\\d{2}-\\d{2}.*"), "yyyy-MM-dd");//2020-02-02
        dateFormats.put(Pattern.compile("^\\w*, \\d \\w* \\d{4}.*"), "E, d MMM yyyy");//Monday 2 March 2020
        dateFormats.put(Pattern.compile("^\\w*, \\d{2} \\w* \\d{4}.*"), "E, dd MMM yyyy");//Monday 2 March 2020
        dateFormats.put(Pattern.compile("^\\d{4}-\\d{2}-\\d{2}.*"), "yyyy-MM-dd"); //2020-03-08
        dateFormats.put(Pattern.compile("^\\d{4}-\\d-\\d.*"), "yyyy-M-d"); //2020-3-8
        dateFormats.put(Pattern.compile("^\\d{4}/\\d{2}/\\d{2}.*"), "yyyy/MM/dd"); //2020/03/08
        dateFormats.put(Pattern.compile("^\\d{4}/\\d/\\d.*"), "yyyy/M/d"); //2020/3/8
        dateFormats.put(Pattern.compile("^\\d{4}\\.\\d{2}\\.\\d{2}.*"), "yyyy.MM.dd"); //2020.03.08
        dateFormats.put(Pattern.compile("^\\d{4}\\.\\d\\.\\d.*"), "yyyy.M.d"); //2020.3.8
        dateFormats.put(Pattern.compile("^\\d{2}/\\d{2}/\\d{4}.*"), "dd/MM/yyyy"); //08/03/2020
        dateFormats.put(Pattern.compile("^\\d{2}-\\d{2}-\\d{2}.*"), "dd-MM-yy"); //08-03-20
        dateFormats.put(Pattern.compile("^\\d{2}/\\d{2}/\\d{2}.*"), "dd/MM/yy"); //08/03/20
        dateFormats.put(Pattern.compile("^\\d{2}-\\d-\\d.*"), "yy-M-d"); //20-3-8
        dateFormats.put(Pattern.compile("^\\d{2}/\\d/\\d.*"), "yy/M/d"); //20/3/8
        dateFormats.put(Pattern.compile("^\\d{2}\\.\\d\\.\\d.*"), "yy.M.d"); //20.3.8
        dateFormats.put(Pattern.compile("^\\d/\\d/\\d{4}.*"), "M/d/yyyy"); //3/8/2020
        dateFormats.put(Pattern.compile("^\\d/\\d/\\d{2}.*"), "M/d/yy"); //3/8/20
        dateFormats.put(Pattern.compile("^\\d-\\d-\\d{4}.*"), "d-M-yyyy"); //8-3-2020
        dateFormats.put(Pattern.compile("^\\d-\\d-\\d{2}.*"), "d-M-yy"); //8-3-20
        dateFormats.put(Pattern.compile("^\\d{2}-\\d{2}-\\d{4}.*"), "dd-MM-yyyy"); //08-03-2020
        dateFormats.put(Pattern.compile("^\\d{2}-\\w*-\\d{2}.*"), "dd-MMM-yy"); //08-Mar-20
        dateFormats.put(Pattern.compile("^\\d{2}/\\w*/\\d{4}.*"), "dd/MMM/yyyy"); //08/Mar/2020
        dateFormats.put(Pattern.compile("^\\d{2}\\.\\w*\\.\\d{4}.*"), "dd.MMM.yyyy"); //08.Mar.2020
        dateFormats.put(Pattern.compile("^\\d{2}\\.\\d{2}\\.\\d{4}.*"), "dd.MM.yyyy"); //08.03.2020
        dateFormats.put(Pattern.compile("^\\d{2}\\.\\d{2}\\.\\d{2}.*"), "dd.MM.yy"); //08.03.20
        dateFormats.put(Pattern.compile("^\\d{2}\\.\\d\\.\\d{2}.*"), "dd.M.yy"); //08.3.20
        dateFormats.put(Pattern.compile("^\\d{2}\\. \\w*\\. \\d{4}.*"), "dd. MMM. yyyy"); //08. Mar. 2020
        dateFormats.put(Pattern.compile("^\\d/\\d{2}/\\d{2}.*"), "d/MM/yy"); //8/03/20
        dateFormats.put(Pattern.compile("^\\d/\\d/\\d{4}.*"), "d/M/yyyy"); //8/3/2020
        dateFormats.put(Pattern.compile("^\\d/\\d/\\d{2}.*"), "d/M/yy"); //8/3/20
        dateFormats.put(Pattern.compile("^\\d\\.\\d{2}\\.\\d{4}.*"), "d.MM.yyyy"); //8.03.2020
        dateFormats.put(Pattern.compile("^\\d\\.\\d\\.\\d{4}.*"), "d.M.yyyy"); //8.3.2020
        dateFormats.put(Pattern.compile("^\\d\\.\\d\\.\\d{2}.*"), "d.M.yy"); //8.3.20
        dateFormats.put(Pattern.compile("^\\d\\. \\d\\. \\d{4}.*"), "d. M. yyyy"); //8. 3. 2020
        dateFormats.put(Pattern.compile("^\\d \\w* \\d{4}.*"), "d MMM yyyy"); //8 Mar 2020


        timeFormats = new LinkedHashMap<>();
        timeFormats.put(Pattern.compile(".*T\\d{2}:\\d{2}:\\d{2}\\.\\d{7}$"), "'T'HH:mm:ss.SSSSSSS"); //T20:35:50.4812347
        timeFormats.put(Pattern.compile(".*T\\d{2}:\\d{2}:\\d{2}\\.\\d{6}$"), "'T'HH:mm:ss.SSSSSS"); //T20:35:50.4812347
        timeFormats.put(Pattern.compile(".*T\\d{2}:\\d{2}:\\d{2}\\.\\d{5}$"), "'T'HH:mm:ss.SSSSS"); //T20:35:50.4812347
        timeFormats.put(Pattern.compile(".*T\\d{2}:\\d{2}:\\d{2}\\.\\d{4}$"), "'T'HH:mm:ss.SSSS"); //T20:35:50.4812347
        timeFormats.put(Pattern.compile(".*T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}$"), "'T'HH:mm:ss.SSS"); //T20:35:50.487
        timeFormats.put(Pattern.compile(".*T\\d{2}:\\d{2}:\\d{2}\\.\\d{2}$"), "'T'HH:mm:ss.SS"); //T20:35:50.48
        timeFormats.put(Pattern.compile(".*T\\d{2}:\\d{2}:\\d{2}\\.\\d{1}$"), "'T'HH:mm:ss.S"); //T20:35:50.4
        timeFormats.put(Pattern.compile(".*T\\d{2}:\\d{2}:\\d{2}\\.$"), "'T'HH:mm:ss."); //T20:35:50.
        timeFormats.put(Pattern.compile(".* \\d{2}:\\d{2}:\\d{2} \\w{3}$"), " HH:mm:ss zzz");// 20:32:20 UTC
        timeFormats.put(Pattern.compile(".* \\d{2}:\\d{2}:\\d{2}$"), " HH:mm:ss"); //18:08:23
        timeFormats.put(Pattern.compile(".* \\d:\\d{2} (AM|PM)$"), " h:mm aa"); //6:08 PM
        timeFormats.put(Pattern.compile(".* \\d{2}:\\d{2} (AM|PM)$"), " hh:mm aa"); //06:08 PM
        timeFormats.put(Pattern.compile(".* (AM|PM) \\d:\\d{2}$"), " aa h:mm"); //PM 6:08
        timeFormats.put(Pattern.compile(".* (AM|PM) \\d{2}:\\d{2}$"), " aa hh:mm"); //PM 06:08
        timeFormats.put(Pattern.compile(".* \\d{2}:\\d{2}$"), " HH:mm"); //18:08
        timeFormats.put(Pattern.compile(".* \\d{2}:\\d{2} Uhr$"), " HH:mm' Uhr'"); //18:08 Uhr
        timeFormats.put(Pattern.compile(".* \\d{2}\\.\\d{2}$"), " HH.mm"); //18.08
        timeFormats.put(Pattern.compile(".* \\d{2}H\\d{2}$"), " HH'H'mm"); //18H08
        timeFormats.put(Pattern.compile(".* \\d{2} h \\d{2}$"), " HH' h 'mm"); //18 h 08
        timeFormats.put(Pattern.compile(".* \\d{2}h\\d{2}$"), " HH'h'mm"); //18h08
        timeFormats.put(Pattern.compile(".* \\d:\\d{2}$"), " H:mm"); //18:08
        timeFormats.put(Pattern.compile(".* \\d\\.\\d{2}$"), " H.mm"); //18.08

    }

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String dateTimeText = p.getValueAsString();
        try {
            for (Pattern dateRegex : dateFormats.keySet()) {
                if (dateRegex.matcher(dateTimeText).matches()) {
                    for (Pattern timeRegex : timeFormats.keySet()) {
                        if (timeRegex.matcher(dateTimeText).matches()) {
                            return LocalDateTime.parse(dateTimeText, DateTimeFormatter.ofPattern(dateFormats.get(dateRegex) + timeFormats.get(timeRegex)));
                        }
                    }
                }
            }
        } catch (DateTimeParseException ex) {
            throw new JsonParseException(p, "failed to parse date string", ex);
        }
        throw new JsonParseException(p, "Unknown date string pattern '" + dateTimeText + "'");
    }
}
