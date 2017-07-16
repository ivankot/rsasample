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

package com.ivankot.rsasample.runtime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author Ivan
 */
public enum Cli {

    /**
     * Singleton instance of the Cli class
     */
    INSTANCE;

    /**
     * Application name
     */
    public static final String APPLICATION = "rsasample";

    /**
     * STDOUT identifier
     */
    public static final String DEFAULT_OUTPUT = "stdout";

    /**
     *
     */
    public static final String CMD_KEY = "k";
    public static final String CMD_KEY_LONG = "key";
    public static final String CMD_KEY_DESC = "Path to private/public key";

    /**
     *
     */
    public static final String CMD_ENCODE = "e";
    public static final String CMD_ENCODE_LONG = "encrypt";
    public static final String CMD_ENCODE_DESC = "Tells to encrypt input using private key";

    /**
     *
     */
    public static final String CMD_DECODE = "d";
    public static final String CMD_DECODE_LONG = "decrypt";
    public static final String CMD_DECODE_DESC = "Tells to decrypt input using public key";

    /**
     *
     */
    public static final String CMD_OUTPUT = "o";
    public static final String CMD_OUTPUT_LONG = "output";
    public static final String CMD_OUTPUT_DESC = "File/stream to use as output, if no given will write to stdout";

    /**
     *
     */
    public static final String CMD_HELP = "h";
    public static final String CMD_HELP_LONG = "help";
    public static final String CMD_HELP_DESC = "Display this help menu";

    /**
     *
     */
    public static final String CMD_GENERATE = "g";
    public static final String CMD_GENERATE_LONG = "generate";
    public static final String CMD_GENERATE_DESC = "Generate private & public key in the current directory";

    /**
     *
     */
    public static final String CMD_BACKGROUND = "b";
    public static final String CMD_BACKGROUND_LONG = "background";
    public static final String CMD_BACKGROUND_DESC = "Execute encryption/decryption in the background";

    /**
     *
     */
    public static final String CMD_VERBOSE = "v";
    public static final String CMD_VERBOSE_LONG = "verbose";
    public static final String CMD_VERBOSE_DESC = "Be verbose about what's going on";

    private static final String ERR_DEFINE_ACTION = "Please define action: encode, decode, generate";
    private static final String ERR_DEFINE_KEY = "Please specify the key to use";
    private static final String ERR_DEFINE_KEY_SOURCE = "Please specify valid key and input";
    private static final String ERR_OUTPUT_NOT_WRITABLE = "Please make sure output path is writable";
    private static final String ERR_GEN_PATH_NOT_WRITABLE = "Current directory is not writable - cannot generate the keys";

    private final Options options = new Options();

    private CommandLine cmd = null;
    private String lastError = null;

    private Cli() {
        populateOptions();
    }

    /**
     * Initialize the Cli by passing and parsing cmd args
     * @param args cmd args
     * @return result, true if success, false if not, last error available via printErrors
     */
    public boolean init(String[] args) {
        boolean result = false;
        CommandLineParser cmp = new DefaultParser();
        try {
            cmd = cmp.parse(options, args);
            result = true;
        } catch (ParseException ex) {
            setError(ex.getMessage());
        }
        return result;
    }

