package com.comfydns.logprocess;

import com.comfydns.resolver.resolve.logging.EventLogLineType;
import com.comfydns.resolver.resolve.rfc1035.message.struct.Question;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComfyLogProcessor {
    private static final Pattern EVENT_LOG_LINE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d+-\\d{4} \\w+ EventLogger - \\[EVENT\\]: (?<event>\\{.*\\})");
    private static final Gson gson = new Gson();

    public static void main(String ... args) throws IOException {
        OptionParser parser = new OptionParser();
        OptionSpec<String> file = parser.accepts("file").withRequiredArg().ofType(String.class).required();
        OptionSpec<Void> help = parser.accepts("help").forHelp();
        OptionSet options = parser.parse(args);

        if(options.has(help)) {
            parser.printHelpOn(System.out);
            return;
        }

        try(
            InputStream is = getInputStream(options.valueOf(file));
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader reader = new BufferedReader(isr);
        ) {
            processStream(reader);
        }
    }

    public static RequestMatcher processStream(BufferedReader reader) throws IOException {
        RequestMatcher processor = new RequestMatcher();

        String line = reader.readLine();
        while(line != null ) {
            Matcher matcher = EVENT_LOG_LINE_PATTERN.matcher(line);
            if(matcher.matches()) {
                String rawEvent = matcher.group("event");
                JsonObject event = gson.fromJson(rawEvent, JsonObject.class);
                switch(EventLogLineType.valueOf(event.get("eventType").getAsString())) {
                    case REQUEST_IN:
                        RequestInEvent eIn = new RequestInEvent(event);
                        processor.process(eIn);
                        break;
                    case REQUEST_OUT:
                        RequestOutEvent eOut = new RequestOutEvent(event);
                        processor.process(eOut);
                        break;
                }
            }
            line = reader.readLine();
        }

        System.out.println("Summary:");
        System.out.println("No In: " + processor.getNoIn());
        System.out.println("Matched: " + processor.getMatched());
        System.out.println("Dangling: ");
        for (RequestInEvent e : processor.getLive()) {
            for (Question q : e.getQuestions()) {
                System.out.format("[%s] [%s] %s\n", e.getEventTime(), e.getRequestId(), q.toString());
            }
        }

        return processor;
    }

    private static InputStream getInputStream(String path) throws IOException {
        if(path.equals("-")) {
            return System.in;
        } else {
            Path filePath = Path.of(path);

            if(!Files.exists(filePath)) {
                throw new IllegalArgumentException(path + " does not exist.");
            }

            return Files.newInputStream(filePath, StandardOpenOption.READ);
        }
    }
}
