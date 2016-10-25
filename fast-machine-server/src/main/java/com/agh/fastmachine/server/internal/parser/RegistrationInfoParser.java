package com.agh.fastmachine.server.internal.parser;

import com.agh.fastmachine.server.internal.service.registrationinfo.BindingMode;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationInfo;
import com.agh.fastmachine.server.internal.service.registrationinfo.RegistrationObjectInfo;
import org.eclipse.californium.core.coap.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO refactor this
public class RegistrationInfoParser {
    private static final Pattern PATTERN = Pattern.compile("<(?<url>.*?)/(?<object>\\d+)(?:/(?<instance>\\d+)/?)?>");
    private static final Pattern PATTERN_OBJECT = Pattern.compile("</.*/\\d+>");
    private static final Pattern PATTERN_OBJECT_INSTANCE = Pattern.compile("</.*/\\d+/\\d+>");
    private RegistrationInfo registrationInfo;

    public RegistrationInfo parseRegistrationInfo(String payload, List<String> params) {
        registrationInfo = new RegistrationInfo();
        parseParams(params);
        parseRequest(payload);
        return registrationInfo;
    }

    private void parseObjectsAndInstances(Iterator<String> objectsAndInstances) {
        registrationInfo.objects = new ArrayList<>();
        while (objectsAndInstances.hasNext()) {
            String objectString = objectsAndInstances.next();
            Matcher matcher = PATTERN.matcher(objectString);
            if (matcher.find()) {
                String url = matcher.group("url");
                Integer objectId = Integer.valueOf(matcher.group("object"));
                Integer instanceId = Integer.valueOf(matcher.group("instance"));
                registrationInfo.objects.add(new RegistrationObjectInfo(url, objectId, instanceId));
            }
        }
    }

    private void parseRequest(String payload) {
        List<String> elems = Arrays.asList(payload.split(","));
        Iterator<String> iterator = elems.iterator();
        String elem = elems.get(0);

        if (!PATTERN.matcher(elem).find()) {
            String[] parts = elem.split(";");
            for (String part : parts) {
                if (part.startsWith("</")) {
                    registrationInfo.url = part.substring(2, part.length() - 1);
                } else if (part.startsWith("rt=")) {
                    registrationInfo.resourceType = part.substring(3, part.length() - 1);
                } else if (part.startsWith("ct=")) {
                    registrationInfo.contentType = Integer.valueOf(part.substring(3, part.length() - 1));
                }
            }
            iterator.next();
        }
        parseObjectsAndInstances(iterator);
    }

    private void parseParams(List<String> params) {
        for (String param : params) {
            String paramName = param.split("=")[0];
            String paramValue = param.split("=")[1];

            switch (paramName) {
                case "lt":
                    registrationInfo.lifetime = Integer.parseInt(paramValue);
                    break;
                case "sms":
                    registrationInfo.smsNumber = paramValue;
                    break;
                case "b":
                    registrationInfo.bindingMode = BindingMode.parseFromString(paramValue);
                    break;
                case "lwm2m":
                    registrationInfo.lwm2mVersion = paramValue;
                    break;
                case "ep":
                    registrationInfo.endpointClientName = paramValue;
                    break;
            }
        }
    }
}
