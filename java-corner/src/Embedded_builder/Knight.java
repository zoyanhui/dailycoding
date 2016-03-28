package embedded_builder;

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
