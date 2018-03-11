/**
 * Copyright 2018 Keith Schulze
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package edu.monash.ssee;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.script.ScriptLanguage;
import org.scijava.script.ScriptService;

import jnr.ffi.Struct.id_t;


public class ScalaScriptEngineTest {

    private ScriptEngine sse;
    private final static String LINESEP = System.getProperty("line.separator");

    @Before
    public void setUp() {

        // First create the Scala script engine
        final Context context = new Context(ScriptService.class);
		ScriptService scriptService = context.getService(ScriptService.class);

		final ScriptLanguage language =
			scriptService.getLanguageByExtension("scala");
        sse = language.getScriptEngine();
    }


    @Test
    public void accessInt() throws ScriptException {
        // Create some bindings
        Bindings bindings = sse.createBindings();

        // Add an integer to the bindings
        bindings.put("one", 1);

        // Add 2 to the integer in the bindings. Note we need to cast
        // the integer in the bindings to an Int using asInstanceOf.
        String script = String.join(
            LINESEP, //line separator
            "val three: Int = one.asInstanceOf[Int] + 2",
            "three" // eval three to return it from script
        );

        // note we need to cast output of script to its correct type too!
        int out = (int) sse.eval(script, bindings);
        assertEquals(3, out);
    }

    @Test
    public void arrayLength() throws ScriptException {
        // Create some bindings
        Bindings bindings = sse.createBindings();

        // put an array in the bindings
        bindings.put("arr", new int[]{1, 2, 3, 4, 5});

        // let's access an array method in the scala script.
        // note: without casting this would throw a script
        // exception, value length is not a member of Object.
        String script = String.join(
            LINESEP,
            "val len: Int = arr.asInstanceOf[Array[Int]].length",
            "len" // eval len to return it from script
        );

        // note we need to cast output of script to its correct type too!
        int len = (int) sse.eval(script, bindings);

        assertEquals(5, len);
    }

    @Test
    public void mapArray() throws ScriptException {
        // Create some bindings
        Bindings bindings = sse.createBindings();

        // put an array in the bindings
        bindings.put("arr", new int[]{1, 2, 3, 4, 5});

        // Add 1 to each element of the array using map.
        String script = String.join(
            LINESEP,
            "val out: Array[Int] = arr.asInstanceOf[Array[Int]].map(_ + 1)\n",
            "out"
        );

        // note we need to cast output of script to its correct type too!
        int[] out = (int[]) sse.eval(script, bindings);

        assertArrayEquals(new int[]{2,3,4,5,6}, out);
    }

    @Test
    public void accessMap() throws ScriptException {
        // Create some bindings
        Bindings bindings = sse.createBindings();

        // Create a Map<String, String> and add it to our bindings
        Map<String, String> strMap = new HashMap<>();
        strMap.put("test1", "Hello");
        strMap.put("test2", "World!");
        bindings.put("_strMap", strMap);

        // Get test1 & test2 strings from the Map in the bind and concatenate
        // them. Note again that we first need to cast the ohject in
        // binding to the correct type i.e., asInstanceOf[Map[String,String]]]
        String script = String.join(
            LINESEP,
            "import java.util.Map\n",
            "val strMap = _strMap.asInstanceOf[Map[String, String]]\n",
            "val hw = s\"${strMap.get(\"test1\")} ${strMap.get(\"test2\")}\"\n",
            "hw"
        );

        // note we need to cast output of script to its correct type too!
        String out = (String) sse.eval(script, bindings);
        assertEquals("Hello World!", out);
    }

    @Test
    public void accessString() throws ScriptException {
        // Create some bindings
        Bindings bindings = sse.createBindings();

        // Add a string String to the Bindings
        bindings.put("hello", "Hello from the");

        // generally we don't need to cast Strings because I think the
        // Objects toString method gets called automatically.
        String script = String.join(
            LINESEP,
            "val greeting: String = s\"$hello Scala Script Engine\"",
            "greeting" // evals greeting to return ia from script
        );

        String out = (String) sse.eval(script, bindings);
        assertEquals("Hello from the Scala Script Engine", out);
    }
}