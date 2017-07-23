/*
 * The MIT License
 *
 * Copyright 2017 Ivan.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
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

package com.ivankot.rsasample;

import com.ivankot.rsasample.runtime.Manager;
import org.apache.commons.cli.CommandLine;

/**
 * Application entry point, creates a new thread and executes the app
 * @author Ivan
 */
public class Hello {

    private CommandLine cmd;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        ((Runnable) () -> {
            new Hello().runApp(args);
        }).run();

    }

    /**
     * Runs the container and parses the arguments, then passes everything to a
     * Director to orchestrate the process further
     *
     * @param args cmd line args
     */
    public void runApp(String[] args) {
        
        new Manager(args).orchestrate();

    }


}
