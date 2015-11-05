package ru.mipt.diht.samples.serialization.xml;

import lombok.SneakyThrows;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author s.chebotarev
 * @since 05.11.2015
 */
public class XmlSerializer {
    @SneakyThrows
    public String toXml(Object object) {
        JAXBContext context = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = context.createMarshaller();
        StringWriter writer = new StringWriter();
        marshaller.marshal(object, writer);
        return writer.toString();
    }

    @SneakyThrows
    public <T> T fromXml(String xml, Class<T> type) {
        JAXBContext context = JAXBContext.newInstance(type);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Object result = unmarshaller.unmarshal(new StringReader(xml));
        return type.cast(result);
    }
}
