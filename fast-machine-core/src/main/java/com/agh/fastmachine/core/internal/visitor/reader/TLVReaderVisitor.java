package com.agh.fastmachine.core.internal.visitor.reader;

import com.agh.fastmachine.core.internal.model.*;
import com.agh.fastmachine.core.internal.parser.ObjectFactory;
import com.agh.fastmachine.core.api.model.resourcevalue.*;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;

public class TLVReaderVisitor extends AbstractReaderVisitor {
    private byte[] content;
    private final ObjectFactory objectFactory;
    private int position = 0;
    private int id;
    private int length;

    private ResourceValue<?> value;
    private ObjectNodeModel nodeModel;

    public TLVReaderVisitor(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public <T extends ObjectNodeModel> T read(T node, byte[] content) {
        reset(content);
        if (node instanceof ObjectResourceModel)
            readHeader();
        node.accept(this);
        return (T) nodeModel;
    }

    @Override
    public <T extends ObjectInstanceModel> void visit(ObjectBaseModel<T> objectBase) {
        Map<Integer, ObjectInstanceModel> instances = new TreeMap<>();
        int instancesEnd = position + length;
        while (position < instancesEnd) {
            readHeader();
            int instanceId = id;
            ObjectInstanceModel instance = objectBase.getInstance(instanceId);
            if (instance != null) {
                instance.accept(this);
                instances.put(instanceId, (ObjectInstanceModel) nodeModel);
            } else {
                position += length;
            }
        }
        nodeModel = objectFactory.createObjectBase(instances);
    }

    @Override
    public void visit(ObjectInstanceModel objectInstance) {
        Map<Integer, ObjectResourceModel<?>> resources = new HashMap<>();
        int resourcesEnd = position + length;
        while (position < resourcesEnd) {
            readHeader();
            ObjectResourceModel<?> resource = objectInstance.getResource(id);
            if (resource != null) {
                resource.accept(this);
                resources.put(nodeModel.getId(), (ObjectResourceModel<?>) nodeModel);
            } else
                position += length;
        }
        nodeModel = objectFactory.createObjectInstance(objectInstance.getId(), resources);
    }

    @Override
    public <T extends ResourceValue<?>> void visit(ObjectResourceModel<T> objectResource) {
        if (objectResource.getValue() == null) {
            try {
                ((ResourceValue) objectResource.getValueType().getConstructors()[1].newInstance(null, id)).accept(this);
            } catch (Exception e) {
            }
        } else {
            objectResource.getValue().accept(this);
        }
        nodeModel = objectFactory.createObjectResource(id, (T) value, objectResource.getValueType());
    }


    @Override
    public <T extends ResourceValue<?>> void visit(ObjectMultipleResourceModel<T> objectMultipleResource) {
        int resourceId = this.id;
        int resourceValuesEnd = position + length;
        Map<Integer, T> resources = new HashMap<>();
        while (position < resourceValuesEnd) {
            readHeader();
            try {
                ((ResourceValue) objectMultipleResource.getValueType().getConstructors()[1].newInstance(null, id)).accept(this);
            } catch (Exception e) {
            }
            resources.put(value.id, (T) value);
        }
        nodeModel = objectFactory.createObjectMultipleResource(resourceId, resources, objectMultipleResource.getValueType());
    }

    @Override
    public void visit(StringResourceValue resourceValue) {
        byte[] copy = new byte[length];
        System.arraycopy(content, position, copy, 0, length);
        value = new StringResourceValue(new String(copy, Charset.forName("UTF-8")), resourceValue.id);
        position += length;
    }

    @Override
    public void visit(IntegerResourceValue resourceValue) {
        value = new IntegerResourceValue((int) readNumber(), resourceValue.id);
    }

    @Override
    public void visit(BooleanResourceValue resourceValue) {
        value = new BooleanResourceValue(content[position++] == 1, resourceValue.id);
    }

    @Override
    public void visit(DateResourceValue resourceValue) {
        value = new DateResourceValue(new Date(readNumber() * 1000), resourceValue.id);
    }

    @Override
    public void visit(DoubleResourceValue resourceValue) {
        value = new DoubleResourceValue(ByteBuffer.wrap(content, position, length).getDouble(), resourceValue.id);
        position += length;
    }

    @Override
    public void visit(FloatResourceValue resourceValue) {
        value = new FloatResourceValue(ByteBuffer.wrap(content, position, length).getFloat(), resourceValue.id);
        position += length;
    }

    @Override
    public void visit(LongResourceValue resourceValue) {
        value = new LongResourceValue(readNumber(), resourceValue.id);
    }

    @Override
    public void visit(LinkResourceValue resourceValue) {
        short objectId = ByteBuffer.wrap(Arrays.copyOfRange(content, 0, 2)).getShort();
        short instanceId = ByteBuffer.wrap(Arrays.copyOfRange(content, 3, 5)).getShort();
        Link link = new Link((int) objectId, (int) instanceId);
        value = new LinkResourceValue(link);
    }

    @Override
    public void visit(OpaqueResourceValue resourceValue) {
        byte[] copy = new byte[length];
        System.arraycopy(content, position, copy, 0, length);
        value = new OpaqueResourceValue(copy, resourceValue.id);
        position += length;
    }

    private void reset(byte[] content) {
        this.content = content;
        position = 0;
        length = this.content.length;
    }

    private void readHeader() {
        byte header = content[position++];
        int id;
        if ((header & 0b00100000) == 0) {
            id = content[position++];
        } else {
            id = ((content[position++] & 0xFF) << 8) | (content[position++] & 0xFF);
        }
        int lengthType = (header & 0b00011000) >> 3;
        int length = 0;
        switch (lengthType) {
            case 0:
                length = header & 0b00000111;
                break;
            case 1:
                length = content[position++] & 0xFF;
                break;
            case 2:
                length = ((content[position++] & 0xFF) << 8) | (content[position++] & 0xFF);
                break;
            case 3:
                length = ((content[position++] & 0xFF) << 16) | ((content[position++] & 0xFF) << 8) | content[position++] & 0xFF;
                break;
        }

        this.id = id;
        this.length = length;
    }

    private long readNumberOfLength(int len) {
        long result = 0;
        for (int i = len - 1; i >= 0; i--) {
            result |= (content[position++] & 0xFF) << i * 8;
        }
        return result;
    }

    private long readNumber() {
        long result = 0;
        for (int i = length - 1; i >= 0; i--) {
            result |= (content[position++] & 0xFF) << i * 8;
        }
        return result;
    }
}
