//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.ym.materials.serializer;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.Template;
import org.msgpack.template.builder.DefaultBuildContext;
import org.msgpack.template.builder.JavassistTemplateBuilder.JavassistTemplate;
import org.msgpack.unpacker.Unpacker;

public class Person_$$_Template_1509563803_0 extends JavassistTemplate implements Template {
    public Person_$$_Template_1509563803_0(Class var1, Template[] var2) {
        super(var1, var2);
    }

    public void write(Packer var1, Object var2, boolean var3) throws IOException {
        if (var2 == null) {
            if (var3) {
                throw new MessageTypeException("Attempted to write null");
            } else {
                var1.writeNil();
            }
        } else {
            Person var4 = (Person)var2;
            var1.writeArrayBegin(2);
            if (DefaultBuildContext.readPrivateField(var4, Person.class, "name") == null) {
                var1.writeNil();
            } else {
                DefaultBuildContext.writePrivateField(var1, var4, Person.class, "name", super.templates[0]);
            }

            DefaultBuildContext.writePrivateField(var1, var4, Person.class, "age", super.templates[1]);
            var1.writeArrayEnd();
        }
    }

    public Object read(Unpacker var1, Object var2, boolean var3) throws MessageTypeException, IOException {
        if (!var3 && var1.trySkipNil()) {
            return null;
        } else {
            Person var4;
            if (var2 == null) {
                var4 = new Person();
            } else {
                var4 = (Person)var2;
            }

            var1.readArrayBegin();
            if (!var1.trySkipNil()) {
                DefaultBuildContext.readPrivateField(var1, var4, Person.class, "name", super.templates[0]);
            }

            DefaultBuildContext.readPrivateField(var1, var4, Person.class, "age", super.templates[1]);
            var1.readArrayEnd();
            return var4;
        }
    }
}
