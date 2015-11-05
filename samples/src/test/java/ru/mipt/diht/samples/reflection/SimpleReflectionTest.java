package ru.mipt.diht.samples.reflection;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import ru.mipt.diht.samples.model.Car;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * @author s.chebotarev
 * @since 05.11.2015
 */
@Slf4j
public class SimpleReflectionTest {
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private Collection<Car> carsHolder = new ArrayList<>();

    @Test
    public void testGeneratedMethods() throws Exception {
        Class<?> enrichedType = Car.class;
        Method[] methods = enrichedType.getDeclaredMethods();
        for (Method method : methods) {
            log.info("{}({}): {}", method.getName(), method.getParameterTypes(), method.getReturnType().getSimpleName());
        }
        assertTrue("there are generated methods", methods.length > 0);
    }

    @Test
    public void testFields() {
        Field[] fields = Car.class.getDeclaredFields();
        for (Field field : fields) {
            log.info("{}: {}", field.getName(), field.getType().getSimpleName());
        }
        assertTrue("there are some fields", fields.length > 0);
    }

    @Test
    public void testConstuctors() {
        Constructor[] constructors = Car.class.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            log.info("{}", constructor);
        }
        assertTrue("there are some constructors", constructors.length > 0);
    }

    @Test
    public void testDynamicObjectClass() {
        Object obj = new Car();
        assertEquals("car class", Car.class, obj.getClass());
        assertSame("car class", Car.class, obj.getClass());
        assertEquals("string class", String.class, "123".getClass());
        assertSame("string class", String.class, "123".getClass());
    }

    @Test
    public void testParentClasses() {
        Class type = Car.class;
        Class parent = type.getSuperclass();
        assertNotNull("Car.super", parent);

        log.info("inspecting Car.class superclasses");
        for (; type != null; type = type.getSuperclass()) {
            log.info("=> {}", type.getSimpleName());
        }

        log.info("inspecting Class.class superclasses");
        type = Class.class;
        for (; type != null; type = type.getSuperclass()) {
            log.info("=> {}", type.getSimpleName());
        }
    }

    @Test
    public void testGenericsInfo() throws Exception {
        log.info("{}", getClass().getDeclaredField("carsHolder").getGenericType());
        log.info("{}", carsHolder.getClass().getTypeParameters());
    }

    @Test
    public void testPublicAccess() throws Exception {
        Car car = new Car();
        car.setId("id123");
        Object res = Car.class.getMethod("getId").invoke(car);
        log.info("call res: {}", res);
    }

    @Test(expected = IllegalAccessException.class)
    public void testIllegalPrivateAccess() throws Exception {
        Car car = new Car();
        car.setId("id123");
        Object res = Car.class.getDeclaredField("id").get(car);
        log.info("field access res: {}", res);
    }

    @Test
    public void testSetAccessible() throws Exception {
        Car car = new Car();
        car.setId("id123");
        Field field = Car.class.getDeclaredField("id");
        field.setAccessible(true); // only for this instance
        Object res = field.get(car);
        log.info("field access res: {}", res);
    }

    @Test
    public void testConstructor() throws Exception {
        Car car1 = new Car();
        Car car2 = Car.class.newInstance();
        assertNotNull(car2);
        assertEquals(car1, car2);
        assertNotSame(car1, car2);
    }

    @Test
    public void testAnnotation() throws Exception {
        Test annotation = getClass().getMethod("testIllegalPrivateAccess").getAnnotation(Test.class);
        assertNotNull(annotation);
        assertEquals("expected exception", IllegalAccessException.class, annotation.expected());
        assertEquals("timeout", 0, annotation.timeout());
        assertEquals("timeout", 0L, annotation.annotationType().getMethod("timeout").getDefaultValue());
    }

    @Test
    public void testClassForName() throws Exception {
        assertEquals("Car class found by name", Car.class, Class.forName("ru.mipt.diht.samples.model.Car"));
    }
}
