package org.motechproject.mds.builder;

import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.motechproject.commons.date.model.Time;
import org.motechproject.commons.date.util.DateUtil;
import org.motechproject.mds.builder.impl.EntityBuilderImpl;
import org.motechproject.mds.domain.Entity;
import org.motechproject.mds.domain.Type;
import org.motechproject.mds.repository.AllTypes;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.WordUtils.uncapitalize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.motechproject.mds.testutil.FieldTestHelper.field;
import static org.motechproject.mds.testutil.FieldTestHelper.newVal;

@RunWith(MockitoJUnitRunner.class)
public class EntityBuilderTest {

    private static final String ENTITY_NAME = "BuilderTest";

    private EntityBuilder entityBuilder = new EntityBuilderImpl();

    private MDSClassLoader mdsClassLoader;

    @Mock
    private Entity entity;

    @Before
    public void setUp() {
        mdsClassLoader = new MDSClassLoader(getClass().getClassLoader());
        when(entity.getClassName()).thenReturn(ENTITY_NAME);
    }

    @Test
    public void shouldBuildAnEntityWithFields() throws Exception {
        when(entity.getFields()).thenReturn(asList(field("count", Integer.class),
                field("time", Time.class), field("str", String.class), field("dec", Double.class),
                field("bool", Boolean.class), field("date", Date.class), field("dt", DateTime.class),
                field("list", List.class)));

        Class<?> clazz = buildClass();

        assertNotNull(clazz);
        assertField(clazz, "count", Integer.class);
        assertField(clazz, "time", Time.class);
        assertField(clazz, "str", String.class);
        assertField(clazz, "dec", Double.class);
        assertField(clazz, "bool", Boolean.class);
        assertField(clazz, "date", Date.class);
        assertField(clazz, "dt", DateTime.class);
        assertField(clazz, "list", List.class);
        assertField(clazz, "__IN_TRASH", Boolean.class, false);
    }

    @Test
    public void shouldBuildAnEntityWithFieldsWithDefaultValues() throws Exception {
        final Date date = new Date();
        final DateTime dateTime = DateUtil.now();

        when(entity.getFields()).thenReturn(asList(field("count", Integer.class, 1),
                field("time", Time.class, new Time(10, 10)), field("str", String.class, "defStr"),
                field("dec", Double.class, 3.1), field("bool", Boolean.class, true),
                field("date", Date.class, date), field("dt", DateTime.class, dateTime),
                field("list", List.class, asList("1", "2", "3"))));

        Class<?> clazz = buildClass();

        assertNotNull(clazz);
        assertField(clazz, "count", Integer.class, 1);
        assertField(clazz, "time", Time.class, new Time(10, 10));
        assertField(clazz, "str", String.class, "defStr");
        assertField(clazz, "dec", Double.class, 3.1);
        assertField(clazz, "bool", Boolean.class, true);
        assertField(clazz, "date", Date.class, date);
        assertField(clazz, "dt", DateTime.class, dateTime);
        assertField(clazz, "list", List.class, asList("1", "2", "3"));
        assertField(clazz, "__IN_TRASH", Boolean.class, false);

        java.lang.reflect.Field listField = clazz.getDeclaredField("list");
        // no exception = proper signature
        listField.getGenericType();
    }

    @Test
    public void shouldEditClasses() throws Exception {
        when(entity.getFields()).thenReturn(asList(field("name", Integer.class)));

        Class<?> clazz = buildClass();
        assertField(clazz, "name", Integer.class);

        when(entity.getFields()).thenReturn(asList(field("name2", String.class)));

        // reload the classloader for class edit
        mdsClassLoader = new MDSClassLoader(getClass().getClassLoader());

        clazz = buildClass();
        assertField(clazz, "name2", String.class);

        // assert that the first field no longer exists
        try {
            clazz.getDeclaredField("name");
            fail("Field 'name' was preserved in the class, although it was removed from the entity");
        } catch (NoSuchFieldException e) {
            // expected
        }
    }

    @Test
    public void shouldBuildHistoryClass() throws Exception {
        when(entity.getFields()).thenReturn(asList(field("id", Long.class),
                field("count", Integer.class), field("time", Time.class),
                field("str", String.class), field("dec", Double.class),
                field("bool", Boolean.class), field("date", Date.class),
                field("dt", DateTime.class), field("list", List.class)));

        when(entity.getField("id")).thenReturn(field("id", Long.class));

        ClassData classData = entityBuilder.buildHistory(entity);
        assertEquals(ENTITY_NAME + "__", classData.getClassName());

        Class<?> clazz = mdsClassLoader.defineClass(classData.getClassName(), classData.getBytecode());

        assertNotNull(clazz);
        assertField(clazz, clazz.getSimpleName() + "CurrentVersion", Long.class);
        assertField(clazz, clazz.getSimpleName() + "Previous", clazz);
        assertField(clazz, clazz.getSimpleName() + "Next", clazz);
        assertField(clazz, "id", Long.class);
        assertField(clazz, "count", Integer.class);
        assertField(clazz, "time", Time.class);
        assertField(clazz, "str", String.class);
        assertField(clazz, "dec", Double.class);
        assertField(clazz, "bool", Boolean.class);
        assertField(clazz, "date", Date.class);
        assertField(clazz, "dt", DateTime.class);
        assertField(clazz, "list", List.class);
    }

    private Class<?> buildClass() {
        ClassData classData = entityBuilder.build(entity);

        assertEquals(ENTITY_NAME, classData.getClassName());

        return mdsClassLoader.defineClass(classData.getClassName(), classData.getBytecode());
    }

    private void assertField(Class<?> clazz, String name, Class<?> fieldType) throws Exception {
        assertField(clazz, name, fieldType, null);
    }

    private void assertField(Class<?> clazz, String name, Class<?> fieldType, Object expectedDefaultVal)
            throws NoSuchFieldException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        String uncapitalizeName =  uncapitalize(name);
        java.lang.reflect.Field field = clazz.getDeclaredField(uncapitalizeName);

        assertNotNull(field);
        assertEquals(Modifier.PRIVATE, field.getModifiers());
        assertEquals(fieldType, field.getType());

        Object instance = clazz.newInstance();
        Object val = ReflectionTestUtils.getField(instance, uncapitalizeName);
        assertEquals(expectedDefaultVal, val);

        // assert getters and setters

        Method getter = clazz.getMethod("get" + StringUtils.capitalize(uncapitalizeName));
        assertEquals(fieldType, getter.getReturnType());
        assertEquals(Modifier.PUBLIC, getter.getModifiers());

        Method setter = clazz.getMethod("set" + StringUtils.capitalize(uncapitalizeName), fieldType);
        assertEquals(Void.TYPE, setter.getReturnType());
        assertEquals(Modifier.PUBLIC, setter.getModifiers());

        // getter returns default value
        assertEquals(expectedDefaultVal, getter.invoke(instance));

        // set then get
        Object newVal = newVal(fieldType);
        setter.invoke(instance, newVal);

        assertEquals(newVal, getter.invoke(instance));
    }
}