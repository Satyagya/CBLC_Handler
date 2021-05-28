package com.example.LeadCrawl.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.LeadCrawl.constants.Constants.NEW_LINE;
import static com.example.LeadCrawl.constants.Constants.SIMPLE_DATE_PATTERN;


/**
 * Created by Anshuman on 24/09/19.
 */
@Slf4j
public class Utils {
    private Utils() {
    }

    public static final ObjectMapper objectMapper = new ObjectMapper();
    public static final ObjectReader JSON_NODE_READER = objectMapper.readerFor(JsonNode.class);
    public static final ObjectWriter JSON_NODE_WRITER = objectMapper.writerFor(JsonNode.class);

    private static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

    private static final String GLOBAL_LOCK = "GLOBAL_LOCK";
    private static final String DOMAIN_LOCK = "DOMAIN_LOCK_";


    /**
     * Returns the current timestamp in the following format:
     * 2018-04-16 20:12:09.808
     *
     * @return
     */
    public static Timestamp getCurrentTimestampInUTC() {
        return Timestamp.from(Instant.now());
    }

    /**
     * get JasonArray from text
     *
     * @param text
     *
     * @return
     */
    public static JSONArray convertTextToJsonArray(String text) {
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = null;
        try {
            jsonArray = (JSONArray) jsonParser.parse(text);
        } catch (org.json.simple.parser.ParseException e) {
            log.error("Error occurred while parsing text: {}", text, e);
        }
        return jsonArray;
    }


    /**
     * get string content and return class using objectMapper
     *
     * @param content
     * @param valueType
     * @param <T>
     *
     * @return
     */
    public static <T> T readValue(String content, Class<T> valueType) {
        T t = null;
        try {
            t = objectMapper.readValue(content, valueType);
        } catch (IOException e) {
            log.error("IOException while reading content from class", e);
        }
        return t;
    }

    /**
     * write object into string using objectMapper
     *
     * @param value
     *
     * @return
     */
    public static String writeValueAsString(Object value) {
        String content = null;
        try {
            content = objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException while writing class into string", e);
        }
        return content;
    }

    /**
     * generates hashcode of profileUrl
     *
     * @param profileUrl
     *
     * @return
     */
    public static String getHashCode(String profileUrl) {
        return String.valueOf(profileUrl.hashCode());
    }

    /**
     * convert string to list of string
     *
     * @param text
     *
     * @return
     */
    public static List<String> stringToList(String text) {
        return new ArrayList<>(Arrays.asList(text.split(NEW_LINE))).stream().map(String::trim)
                .filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    /**
     * sleeping for the politness
     *
     * @param politeness
     */
    public static void sleepInSecond(int politeness) {
        try {
            TimeUnit.SECONDS.sleep(politeness);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Exception while sleeping for politeness: {}", politeness, e);
        }
    }

    /**
     * list to string using delimeter
     *
     * @param list
     * @param delimeter
     *
     * @return
     */
    public static String listToString(List<String> list, String delimeter) {
        return list.stream().map(line -> String.format("%s%s", line, delimeter))
                .collect(Collectors.joining());
    }

    public static String getCurrentDate() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(SIMPLE_DATE_PATTERN);
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    /**
     * returns the ip address of machine
     *
     * @return
     */
    public static String getLocalIp() {
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("Unable to find ip address");
        }
        return ip;
    }

    public static String getCurrentDateOnly() {
        return formatter.format(new Date());
    }

    public static RLock getGlobalLock(RedissonClient redissonClient) {
        RLock lock = redissonClient.getLock(GLOBAL_LOCK);
        lock.lock();
        return lock;
    }

    public static RLock getDomainLock(String emailDomain, RedissonClient redissonClient) {
        RLock lock = redissonClient.getLock(DOMAIN_LOCK + emailDomain);
        lock.lock();
        return lock;
    }

    public static void unlock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}

