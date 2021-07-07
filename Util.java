package io.finserveafrica.jenga.authenticationservice.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;
import io.finserveafrica.jenga.authenticationservice.constants.AppConstants;
import io.finserveafrica.jenga.authenticationservice.constants.ResponseMessage;
import io.finserveafrica.jenga.authenticationservice.exceptions.BaseException;
import io.finserveafrica.jenga.authenticationservice.models.*;
import io.finserveafrica.jenga.authenticationservice.security.ApiPrincipal;
import io.finserveafrica.jenga.authenticationservice.security.CredentialModel;
import io.finserveafrica.jenga.authenticationservice.security.JwtHelper;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.persistence.Table;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.by;


public class Util<T> {

    static Logger logger = LoggerFactory.getLogger(Util.class.getName());

    public static ResponseEntity<?> getResponse(CustomResponse response) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.resolve(response.getCode()) != null ? HttpStatus.valueOf(response.getCode()) : HttpStatus.BAD_REQUEST);
        return builder.body(response.getData() != null ? response :  CustomStatus.map(response.isStatus(),response.getCode(),response.getMessage()));
    }
    public static ResponseEntity<?> getResponse(CustomResponse response, Object entity) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(HttpStatus.resolve(response.getCode()) != null ? HttpStatus.valueOf(response.getCode()) : HttpStatus.BAD_REQUEST);
        return builder.body(entity != null ? entity : CustomResponse.map(response.isStatus(),response.getCode(), response.getMessage()));
    }

    public static Pageable getPageable(int page, int size, String direction, String orderBy) {
        Sort sort;
        if (direction.equalsIgnoreCase("ASC")) {
            sort = by(Sort.Direction.ASC, orderBy);
        } else {
            sort = by(Sort.Direction.DESC, orderBy);
        }
        return PageRequest.of(page, size, sort);
    }

    public CustomResponse<PagedData<T>> getPagedResponse(Page page, List content) {
        PagedData<T> pagedData;
        CustomStatus customStatus;
        if (page.getNumberOfElements() == 0) {
            customStatus = CustomStatus.strip(Translator.toLocale(ResponseMessage.NO_RECORDS_FOUND));
            customStatus.setCode(AppConstants.HttpStatusCode.OK);
            customStatus.setStatus(false);
            pagedData = new PagedData<T>( Collections.emptyList(), page.getNumber(),
                    page.getSize(), page.getTotalElements(), page.getTotalPages(),page.isFirst(), page.isLast());
        } else {
            customStatus = CustomStatus.strip(Translator.toLocale(ResponseMessage.SUCCESS));
            customStatus.setStatus(true);
            pagedData = new PagedData<T>(content, page.getNumber(),
                    page.getSize(), page.getTotalElements(), page.getTotalPages(), page.isFirst(),page.isLast());
        }
        return new CustomResponse<>(customStatus, pagedData);
    }

    public static String decodeString(String str) {
        String encodedContent = "";
        try {
            encodedContent = URLDecoder.decode(str, "UTF-8");
        } catch (Exception exception) {
            exception.getMessage();
        }
        return encodedContent;
    }

    public static String encodeString(String str) {
        String encodedContent = "";
        try {
            encodedContent = URLEncoder.encode(str, "UTF-8");
        } catch (Exception exception) {
            exception.getMessage();
        }
        return encodedContent;
    }

