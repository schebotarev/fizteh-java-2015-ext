package ru.mipt.diht.samples.serialization.xml;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import ru.mipt.diht.samples.model.Car;
import ru.mipt.diht.samples.utils.Utils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

/**
 * @author s.chebotarev
 * @since 05.11.2015
 */
@Slf4j
public class XmlSerializerTest {
    private XmlSerializer serializer = new XmlSerializer();

    @Test
    public void testSerialization() {
        Car car = new Car();
        car.setId("EBHF1432141");
        car.setModel("DeLorean DMC-12");
        car.setDateOfManufacture(Utils.parseDate("1981.01.21"));
        log.info("Car: {}", car);

        String carJson = serializer.toXml(car);
        log.info("Json: {}", carJson);
        String expectedJson = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><car><dateOfManufacture>1981-01-21T00:00:00+03:00</dateOfManufacture><id>EBHF1432141</id><model>DeLorean DMC-12</model><priceRur>0</priceRur></car>";
        assertEquals("car json", expectedJson, carJson);

        Car restoredCar = serializer.fromXml(carJson, Car.class);
        log.info("Restored car: {}", restoredCar);
        assertEquals("restored car", car, restoredCar);
        assertNotSame("restored car ref", car, restoredCar);
    }
}