package com.ivankot.rsasample;

import com.ivankot.rsasample.runtime.Manager;
import org.apache.commons.cli.CommandLine;

/**
 *
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
