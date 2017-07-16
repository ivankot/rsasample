package com.ivankot.rsasample.runtime;

import com.ivankot.rsasample.crypto.Generator;
import com.ivankot.rsasample.crypto.Provider;
import com.ivankot.rsasample.crypto.Provider.Cipher.Builder;
import java.util.Map;

/**
 *
 * @author Ivan
 */
public class Manager {

    public static final String MSG_ENCRYPTION_SUCCESS = "Encryption completed successfully";
    public static final String MSG_ENCRYPTION_FAILURE = "Encryption was not completed";
    public static final String MSG_DECRYPTION_SUCCESS = "Decryption completed successfully";
    public static final String MSG_DECRYPTION_FAILURE = "Decryption was not completed";
    public static final String MSG_GENERATION_SUCCESS = "Generted keys in the current directory";

    private final String[] args;

    private final Cli cli = Cli.INSTANCE;

    /**
     *
     * @param args
     */
    public Manager(String[] args) {
        this.args = args;
    }

    /**
     *
     */
    public void orchestrate() {
        if (cli.init(args) && cli.validate()) {
            String action = cli.getAction();
            Map<String, Object> actionOptions = cli.getOptionsForAction(action);
            Provider provider = Provider.INSTANCE;
            String message = null;
            boolean result;

            switch (action) {

                case Cli.CMD_ENCODE:
                    result = configureBuilder(provider.getEncoder().builder(), actionOptions).doFinal();
                    message = (result)
                            ? MSG_ENCRYPTION_SUCCESS
                            : MSG_ENCRYPTION_FAILURE;
                    break;

                case Cli.CMD_DECODE:
                    result = configureBuilder(provider.getDecoder().builder(), actionOptions).doFinal();
                    message = (result)
                            ? MSG_DECRYPTION_SUCCESS
                            : MSG_DECRYPTION_FAILURE;
                    break;

                case Cli.CMD_GENERATE:
                    Generator generator = new Generator(Provider.INSTANCE);
                    result = generator.generate();
                    message = (result)
                            ? MSG_GENERATION_SUCCESS
                            : generator.getLastError();
                    break;

                case Cli.CMD_HELP:
                    cli.printHelp();
                    break;

            }

            if (null != message) 
                System.out.println(message);

        } else {
            cli.printErrors();
            cli.printHelp();
        }
    }

    private Builder configureBuilder(Builder builder, Map<String, Object> actionOptions) {

        Object input = actionOptions.containsKey(Cli.CMD_ENCODE)
                ? actionOptions.get(Cli.CMD_ENCODE)
                : actionOptions.get(Cli.CMD_DECODE);

        return builder
                .background((boolean) actionOptions.get(Cli.CMD_BACKGROUND))
                .verbose((boolean) actionOptions.get(Cli.CMD_VERBOSE))
                .key((String) actionOptions.get(Cli.CMD_KEY))
                .input((String) input)
                .output((String) actionOptions.get(Cli.CMD_OUTPUT));
    }

}
