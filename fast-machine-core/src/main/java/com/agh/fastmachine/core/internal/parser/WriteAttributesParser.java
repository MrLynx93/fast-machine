package com.agh.fastmachine.core.internal.parser;

import com.agh.fastmachine.core.api.model.Attributes;
import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.FloatResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.IntegerResourceValue;
import com.agh.fastmachine.core.api.model.resourcevalue.LongResourceValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WriteAttributesParser {
    private static final Logger LOG = LoggerFactory.getLogger(WriteAttributesParser.class);

    private static Pattern PATTERN = Pattern.compile("<(?<object>\\d+)/(?<instance>\\d+)/(?<resource>\\d+)/>");
    private static Pattern PATTERN_ATTR = Pattern.compile("\\w+=\\w+");

    public Attributes parseResourceWriteAttributes(String payload, ObjectResourceModel<?> resource) {
        Class valueType = resource.getValueType();
        Attributes attributes = new Attributes(isNumericValue(valueType));

        if (!payload.contains(";")) {
            return null;
        }

        List<String> attributesStrings = Arrays.asList(payload.split(",")[0].split(";"));
        attributesStrings = attributesStrings.subList(1, attributesStrings.size());
        for (String elem : attributesStrings) {
            if (PATTERN_ATTR.matcher(elem).find()) {
                String attributeName = elem.split("=")[0];
                String attributeValue = elem.split("=")[1];

                switch (attributeName) {
                    case Attributes.MINIMUM_PERIOD:
                        attributes.minimumPeriod = Integer.valueOf(attributeValue);
                        break;
                    case Attributes.MAXIMUM_PERIOD:
                        attributes.maximumPeriod = Integer.valueOf(attributeValue);
                        break;
                    case Attributes.GREATER_THAN:
                        attributes.greaterThan = Double.valueOf(attributeValue);
                        break;
                    case Attributes.LESS_THAN:
                        attributes.lessThan = Double.valueOf(attributeValue);
                        break;
                    case Attributes.STEP:
                        attributes.step = Double.valueOf(attributeValue);
                        break;
                    default:
                        throw new IllegalArgumentException("Write attribute [" + attributeName + "] not supported");
                }
            }
        }
        LOG.debug("Parsed write attributes from {} to {}", payload, attributes.toString());
        return attributes;
    }

    public Attributes parseWriteAttributes(String payload) {
        Attributes attributes = new Attributes(false);

        if (!payload.contains(";")) {
            return null;
        }

        List<String> attributesStrings = Arrays.asList(payload.split(",")[0].split(";"));
        attributesStrings = attributesStrings.subList(1, attributesStrings.size());
        for (String elem : attributesStrings) {
            if (PATTERN_ATTR.matcher(elem).find()) {
                String attributeName = elem.split("=")[0];
                String attributeValue = elem.split("=")[1];

                switch (attributeName) {
                    case Attributes.MINIMUM_PERIOD:
                        attributes.minimumPeriod = Integer.valueOf(attributeValue);
                        break;
                    case Attributes.MAXIMUM_PERIOD:
                        attributes.maximumPeriod = Integer.valueOf(attributeValue);
                        break;
                    default:
                        throw new IllegalArgumentException("Write attribute [" + attributeName + "] not supported");
                }
            }
        }
        LOG.debug("Parsed write attributes from {} to {}", payload, attributes.toString());
        return attributes;
    }

    public List<Integer> parseSupportedResources(String payload) { // TODO do naprawienia???
        List<String> elems = Arrays.asList(payload.split(","));
        Iterator<String> iterator = elems.iterator();
        return parseObjectsAndInstances(iterator);
    }

    private List<Integer> parseObjectsAndInstances(Iterator<String> objectsAndInstances) { // TODO do naprawienia???
        List<Integer> resources = new ArrayList<>();

        while (objectsAndInstances.hasNext()) {
            String objectString = objectsAndInstances.next();
            Matcher matcher = PATTERN.matcher(objectString);
            if (matcher.find()) {
                Integer resourceId = Integer.valueOf(matcher.group("resourcevalue"));
                resources.add(resourceId);
            }
        }
        return resources;
    }

    private boolean isNumericValue(Class<?> valueClass) {
        return valueClass.equals(IntegerResourceValue.class) || valueClass.equals(FloatResourceValue.class) || valueClass.equals(LongResourceValue.class);
    }
}
