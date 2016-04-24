Design Pattern: Immutable Embedded Builder


Last week I wrote about what makes a pattern anti-pattern. This week I present a design pattern… or wait… perhaps this is an anti-pattern. Or is it? Let’ see!

The builder pattern is a programming style when there is a class that build an instance of another. The original aim of the builder pattern is to separate the building process of an object, that can be fairly complex in some cases, from the class of the object itself thus the builder can deliver different types of objects based on how the building process progresses. This is a clear example of the separation of concerns.

Immutable objects are objects that are created and can not be altered after the creation process.

Builders and immutable objects just come together very natural.

The builder and the built objects are very closely related and therefore they are usually put into the same package. But why are they implemented in separate classes? On one hand: they have to be separate classes of course. That is the whole thing is about. But on the other hand: why can not the builder be an inner class of the built class? Builder usually collect the building information in their own state and when the caller requests the object to be built this information is used to build the built object. This “use” is a copy operation most of the time. If the builder is inner class all this information can be stored in the built object. Note that the inner class can access all private parts of the class embedding it. The builder can create a built object just not ready yet and store the build information in it. When requested to build all it does are the final paintings.

This pattern is followed by Guava for the immutable collections. The builders are static inner classes. If you look at the code of ImmutableList you can see that there is an internal Builder class inside the abstract class.

But this is not the only way to embed the builder and the implementation. What if we embed the implementation inside the builder? The builder is the only code that needs mutable access to the class. An interface defining the query methods the class implements should be enough for anybody else. And if we get to this point why not to create a matrjoschka?

Lets have an interface. Lets have a builder inside the interface as an inner class (static and public by default and can not be any other way). Lets have the implementation inside the builder as a private static class implementing the outer interface.

public interface Knight {
    boolean saysNi();

    public class Builder {
        private Implementation implementation = new Implementation();

        public Builder setState(String say) {
            implementation.say = say;
            return this;
        }

        public Implementation build() {
            Implementation knight = implementation;
            implementation = null;
            return knight;
        }

        private static class Implementation implements Knight {
            private String say;

            public boolean saysNi() {
                return say.indexOf("ni") != -1;
            }
        }
    }
}
The builder can access any fields of the Knight implementation since they are in the same top level class. (JLS1.7, section 6.6.1 Determining Accessibility)

There is no other way (except nasty reflection tricks or byte code abuse, which are out of scope for now) to get access to the implementation except using the builder.

The builder can be used to build the implementation and once it returned it it has no access to it anymore, there is no way to modify the implementation via the builder. If the implementation is immutable it is guaranteed to save the state.

Is this a pattern or an antipattern?