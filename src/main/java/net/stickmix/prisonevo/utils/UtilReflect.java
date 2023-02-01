package net.stickmix.prisonevo.utils;

import org.apache.commons.lang.math.NumberUtils;
import sun.reflect.ReflectionFactory;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author 0xC0deBabe <iam@kostya.sexy>
 */
public class UtilReflect {

    private static final Map<String, ClassData> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <ARG, RETURN> Function<ARG, RETURN> createConstructorLambda(Class<RETURN> clazz, Class<ARG> constructorArgumentClass) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodType constructorType = MethodType.methodType(void.class, constructorArgumentClass);
            MethodHandle mh = lookup.findConstructor(clazz, constructorType);
            return (Function<ARG, RETURN>) LambdaMetafactory.metafactory(
                    lookup,
                    "apply",
                    MethodType.methodType(Function.class),
                    mh.type().generic(),
                    mh,
                    mh.type()
            ).getTarget().invokeExact();
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Returns list of fields (with their types) of the class of given object and of all superclasses.
     *
     * @param object
     * @return human-understandable list of fields with their types.
     */
    public static List<String> listFieldsRecursively(Object object) {
        List<String> list = new ArrayList<>();
        Class clazz = object.getClass();
        while (clazz != Object.class) {
            for (Field f : clazz.getDeclaredFields()) {
                list.add(String.format("%s (%s)", f.getName(), f.getType().getSimpleName()));
            }
            clazz = clazz.getSuperclass();
        }
        return list;
    }

    /**
     * Returns list of fields (with their types) of the class of given object and.
     *
     * @param object
     * @return human-understandable list of fields with their types.
     */
    public static List<String> listFields(Object object) {
        List<String> list = new ArrayList<>();
        Class clazz = object.getClass();
        for (Field f : clazz.getDeclaredFields()) {
            list.add(String.format("%s (%s)", f.getName(), f.getType().getSimpleName()));
        }
        return list;
    }

