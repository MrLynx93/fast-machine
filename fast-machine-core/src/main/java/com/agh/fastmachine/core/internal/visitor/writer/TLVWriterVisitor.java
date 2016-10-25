package com.agh.fastmachine.core.internal.visitor.writer;

import com.agh.fastmachine.core.internal.model.ObjectBaseModel;
import com.agh.fastmachine.core.internal.model.ObjectInstanceModel;
import com.agh.fastmachine.core.internal.model.ObjectMultipleResourceModel;
import com.agh.fastmachine.core.internal.model.ObjectResourceModel;
import com.agh.fastmachine.core.api.model.resourcevalue.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class TLVWriterVisitor extends AbstractWriterVisitor {
    private final int resourceHeaderCode = 0b11000000;
    private final int multipleResourceHeaderCode = 0b10000000;
    private final int resourceInstanceHeaderCode = 0b01000000;
    private final int objectInstanceHeaderCode = 0b00000000;

    private byte[] content;
    
    @Override
    public <T extends ObjectInstanceModel> void visit(ObjectBaseModel<T> objectBase) {
        int numberOfInstances = objectBase.getObjectInstances().size();

        int totalInstancesLength = 0;
        byte[][] instances = new byte[numberOfInstances][];
        int index = 0;

        for (ObjectInstanceModel objectInstance : objectBase.getObjectInstances().values()) {
            objectInstance.accept(this);
            byte[] header = createHeader(objectInstanceHeaderCode, objectInstance.getId(), content.length);
            byte[] instance = new byte[header.length + content.length];
            System.arraycopy(header, 0, instance, 0, header.length);
            System.arraycopy(content, 0, instance, header.length, content.length);
            instances[index++] = instance;
            totalInstancesLength += instance.length;
        }

        byte[] object = new byte[totalInstancesLength];
        int position = 0;
        for (int i = 0; i < numberOfInstances; i++) {
            byte[] instance = instances[i];
            System.arraycopy(instance, 0, object, position, instance.length);
            position += instance.length;
        }
        content = object;
    }

    @Override
    public void visit(ObjectInstanceModel objectInstance) {
        int numberOfResources = objectInstance.getResources().size();

        int totalResourcesLength = 0;
        byte[][] resources = new byte[numberOfResources][];
        int index = 0;

        for (ObjectResourceModel<?> objectResource : objectInstance.getResources().values()) {
            if (objectResource.getValue() != null && objectResource.getValue().value != null) {
                objectResource.accept(this);
                resources[index++] = content;
                totalResourcesLength += content.length;
            } else {
                numberOfResources--;
            }
        }


        byte[] instance = new byte[totalResourcesLength];

        int position = 0;
        for (int i = 0; i < numberOfResources; i++) {
            byte[] resource = resources[i];
            System.arraycopy(resource, 0, instance, position, resource.length);
            position += resource.length;
        }
        content = instance;
    }

    @Override
    public void visit(ObjectResourceModel objectResource) {
        objectResource.getValue().accept(this);
        byte[] header = createHeader(resourceHeaderCode, objectResource.getId(), content.length);
        byte[] resource = new byte[content.length + header.length];
        System.arraycopy(header, 0, resource, 0, header.length);
        System.arraycopy(content, 0, resource, header.length, content.length);
        content = resource;
    }

    @Override
    public void visit(ObjectMultipleResourceModel objectMultipleResource) {
        int numberOfResourceInstances = objectMultipleResource.getValues().size();
        ResourceValue<?>[] resourceValues = new ResourceValue[numberOfResourceInstances];
        objectMultipleResource.getValues().values().toArray(resourceValues);

        byte[][] resourceInstanceValue = new byte[numberOfResourceInstances][];
        byte[][] resourceInstanceHeader = new byte[numberOfResourceInstances][];

        int totalResourceInstancesLength = 0;
        for (int i = 0; i < numberOfResourceInstances; i++) {
            resourceValues[i].accept(this);
            byte[] header = createHeader(resourceInstanceHeaderCode, resourceValues[i].id, content.length);
            totalResourceInstancesLength += content.length + header.length;
            resourceInstanceValue[i] = content;
            resourceInstanceHeader[i] = header;
        }

        byte[] header = createHeader(multipleResourceHeaderCode, objectMultipleResource.getId(), totalResourceInstancesLength);

        byte[] multipleResource = new byte[totalResourceInstancesLength + header.length];
        System.arraycopy(header, 0, multipleResource, 0, header.length);

        int position = header.length;
        for (int i = 0; i < numberOfResourceInstances; i++) {
            System.arraycopy(resourceInstanceHeader[i], 0, multipleResource, position, resourceInstanceHeader[i].length);
            position += resourceInstanceHeader[i].length;
            System.arraycopy(resourceInstanceValue[i], 0, multipleResource, position, resourceInstanceValue[i].length);
            position += resourceInstanceValue[i].length;
        }

        content = multipleResource;
    }


    @Override
    public void visit(StringResourceValue resourceValue) {
        content = resourceValue.value.getBytes(Charset.forName("UTF-8"));

    }

    @Override
    public void visit(IntegerResourceValue resourceValue) {
        int value = resourceValue.value;
        setIntegerContent(value);
    }

    @Override
    public void visit(BooleanResourceValue resourceValue) {
        content = new byte[]{(byte) (resourceValue.value ? 1 : 0)};
    }

    @Override
    public void visit(DateResourceValue resourceValue) {
        long value = resourceValue.value.getTime() / 1000;
        setLongContent(value);
    }

    @Override
    public void visit(DoubleResourceValue resourceValue) {
        content = ByteBuffer.allocate(8).putDouble(resourceValue.value).array();
    }

    @Override
    public void visit(FloatResourceValue resourceValue) {
        content = ByteBuffer.allocate(4).putFloat(resourceValue.value).array();
    }

    @Override
    public void visit(LongResourceValue resourceValue) {
        setLongContent(resourceValue.value);
    }

    @Override
    public void visit(OpaqueResourceValue resourceValue) {
        content = resourceValue.value;
    }


    private byte[] createHeader(int type, int id, int length) {
        //header: type(byte) id(1 or 2 bytes) length( 0 to 3 bytes)
        byte[] header = new byte[5];

        header[0] = (byte) type;
        header[0] &= 0b11100111;

        // Set type and id
        int nextByte;
        if (id < 256) {
            header[0] &= 0b11011111;
            header[1] = (byte) id;
            nextByte = 2;
        } else {
            header[0] |= 0b00100000;
            header[1] = (byte) (id >> 8);
            header[2] = (byte) id;
            nextByte = 3;
        }

        // Set length
        if (length < 8) {
            header[0] |= length;

        } else if (length < 256) {
            header[0] |= 0b00001000;
            header[nextByte++] = (byte) length;
        } else if (length < 32767) {
            header[0] |= 0b00010000;
            header[nextByte++] = (byte) (length >> 8);
            header[nextByte++] = (byte) length;

        } else {
            header[0] |= 0b00011000;
            header[nextByte++] = (byte) (length >> 16);
            header[nextByte++] = (byte) (length >> 8);
            header[nextByte++] = (byte) length;
        }

        return Arrays.copyOfRange(header, 0, nextByte);
    }

    private void setLongContent(long value) {
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            content = ByteBuffer.allocate(8).putLong(value).array();
        } else {
            setIntegerContent((int) value);
        }
    }

    private void setIntegerContent(int value) {
        if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
            content = new byte[]{(byte) value};
        } else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
            content = ByteBuffer.allocate(2).putShort((short) value).array();
        } else {
            content = ByteBuffer.allocate(4).putInt(value).array();
        }
    }

    @Override
    protected byte[] getBytes() {
        return content;
    }
}
