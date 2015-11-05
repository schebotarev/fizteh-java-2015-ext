package ru.mipt.diht.samples.serialization.json;

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
public class JsonSerializerTest {
    private JsonSerializer serializer = new JsonSerializer();

    @Test
    public void testSerialization() {
        Car car = new Car();
        car.setId("EBHF1432141");
        car.setModel("DeLorean DMC-12");
        car.setDateOfManufacture(Utils.parseDate("1981.01.21"));
        log.info("Car: {}", car);

        String carJson = serializer.toJson(car);
        log.info("Json: {}", carJson);
        String expectedJson = "{\"id\":\"EBHF1432141\",\"model\":\"DeLorean DMC-12\",\"dateOfManufacture\":348872400000,\"priceRur\":0}";
        assertEquals("car json", expectedJson, carJson);

        Car restoredCar = serializer.fromJson(carJson, Car.class);
        log.info("Restored car: {}", restoredCar);
        assertEquals("restored car", car, restoredCar);
        assertNotSame("restored car ref", car, restoredCar);
    }
}