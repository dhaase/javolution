package org.javolution.io;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RunWith(BlockJUnit4ClassRunner.class)
public class StructTest {

    /**
     * <pre><code>
     * enum Gender{MALE, FEMALE};
     *
     * struct Date {
     *    unsigned short year;
     *    unsigned byte month;
     *    unsigned byte day;
     * };
     *
     * struct Student {
     *    enum Gender gender;
     *    char        name[64];
     *    struct Date birth;
     *    float       grades[10];
     *    Student*    next;
     * };
     * </code></pre>
     */
    @Test
    public void test() throws IOException {
        Student student1 = new Student();
        student1.gender.set(Gender.MALE);
        student1.name.set("John Doe"); // Null terminated (C compatible)
        int age = 2003 - student1.birth.year.get();
        student1.grades[2].set(12.5f);
        student1.number1.set(12345L);
        student1.number2.set(54321L);
        System.out.println("student1.gender.get(): " + student1.gender.get());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        student1.write(baos);

        byte[] bytes = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Student student1a = new Student();
        student1a.read(bais);

        System.out.println("student1.gender.get(): " + student1a.gender.get());
        System.out.println("student1.number1: " + student1.number1.get());
        System.out.println("student1.number2: " + student1.number2.get());
    }

    public enum Gender {MALE, FEMALE}


    public static class Date extends Struct {
        public final Unsigned16 year = new Unsigned16();
        public final Unsigned8 month = new Unsigned8();
        public final Unsigned8 day = new Unsigned8();
    }

    public static class Student extends Struct {
        public final Enum32<Gender> gender = new Enum32<Gender>(Gender.values());
        public final UTF8String name = new UTF8String(128);
        public final Date birth = inner(new Date());
        public final Signed64 number1 = new Signed64();
        public final Float32[] grades = array(new Float32[10]);
        public final Signed64 number2 = new Signed64();
    }
}