    /**
     * Validate input based on pre-defined rules for it
     * @return validation result, last error available via printErrors
     */
    public boolean validate() {
        boolean result = false;

        if (null != cmd) {
            if (cmd.hasOption(CMD_HELP)) {
                result = true;
            } else if (cmd.hasOption(CMD_ENCODE) || cmd.hasOption(CMD_DECODE) || cmd.hasOption(CMD_GENERATE)) {
                if (cmd.hasOption(CMD_ENCODE) || cmd.hasOption(CMD_DECODE)) {

                    if (cmd.hasOption(CMD_KEY)) {
                        String filePath = cmd.hasOption(CMD_ENCODE)
                                ? cmd.getOptionValue(CMD_ENCODE)
                                : cmd.getOptionValue(CMD_DECODE);
                        String keyPath = cmd.getOptionValue(CMD_KEY);

                        Path source = Paths.get(filePath);
                        Path key = Paths.get(keyPath);

                        if (Files.exists(key) && Files.exists(source)) {
                            if (cmd.hasOption(CMD_OUTPUT)) {
                                String outputPath = cmd.getOptionValue(CMD_OUTPUT);
                                Path output = Paths.get(outputPath).toAbsolutePath();

                                if (Files.exists(output) || Files.isWritable(output.getParent())) {
                                    result = true;
                                } else {
                                    setError(ERR_OUTPUT_NOT_WRITABLE);
                                }

                            } else {
                                result = true;
                            }
                        } else {
                            setError(ERR_DEFINE_KEY_SOURCE);
                        }

                    } else {
                        setError(ERR_DEFINE_KEY);
                    }

                } else {
                    Path currentPath = Paths.get("");
                    if (Files.isWritable(currentPath)) {
                        result = true;
                    } else {
                        setError(ERR_GEN_PATH_NOT_WRITABLE);
                    }
                }
            } else {
                setError(ERR_DEFINE_ACTION);
            }
        }

        return result;
    }

    /**
     * Returns command based on args submitted
     * @return string id of the command parsed: encrypt/decrypt, generate, or help
     */
    public String getAction() {
        String action = null;
        if (null != cmd) {
            if (cmd.hasOption(CMD_ENCODE)) {
                action = CMD_ENCODE;
            } else if (cmd.hasOption(CMD_DECODE)) {
                action = CMD_DECODE;
            } else if (cmd.hasOption(CMD_GENERATE)) {
                action = CMD_GENERATE;
            } else if (cmd.hasOption(CMD_HELP)) {
                action = CMD_HELP;
            }
        }
        return action;
    }

    /**
     * Prepares a map with all of the options required to perform an action
     * @param action string id of the action
     * @return map with options in a form of <String, Object>
     */
    public Map<String, Object> getOptionsForAction(String action) {
        Map<String, Object> actionOptions = new HashMap<>();
        actionOptions.put(CMD_KEY, cmd.getOptionValue(CMD_KEY));
        actionOptions.put(CMD_BACKGROUND, cmd.hasOption(CMD_BACKGROUND));
        actionOptions.put(CMD_VERBOSE, cmd.hasOption(CMD_VERBOSE));

        String outputValue = cmd.hasOption(CMD_OUTPUT)
                ? cmd.getOptionValue(CMD_OUTPUT)
                : DEFAULT_OUTPUT;

        actionOptions.put(CMD_OUTPUT, outputValue);

        switch (action) {

            case CMD_ENCODE:
                actionOptions.put(CMD_ENCODE, cmd.getOptionValue(CMD_ENCODE));
                break;

            case CMD_DECODE:
                actionOptions.put(CMD_DECODE, cmd.getOptionValue(CMD_DECODE));

        }

        return actionOptions;
    }

    /**
     * Prints the usage window using the default commons HelpFormatter
     */
    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(APPLICATION, options);
    }

    /**
     * Prints last error for init or validate
     */
    public void printErrors() {
        System.out.println(lastError);
    }

    private void populateOptions() {
        options.addOption(CMD_KEY, CMD_KEY_LONG, true, CMD_KEY_DESC);
        options.addOption(CMD_ENCODE, CMD_ENCODE_LONG, true, CMD_ENCODE_DESC);
        options.addOption(CMD_DECODE, CMD_DECODE_LONG, true, CMD_DECODE_DESC);
        options.addOption(CMD_OUTPUT, CMD_OUTPUT_LONG, true, CMD_OUTPUT_DESC);
        options.addOption(CMD_HELP, CMD_HELP_LONG, false, CMD_HELP_DESC);
        options.addOption(CMD_GENERATE, CMD_GENERATE_LONG, false, CMD_GENERATE_DESC);
        options.addOption(CMD_BACKGROUND, CMD_BACKGROUND_LONG, false, CMD_BACKGROUND_DESC);
    }

    private void setError(String message) {
        lastError = message;
    }

}