//    public static String toXML(Object entity) {
//        String xml = "";
//        XmlMapper xmlMapper = new XmlMapper();
//        try {
//            xml = xmlMapper.writeValueAsString(entity);
//        } catch (JsonProcessingException e) {
//            logger.info(e.getMessage());
//        }
//        return xml;
//    }

    public static String toJson(Object entity) {
        String json = "";
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            logger.info(e.getMessage());
        }
        return json;
    }

    public static  String getLastWordsOfString (int charCount, String myString) {
        if(myString != null && myString.length() > charCount)
            return myString.substring(myString.length()- charCount);
        else
            return myString;
    }

    public static LogLocation getLogLocation() {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        String methodName = null;
        Integer lineNumber = null;
        String className = null;
        String fileName = null;
        for (int i = 0; i < stacktrace.length; i++) {
            if (stacktrace[i].getMethodName().equals("getLogLocation")) {
                methodName = stacktrace[i + 1].getMethodName();
                lineNumber = stacktrace[i + 1].getLineNumber();
                className = stacktrace[i + 1].getClassName();
                fileName = stacktrace[i + 1].getFileName();
                break;
            }

        }
        return new LogLocation(className, methodName, lineNumber, fileName);

    }

    public static LogLocation getLogLocation(StackTraceElement[] stacktrace) {
        String methodName = null;
        Integer lineNumber = null;
        String className = null;
        String fileName = null;
        for (int i = 0; i < stacktrace.length; i++) {
            if (stacktrace[i].getClassName().startsWith(AppConstants.PACKAGE_NAME)) {
                methodName = stacktrace[i + 1].getMethodName();
                lineNumber = stacktrace[i + 1].getLineNumber();
                className = stacktrace[i + 1].getClassName();
                fileName = stacktrace[i + 1].getFileName();
                break;
            }

        }
        return new LogLocation(className, methodName, lineNumber, fileName);

    }

    public static String appendTimestampToFileName(String name) {
        return String.format("%s_%s", name.replaceAll(" ", "_"), getTimestamp());
    }


    public static String getTimestamp() {
        final String TIMESTAMP_PATTERN = "yyyyMMddHHmmss";
        return formatDate(new Date(), TIMESTAMP_PATTERN);
    }

    public static Long convertStringToLong(String str) {
        Long val = null;
        try {
            val = Long.parseLong(str);
        } catch (Exception e) {

            BaseException.handleException(e, AppConstants.LogCategory.UNIVERSAL_LOG,null, str);
        }

        return val;
    }

    public static CustomStatus stripMessage(String message) {
        String[] codeMessage = message.split(":", 2);
        String code = codeMessage[0];
        String msg = codeMessage[1];
        return new CustomStatus(Integer.parseInt(code), msg);
    }

    public static String formatDate(Date date, String formatPattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(formatPattern);
        return formatter.format(date);
    }

    public static Date parseDate(String strDate, String formatPattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatPattern);
        Date date = null;
        try {
            date = dateFormat.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            logger.info("Parse Date: " + e.getMessage());
        }
        return date;
    }


    public static String getDataString(HashMap<String, Object> params) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("\n");
            }
            result.append(entry.getKey());
            result.append(":");
            result.append(entry.getValue());
        }

        logger.info("Data String :" + result);
        return result.toString();

    }

    public static String toJsonTimeZone(Object entity) {
        String json = "";
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // StdDateFormat is ISO8601 since jackson 2.9
        mapper.setDateFormat(new StdDateFormat().withTimeZone(TimeZone.getTimeZone("EAT")));
        try {
            json = mapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }

//    public T fromXML(String readContent, Class<T> type) {
//        JacksonXmlModule xmlModule = new JacksonXmlModule();
//        xmlModule.setDefaultUseWrapper(false);
//        XmlMapper mapper = new XmlMapper(xmlModule);
//        try {
//            return mapper.readValue(readContent, type);
//        } catch (IOException e) {
//            logger.info(e.getMessage());
//        }
//        return null;
//    }



    public static String getIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    public T fromJson(String json, Class<T> type) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            logger.info(e.getMessage());
        }
        return null;
    }

    public T fromObject(Object object, Class<T> type) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.convertValue(object, type);
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        return null;
    }

    public String getDisplayDateTime(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh.mm.ss aa");
        dateFormat.setTimeZone(TimeZone.getTimeZone("EAT"));
        String strDate = dateFormat.format(date);
        return strDate;
    }

    public  String getTableName(Class<T>clazz){
        if(clazz != null){
            Class<?> c = clazz;
            Table table = c.getAnnotation(Table.class);
            return table.name();
        }else{
            return null;
        }

    }

    public List<DataChangeModel>  getObjectDifference(Object item1, Object item2){
        DiffNode diff = ObjectDifferBuilder.buildDefault().compare(item1, item2);
        List<DataChangeModel>dataChangeModelList =  new ArrayList<>();
        if (diff.hasChanges()) {
            diff.visit(new DiffNode.Visitor() {
                @Override
                public void node(DiffNode diffNode, Visit visit) {
                    if (!diffNode.hasChildren()) { // Only print if the property has no child
                        final Object oldValue = diffNode.canonicalGet(item1);
                        final Object newValue = diffNode.canonicalGet(item2);

                        dataChangeModelList.add(new DataChangeModel(diffNode.getPropertyName(), oldValue, newValue));

                    }
                }
            });
        } else {
            System.out.println("No differences");
        }
        return dataChangeModelList;
    }


    public static Long getMerchantIdFromPrincipal(Long merchantId, ApiPrincipal apiPrincipal){
        if(apiPrincipal.getAccount().getMerchant() != null){
            merchantId = apiPrincipal.getAccount().getMerchant().getId();
        }
        return merchantId;
    }

    public static String getMerchantCodeFromPrincipal(String merchantCode, ApiPrincipal apiPrincipal){

        if(apiPrincipal.getAccount().getMerchant() != null){
            merchantCode = apiPrincipal.getAccount().getMerchant().getCode();
        }
        return merchantCode;
    }

    public static String getMerchantNameFromPrincipal(String merchantName, ApiPrincipal apiPrincipal){

        if(apiPrincipal.getAccount().getMerchant() != null){
            merchantName = apiPrincipal.getAccount().getMerchant().getName();
        }
        return merchantName;
    }

    public static ApiPrincipal getUserDetails(HttpServletRequest httpServletRequest) throws IOException {
        String AUTH_HEADER_STRING = "Authorization";
        String token = httpServletRequest.getHeader(AUTH_HEADER_STRING);
        String requestBody = httpServletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        String urlURI = httpServletRequest.getRequestURI();
        String queryString = httpServletRequest.getQueryString();
        String httpMethod = httpServletRequest.getMethod();
        String ipAddress = Util.getIP(httpServletRequest);

        String authType = null;
        CredentialModel credentialModel = null;
        if (token != null) {
            Claims claims = new JwtHelper().extractAllClaims(token);


            authType = (String) claims.get("tokenType");
            credentialModel = new ObjectMapper().convertValue(claims.get("account"), CredentialModel.class);

        }

        return new ApiPrincipal(credentialModel, token, authType, null, urlURI, httpMethod, requestBody, queryString, ipAddress);
    }

    public  static  String decryptData(String encryptedData){
        AesUtil aesUtil =  new AesUtil();
        return aesUtil.decrypt(JwtHelper.SECRET_KEY,encryptedData);
    }



}
