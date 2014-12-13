package com.github.relai.vertx.springdata.example.todosapp.handlebars;

import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import java.io.IOException;

/**
 * Custom function for handlerbars.
 * 
 * @author relai
 */
public class IfEqualHelper implements Helper{

    @Override
    public CharSequence apply(Object context, Options options) throws IOException {
        Object obj = options.param(0);
        CharSequence result = null;       
        if (context != null && context.equals(obj)) {
            //result = options.fn();
            result = options.param(1);
        }
        return result;
    }
    
}