    /**
     * Returns list of methods (with their arguments) of the class of given object and of all superclasses.
     *
     * @param object
     * @return human-understandable list of methods with their arguments.
     */
    public static List<String> listMethodsRecursively(Object object) {
        List<String> list = new ArrayList<>();
        Class clazz = object.getClass();
        while (clazz != Object.class) {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().contains("$")) {
                    continue;
                }
                StringBuilder args = new StringBuilder();
                for (Class cls : m.getParameterTypes()) {
                    args.append(cls.getSimpleName()).append(" ");
                }
                list.add(String.format("%s (%s)", m.getName(), args.toString().trim()));
            }
            clazz = clazz.getSuperclass();
        }
        return list;
    }

    /**
     * Returns list of methods (with their arguments) of the class of given object.
     *
     * @param object
     * @return human-understandable list of methods with their arguments.
     */
    public static List<String> listMethods(Object object) {
        List<String> list = new ArrayList<>();
        Class clazz = object.getClass();
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().contains("$")) {
                continue;
            }
            StringBuilder args = new StringBuilder();
            for (Class cls : m.getParameterTypes()) {
                args.append(cls.getSimpleName()).append(" ");
            }
            list.add(String.format("%s (%s)", m.getName(), args.toString().trim()));
        }
        return list;
    }

    /**
     * Returns value of field of the given name of target object's class or of one of it's superclasses.
     *
     * @param object    target object.
     * @param fieldName name of the field.
     * @return value of the field.
     * @throws Exception
     */
    public static Object getFieldRecursively(Object object, String fieldName) throws Exception {
        Class clazz = object.getClass();
        boolean ended = false;
        do {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getName().equalsIgnoreCase(fieldName)) {
                    ended = true;
                    f.setAccessible(true);
                    try {
                        return f.get(object);
                    } finally {
                        f.setAccessible(false);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class && !ended);
        throw new NullPointerException("There is no field with given name");
    }

    /**
     * Returns value of field of the given name of target object's class.
     *
     * @param object    target object.
     * @param fieldName name of the field.
     * @return value of the field.
     * @throws Exception
     */
    public static Object getField(Object object, String fieldName) throws Exception {
        Field f = object.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        try {
            return f.get(object);
        } finally {
            f.setAccessible(false);
        }
    }

    public static Object getField(Object object, Class<?> parent, String fieldName) throws Exception {
        Class<?> current = object.getClass();
        while (current != null && current != parent) {
            current = current.getSuperclass();
        }
        if (current == null) {
            return null;
        }
        Field f = current.getDeclaredField(fieldName);
        f.setAccessible(true);
        try {
            return f.get(object);
        } finally {
            f.setAccessible(false);
        }
    }

    public static Object getStaticField(Class<?> clazz, String fieldName) throws Exception {
        Field f = clazz.getDeclaredField(fieldName);
        f.setAccessible(true);
        try {
            return f.get(null);
        } finally {
            f.setAccessible(false);
        }
    }

    /**
     * Sets value of field of the given name of target object's class or of one of it's superclasses.
     * Be aware that string-value which is convertable to number WILL BE converted to number.
     *
     * @param object    target object.
     * @param fieldName name of the field.
     * @param value     new value of the field.
     * @throws Exception
     */
    public static void setFieldRecursively(Object object, String fieldName, Object value) throws Exception {
        Class clazz = object.getClass();
        boolean ended = false;
        do {
            for (Field f : clazz.getDeclaredFields()) {
                if (f.getName().equalsIgnoreCase(fieldName)) {
                    ended = true;
                    f.setAccessible(true);
                    if (value instanceof String) {
                        if (NumberUtils.isNumber((String) value)) {
                            value = NumberUtils.createNumber((String) value);
                        } else {
                            try {
                                value = f.getType().getConstructor(String.class).newInstance(value);
                            } catch (NoSuchMethodException | SecurityException | InstantiationException |
                                     IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                            }
                        }
                    }
                    f.set(object, value);
                    f.setAccessible(false);
                    return;
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class && !ended);
        throw new NullPointerException("There is no field with given name");
    }

    /**
     * Sets value of field of the given name of target object's class.
     * Be aware that string-value which is convertable to number WILL BE converted to number.
     *
     * @param object    target object.
     * @param fieldName name of the field.
     * @param value     new value of the field.
     * @throws Exception
     */
    public static void setField(Object object, String fieldName, Object value) throws Exception {
        Field f = object.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        if (value instanceof String) {
            if (NumberUtils.isNumber((String) value)) {
                value = NumberUtils.createNumber((String) value);
            } else {
                try {
                    value = f.getType().getConstructor(String.class).newInstance(value);
                } catch (NoSuchMethodException | SecurityException | InstantiationException |
                         IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                }
            }
        }
        f.set(object, value);
        f.setAccessible(false);
    }

    /**
     * Invokes method with given name and arguments of target object's class or one of it's superclasses.
     *
     * @param object     target object.
     * @param methodName name of the method.
     * @param args       arguments.
     * @throws Exception
     */
    public static void invokeMethodRecursively(Object object, String methodName, Object... args) throws Exception {
        invokeMethodAndGetRecursively(object, methodName, args);
    }

    /**
     * Invokes method with given name and arguments of target object's class.
     *
     * @param object     target object.
     * @param methodName name of the method.
     * @param args       arguments.
     * @throws Exception
     */
    public static void invokeMethod(Object object, String methodName, Object... args) throws Exception {
        invokeMethodAndGet(object, methodName, args);
    }

    /**
     * Invokes given class method with given name and arguments.
     *
     * @param owner      class which contains method with given name
     * @param methodName name of the method.
     * @param args       arguments.
     * @throws Exception
     */
    public static void invokeMethodStatic(Class owner, String methodName, Object... args) throws Exception {
        invokeMethodAndGetStatic(owner, methodName, args);
    }

    /**
     * Invokes method with given name and arguments of target object's class or one of it's superclasses.
     * Be aware of that all boolean/number-convertable arguments WILL BE converted to boolean/number.
     *
     * @param object     target object.
     * @param methodName name of the method.
     * @param strings    arguments.
     * @throws Exception
     */
    public static void invokeMethodWithStringArgsRecursively(Object object, String methodName, String... strings) throws Exception {
        invokeMethodAndGetWithStringArgsRecursively(object, methodName, strings);
    }

    /**
     * Invokes method with given name and arguments of target object's class.
     * Be aware of that all boolean/number-convertable arguments WILL BE converted to boolean/number.
     *
     * @param object     target object.
     * @param methodName name of the method.
     * @param strings    arguments.
     * @throws Exception
     */
    public static void invokeMethodWithStringArgs(Object object, String methodName, String... strings) throws Exception {
        invokeMethodAndGetWithStringArgs(object, methodName, strings);
    }

    /**
     * Invokes and returns result of method with given name and arguments of target object's class or one of it's superclasses.
     *
     * @param object     target object.
     * @param methodName name of the method.
     * @param args       arguments.
     * @return result of method invokation.
     * @throws Exception
     */
    public static Object invokeMethodAndGetRecursively(Object object, String methodName, Object... args) throws Exception {
        Class clazz = object.getClass();
        boolean ended = false;
        do {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equalsIgnoreCase(methodName)) {
                    ended = true;
                    m.setAccessible(true);
                    try {
                        return m.invoke(object, args);
                    } finally {
                        m.setAccessible(false);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class && !ended);
        throw new NullPointerException("There is no method with given name");
    }

    /**
     * Invokes and returns result of method with given name and arguments of target object's class.
     *
     * @param object     target object.
     * @param methodName name of the method.
     * @param args       arguments.
     * @return result of method invokation.
     * @throws Exception
     */
    public static Object invokeMethodAndGet(Object object, String methodName, Object... args) throws Exception {
        Class[] params = new Class[args.length];
        for (int i = 0; i < args.length; ++i) {
            params[i] = args[i].getClass();
        }
        Method m = object.getClass().getDeclaredMethod(methodName, params);
        m.setAccessible(true);
        try {
            return m.invoke(object, args);
        } finally {
            m.setAccessible(false);
        }
    }

    /**
     * Invokes and returns result of given class method execution with given name and arguments.
     *
     * @param owner      class which contains specified method.
     * @param methodName name of the method.
     * @param args       arguments.
     * @return result of method invokation.
     * @throws Exception
     */
    public static Object invokeMethodAndGetStatic(Class owner, String methodName, Object... args) throws Exception {
        Class[] params = new Class[args.length];
        for (int i = 0; i < args.length; ++i) {
            params[i] = args[i].getClass();
        }
        Method m = owner.getDeclaredMethod(methodName, params);
        m.setAccessible(true);
        try {
            return m.invoke(null, args);
        } finally {
            m.setAccessible(false);
        }
    }

    /**
     * Invokes and returns result of method with given name and arguments of target object's class or one of it's superclasses.
     * Be aware of that all boolean/number-convertable arguments WILL BE converted to boolean/number.
     *
     * @param object     target object.
     * @param methodName name of the method.
     * @param strings    arguments.
     * @return result of method invokation.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Object invokeMethodAndGetWithStringArgsRecursively(Object object, String methodName, String... strings) throws Exception {
        Class clazz = object.getClass();
        boolean ended = false;
        do {
            for (Method m : clazz.getDeclaredMethods()) {
                if (m.getName().equalsIgnoreCase(methodName)) {
                    ended = true;
                    Class[] types = m.getParameterTypes();
                    Object[] args = new Object[strings.length];
                    for (int i = 0; i < args.length; ++i) {
                        String s = strings[i];
                        if (s.equalsIgnoreCase("true")) {
                            args[i] = true;
                        } else if (s.equalsIgnoreCase("false")) {
                            args[i] = false;
                        } else if (NumberUtils.isNumber(s)) {
                            args[i] = NumberUtils.createNumber(s);
                        } else {
                            try {
                                args[i] = types[i].getConstructor(String.class).newInstance(s);
                            } catch (Exception ex) {
                                args[i] = s;
                            }
                        }
                    }
                    m.setAccessible(true);
                    try {
                        return m.invoke(object, args);
                    } finally {
                        m.setAccessible(false);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class && !ended);
        throw new NullPointerException("There is no method with given name");
    }

    /**
     * Invokes and returns result of method with given name and arguments of target object's class.
     * Be aware of that all boolean/number-convertable arguments WILL BE converted to boolean/number.
     *
     * @param object     target object.
     * @param methodName name of the method.
     * @param strings    arguments.
     * @return result of method invokation.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static Object invokeMethodAndGetWithStringArgs(Object object, String methodName, String... strings) throws Exception {
        Class clazz = object.getClass();
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equalsIgnoreCase(methodName)) {
                Class[] types = m.getParameterTypes();
                Object[] args = new Object[strings.length];
                for (int i = 0; i < args.length; ++i) {
                    String s = strings[i];
                    if (s.equalsIgnoreCase("true")) {
                        args[i] = true;
                    } else if (s.equalsIgnoreCase("false")) {
                        args[i] = false;
                    } else if (NumberUtils.isNumber(s)) {
                        args[i] = NumberUtils.createNumber(s);
                    } else {
                        try {
                            args[i] = types[i].getConstructor(String.class).newInstance(s);
                        } catch (Exception ex) {
                            args[i] = s;
                        }
                    }
                }
                m.setAccessible(true);
                try {
                    return m.invoke(object, args);
                } finally {
                    m.setAccessible(false);
                }
            }
        }
        throw new NullPointerException("There is no method with given name");
    }

    public static <T> ClassData<T> getClass(Class<T> clazz) {
        ClassData<T> data = cache.get(clazz.getName());
        if (data == null) {
            cache.put(clazz.getName(), data = new ClassData<>(clazz));
        }
        return data;
    }

    public static <T extends Enum<?>> T addEnum(Class<T> enumType, String name, Object... args) {
        try {
            ClassData<T> data = getClass(enumType);

            // Find field with all enums
            Field field = null;
            try {
                field = data.findFinalField("$VALUES");
            } catch (UnableToFindFieldException ex) {
                try {
                    field = data.findFinalField("ENUM$VALUES"); // Eclipse internal compiler
                } catch (UnableToFindFieldException ignored) {
                }
            }
            if (field == null) {
                int flags = Modifier.STATIC | Modifier.FINAL | 0x00001000 /*SYNTHETIC*/;
                String valueType = "[L" + enumType.getName().replace('.', '/') + ";";
                for (Field f : enumType.getDeclaredFields()) {
                    if ((f.getModifiers() & flags) == flags && f.getType().getName().replace('.', '/').equals(valueType)) {
                        field = f;
                        field.setAccessible(true);
                        ClassData.FIELD_MODIFIERS.set(field, field.getModifiers() & ~Modifier.FINAL);
                        break;
                    }
                }
            }
            if (field == null) {
                throw new UnableToFindFieldException(enumType, "$VALUES");
            }

            // Getting previous enum values
            T[] prev = (T[]) field.get(null);
            List<T> values = new ArrayList<>(Arrays.asList(prev));

            // Adapting constructor parameters
            Object[] params = new Object[args.length + 2];
            params[0] = name;
            params[1] = values.size();
            System.arraycopy(args, 0, params, 2, args.length);

            // Creating new enum instance
            ReflectionFactory rFactory = ReflectionFactory.getReflectionFactory();
            T newValue = (T) rFactory.newConstructorAccessor(data.findConstructor(params)).newInstance(params);

            // Adding new enum
            values.add(newValue);
            field.set(null, values.toArray((T[]) Array.newInstance(enumType, 0)));

            //Clearing cache
            ClassData<Class> cdata = getClass(Class.class);
            cdata.setFinal(enumType, "enumConstants", null);
            cdata.setFinal(enumType, "enumConstantDirectory", null);

            return newValue;
        } catch (Exception e) {
            return null;
        }

    }

    public static class ClassData<K> {
        private static Field FIELD_MODIFIERS = null;

        static {
            try {
                FIELD_MODIFIERS = Field.class.getDeclaredField("modifiers");
                FIELD_MODIFIERS.setAccessible(true);
            } catch (Exception ex) {
            }
        }

        private final Class<K> clazz;
        private final Map<String, Field> fields = new HashMap<>();
        private final Map<Object, Method> methods = new HashMap<>();
        private final Map<Object, Constructor<K>> constructors = new HashMap<>();

        boolean aggressiveOverloading = false;

        public ClassData(Class<K> clazz) {
            this.clazz = clazz;
        }

        public void set(Object instance, String field, Object value) throws Exception {
            this.findField(field).set(instance, value);
        }

        public void setFinal(Object instance, String field, Object value) throws Exception {
            findFinalField(field).set(instance, value);
        }

        public Object get(Object instance, String field) throws Exception {
            return this.findField(field).get(instance);
        }

        public Object invoke(Object instance, String method, Object... args) throws Throwable {
            return this.findMethod(method, args).invoke(instance, args);
        }

        public Object invokeTied(K instance, String method, Object... args) throws Throwable {
            return this.findMethod(method, args).invoke(instance, args);
        }

        public K construct(Object... args) throws Exception {
            return this.findConstructor(args).newInstance(args);
        }

        /**
         * Поиск конструктора с заданными аргументами
         *
         * @param args аргументы
         * @return конструктор или null, если таковой не найден
         */
        public Constructor<K> findConstructor(Object... args) {
            return findConstructor0(toTypes(args));
        }

        /**
         * Поиск конструктора с заданными типами аргументов
         *
         * @param types типы аргументов
         * @return конструктор или null, если таковой не найден
         */
        public Constructor<K> findConstructor0(Class... types) {
            Object mapped = new ConstructorMapKey(types);
            Constructor<K> con = constructors.get(mapped);
            if (con == null) {
                constructorsLoop:
                for (Constructor c : clazz.getDeclaredConstructors()) {
                    Class<?>[] ptypes = c.getParameterTypes();
                    if (ptypes.length != types.length) {
                        continue;
                    }

                    for (int i = 0; i < ptypes.length; i++) {
                        if (types[i] != null && ptypes[i] != types[i] && !ptypes[i].isAssignableFrom(types[i])) {
                            continue constructorsLoop;
                        }
                    }

                    con = c;
                    con.setAccessible(true);
                    constructors.put(mapped, con);
                    break;
                }

                if (con == null) {
                    throw new UnableToFindConstructorException(clazz, types);
                }
            }
            return con;
        }

        /**
         * Рекурсивный поиск метода в классе и всех суперклассам данного класса
         *
         * @param name имя метода
         * @param args аргументы метода
         * @return метод или null, если он не найден
         */
        public Method findMethod(String name, Object... args) {
            Class[] types = null;
            Object mapped;
            if (aggressiveOverloading) {
                types = toTypes(args);
                mapped = new AggressiveMethodMapKey(name, types);
            } else {
                mapped = new MethodMapKey(name, args.length);
            }

            Method method = methods.get(mapped);
            if (method == null) {
                if (types == null) {
                    types = toTypes(args);
                }

                method = fastFindMethod(name, types);

                if (method == null) {
                    throw new UnableToFindMethodException(clazz, name, types);
                } else {
                    methods.put(mapped, method);
                }
            }
            return method;
        }

        /**
         * Рекурсивный поиск метода в классе и всех суперклассам данного класса
         *
         * @param name  имя метода
         * @param types типы аргументов метода
         * @return метод или null, если он не найден
         */
        private Method findMethod0(String name, Class... types) {
            Object mapped;
            if (aggressiveOverloading) {
                mapped = new AggressiveMethodMapKey(name, types);
            } else {
                mapped = new MethodMapKey(name, types.length);
            }

            Method method = methods.get(mapped);
            if (method == null) {
                method = fastFindMethod(name, types);
                if (method == null) {
                    throw new UnableToFindMethodException(clazz, name, types);
                } else {
                    methods.put(mapped, method);
                }
            }
            return method;
        }

        /**
         * Выполняет рекурсивный поиск метода в классе и всех суперклассам данного класса.
         * Увеличение производительности обеспечивается за счет упрощенного сравнения аргументов
         * метода.
         *
         * @param name  имя метода
         * @param types типы аргументов, которые принимает данный метод
         * @return метод или null, если он не найден
         */
        @SuppressWarnings("StringEquality")
        private Method fastFindMethod(String name, Class... types) {
            Method method = null;
            name = name.intern();
            Class clazz0 = clazz;
            do {
                methodsLoop:
                for (Method m : clazz0.getDeclaredMethods()) {
                    if (name != m.getName()) {
                        continue;
                    }

                    Class<?>[] ptypes = m.getParameterTypes();
                    if (ptypes.length != types.length) {
                        continue;
                    }

                    for (int i = 0; i < ptypes.length; i++) {
                        if (types[i] != null && ptypes[i] != types[i] && !ptypes[i].isAssignableFrom(types[i])) {
                            continue methodsLoop;
                        }
                    }

                    method = m;
                    break;
                }
                if (method != null) {
                    method.setAccessible(true);
                    break;
                }
                clazz0 = clazz0.getSuperclass();
            } while (clazz0 != null);
            return method;
        }

        /**
         * Рекурсивный поиск поля по классу и всем суперклассам данного класса<br/>
         * Если поле находится, то у него убирается флаг final
         *
         * @param name имя поля
         * @return поле или null, если оно не найдено
         */
        public Field findFinalField(String name) throws Exception {
            Field field = findField(name);
            FIELD_MODIFIERS.set(field, field.getModifiers() & ~Modifier.FINAL);
            return field;
        }

        /**
         * Рекурсивный поиск поля по классу и всем суперклассам данного класса
         *
         * @param name имя поля
         * @return поле или null, если оно не найдено
         */
        public Field findField(String name) {
            Field field = fields.get(name);
            if (field == null) {
                Class clazz0 = clazz;
                while (clazz0 != null) {
                    try {
                        field = clazz0.getDeclaredField(name);
                        field.setAccessible(true);
                        fields.put(name, field);
                        break;
                    } catch (Exception e) {
                        clazz0 = clazz0.getSuperclass();
                    }
                }
                if (field == null) {
                    throw new UnableToFindFieldException(clazz, name);
                }
            }
            return field;
        }

        /**
         * Преобразует объекты в их классы
         *
         * @param objects объекты
         * @return классы объектов
         */
        private Class[] toTypes(Object[] objects) {
            if (objects.length == 0) {
                return new Class[0];
            }

            Class[] types = new Class[objects.length];
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] == null) {
                    types[i] = null;
                    continue;
                }
                Class type = objects[i].getClass();
                if (type == Integer.class) {
                    type = Integer.TYPE;
                } else if (type == Double.class) {
                    type = Double.TYPE;
                } else if (type == Boolean.class) {
                    type = Boolean.TYPE;
                } else if (type == Float.class) {
                    type = Float.TYPE;
                } else if (type == Long.class) {
                    type = Long.TYPE;
                } else if (type == Character.class) {
                    type = Character.TYPE;
                } else if (type == Byte.class) {
                    type = Byte.TYPE;
                } else if (type == Short.class) {
                    type = Short.TYPE;
                }
                types[i] = type;
            }
            return types;
        }
    }

    static class ConstructorMapKey {
        Class[] types;

        public ConstructorMapKey(Class[] types) {
            this.types = types;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(types);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AggressiveMethodMapKey)) {
                return false;
            }
            AggressiveMethodMapKey other = (AggressiveMethodMapKey) obj;
            if (types.length != other.types.length) {
                return false;
            }
            for (int i = 0; i < types.length; i++) {
                if (types[i] != other.types[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    static class MethodMapKey {
        String name;
        int args;

        public MethodMapKey(String name, int args) {
            this.name = name;
            this.args = args;
        }

        @Override
        public int hashCode() {
            return name.hashCode() + args;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MethodMapKey)) {
                return false;
            }
            MethodMapKey other = (MethodMapKey) obj;
            return other.args == args && other.name.equals(name);
        }
    }

    static class AggressiveMethodMapKey {
        Class[] types;
        String name;

        public AggressiveMethodMapKey(String name, Class[] types) {
            this.name = name;
            this.types = types;
        }

        @Override
        public int hashCode() {
            int hash = name.hashCode();
            hash = 31 * hash + Arrays.hashCode(types);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AggressiveMethodMapKey)) {
                return false;
            }
            AggressiveMethodMapKey other = (AggressiveMethodMapKey) obj;
            if (types.length != other.types.length ||
                    !other.name.equals(name)) {
                return false;
            }
            for (int i = 0; i < types.length; i++) {
                if (types[i] != other.types[i]) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Преобразует массив классов в строку, почти как дескриптор в байткоде
     */
    private static String classesToString(Class[] classes) {
        int iMax = classes.length - 1;
        if (iMax == -1) {
            return "()";
        }

        StringBuilder b = new StringBuilder();
        b.append('(');
        for (int i = 0; ; i++) {
            b.append(classes[i].getName());
            if (i == iMax) {
                return b.append(')').toString();
            }
            b.append(',');
        }
    }

    private static class UnableToFindFieldException extends RuntimeException {
        private final String fieldName;
        private final String className;

        public UnableToFindFieldException(Class clazz, String fieldName) {
            super();
            this.fieldName = fieldName;
            this.className = clazz.getName();
        }

        @Override
        public String getMessage() {
            return toString();
        }

        @Override
        public String toString() {
            return "Unable to find field '" + fieldName + "' in class '" + className + "'";
        }
    }

    private static class UnableToFindMethodException extends RuntimeException {
        protected String methodName;
        protected String className;
        protected Class[] types;

        public UnableToFindMethodException(Class clazz, String methodName, Class[] types) {
            super();
            this.methodName = methodName;
            this.className = clazz.getName();
            this.types = types;
        }

        @Override
        public String getMessage() {
            return toString();
        }

        @Override
        public String toString() {
            return "Unable to find method '" + className + "." + methodName + classesToString(types) + "'";
        }
    }

    private static class UnableToFindConstructorException extends UnableToFindMethodException {
        public UnableToFindConstructorException(Class clazz, Class[] types) {
            super(clazz, null, types);
        }

        @Override
        public String toString() {
            return "Unable to find constructor '" + className + ".<init>" + classesToString(types) + "'";
        }
    }

}
