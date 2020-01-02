package double_brace_initialize_collection;

import java.util.*;

public class DoubleBrace {
    private List<String> array = new ArrayList<String>()
    {
        {
            add("hello");
        }
    };
}
